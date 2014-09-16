import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;

public class Updater implements PropertyChangeListener{
	private JFileChooser fc;
	public DBConnector db;
	MainFrame mainFrame;
	public boolean png;
	public JFrame progressFrame;
	private JProgressBar progressBar;
	public int progressResult;
	public int progressCounter;
	private boolean someError;
	String feedback;
	String command;
	public Task backgroundTask;
	public int numberOfVideos;

	public Updater(MainFrame mf){
		mainFrame = mf;
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		db = mainFrame.db;
		try {
			db.initializeDB();
		} catch (Exception e) {
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
	}

	protected void updateDB(String theCommand) {
		command = theCommand;
		backgroundTask = new Task();
		backgroundTask.addPropertyChangeListener(this);
		backgroundTask.execute();
	}

	public class Task extends SwingWorker<Void, Void> {
		
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() throws Exception{
			doTheUpdate();
			System.out.println("Did the update");
			return null;
		}

		public void updateProgress(int input){
			setProgress(input);
		}
		private void doTheUpdate() {
			someError = false;
			boolean updateMode = "updateProject".equals(command);
			boolean customMode = "addCustomProject".equals(command);
			if(customMode){				
				CustomAddProjectFrame customAddProjFrame = new CustomAddProjectFrame();
				customAddProjFrame.setBounds(500, 200, 518, 423);
				customAddProjFrame.setVisible(true);
			}
			else{
				int returnVal = fc.showOpenDialog(null);
				if(returnVal!=JFileChooser.APPROVE_OPTION){
					System.out.println("Nothing chosen");
					return;
				}
				File f = fc.getSelectedFile();
				String projectName = f.getName();
				File[] videos = f.listFiles();
				if(videos==null || videos.length==0){
					JOptionPane.showMessageDialog(null, "The file you selected was not a valid project directory.","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				String projectAddress = f.getAbsolutePath();
				//Make sure this is a new project, at least by making sure the address is new
				try {
					boolean isNew = db.checkProjectIsNew(projectAddress);
					if(!isNew && !updateMode){
						JOptionPane.showMessageDialog(null, "This project has already been added to the database. No operation was performed.","Error",JOptionPane.ERROR_MESSAGE);
						someError = true;
						return;
					}
					else if(isNew && updateMode){
						JOptionPane.showMessageDialog(null, "This project does not exist in the database. No operation was performed.","Error",JOptionPane.ERROR_MESSAGE);
						someError = true;
						return;
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					MainFrame.printErrorLog(e1);
					JOptionPane.showMessageDialog(null, "There was an error while checking whether the project is new.","Error",JOptionPane.ERROR_MESSAGE);
					someError = true;
					return;
				}
				try {
					boolean isNew = db.checkProjectNameIsNew(projectName);
					if(!isNew && !updateMode){
						JOptionPane.showMessageDialog(null, "A project with this name already exists in the database. Make sure the project is new\nand try renaming it.","Error",JOptionPane.ERROR_MESSAGE);
						someError = true;
						return;
					}
					else if(isNew && updateMode){
						JOptionPane.showMessageDialog(null, "A project with this name does not exist in the database. No operation was performed.","Error",JOptionPane.ERROR_MESSAGE);
						someError = true;
						return;
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					MainFrame.printErrorLog(e1);
					JOptionPane.showMessageDialog(null, "There was some error while checking whether the project is new.","Error",JOptionPane.ERROR_MESSAGE);
					someError = true;
					return;
				}
				String language = "";
				if(!updateMode){
					language = (String) JOptionPane.showInputDialog(null, "Please insert the language for this project.");
				}


				//
				progressFrame = new JFrame();
				progressBar = new JProgressBar(0,100);
				progressBar.setValue(0);
				progressBar.setStringPainted(true);
				JLabel loadingLabel = new JLabel("Loading...");
				progressFrame.setBounds(500, 400, 380, 100);
				JPanel somePanel = new JPanel();
				progressFrame.add(somePanel);
				somePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				somePanel.add(loadingLabel);
				somePanel.add(progressBar);
				progressFrame.setVisible(true);
				//
				numberOfVideos = 0;
				for (File video: videos){
					if(!video.getName().startsWith(".") && video.getName().contains("_") && video.getName().contains("-")){
						numberOfVideos++;
					}
				}
				System.out.println("Number of videos: "+numberOfVideos);

				int videoCounter = 0;
				for (File video: videos){
					System.out.println("Examining a new folder: "+video.getName());
					if(!video.getName().contains("_")){
						System.out.println("No underscore");
						continue;
					}
					//			int underscoreIndex = video.getName().indexOf("_");
					//			String subjectName = video.getName().substring(0, underscoreIndex);
					String videoName = video.getName();
					String videoAddress = video.getAbsolutePath();
					File framesDirectory = new File(video.getAbsolutePath()+"/frames");
					if(!framesDirectory.exists()){
						System.out.println("No frames directory");
						continue;
					}
					File[] theFiles = framesDirectory.listFiles();
					//				for(File file:theFiles){
					//					if(file.getName().startsWith("frame-")){
					//						String newName = file.getParentFile().getAbsolutePath()+"/"+file.getName().substring(6);
					//						boolean success = file.renameTo(new File(newName));
					//						System.out.println(success);
					//					}
					//				}
					theFiles = framesDirectory.listFiles();
					HashMap<String,ImageData> result = new HashMap<String,ImageData>();
					progressCounter = 0;
					System.out.println("Start reading the files");

					//Save the image files
					for(File file:theFiles){
						if(file.getName().endsWith("png") || file.getName().endsWith("jpg")){
							if(file.getName().endsWith("png")){
								png = true;
							}
							else{
								png = false;
							}
							//it is an image
							ImageData image = new ImageData();
							//					image.project = projectName;
							//					image.subject = subjectName;
							String name = file.getName();
							if(name.contains("frame-")){
								name = name.replace("frame-", "");
							}
							image.title = name;
							//					image.video = videoName;
							image.address = file.getAbsolutePath();
							result.put(image.title,image);
							progressCounter++;
							progressResult = 100*videoCounter/numberOfVideos + (5*progressCounter/theFiles.length) /numberOfVideos;
							setProgress(progressResult);
						}
					}
					System.out.println("Finished reading the files");
					ArrayList<Trace> traceFiles = new ArrayList<Trace>();

					//Also look in the possibly existent folder "completedTraces"
					File tracesDirectory = new File(video.getAbsolutePath()+"/completedTraces");
					File[] theTraceFolderFiles = null;
					if(tracesDirectory.exists()){
						theTraceFolderFiles = tracesDirectory.listFiles();
					}
					ArrayList<File> possibleTraceFiles = new ArrayList<File>();
					for(File file: theFiles){
						possibleTraceFiles.add(file);
					}
					if(tracesDirectory.exists()){
						for(File file: theTraceFolderFiles){
							possibleTraceFiles.add(file);
						}
					}
					for(File file: possibleTraceFiles){
						if( (file.getName().contains("png") || file.getName().contains("jpg")) && file.getName().endsWith("txt")){
							Trace trace = new Trace();
							trace.address = file.getAbsolutePath();
							int pngIndex = file.getName().indexOf("png");
							if(pngIndex==-1){
								pngIndex = file.getName().indexOf("jpg");
							}
							String imageName = file.getName().substring(0, pngIndex+3);
							if(imageName.contains("frame-")){
								imageName = imageName.replace("frame-", "");
							}
							int nextDotIndex = pngIndex+4+file.getName().substring(pngIndex+4).indexOf(".");
							String tracerName = file.getName().substring(pngIndex+4,nextDotIndex);
							trace.tracer = tracerName;
							trace.imageName = imageName;
							traceFiles.add(trace);
							progressCounter++;
							progressResult = 100*videoCounter/numberOfVideos + (5*progressCounter/theFiles.length) /numberOfVideos;
							setProgress(progressResult);
						}
					}

					//Fix the names of traces files that look like 01b_1;2_sad_m_si_T_frame-0005273.png.MAK.traced
					for(Trace trace: traceFiles){
						trace.imageName = trace.imageName.replaceAll(".*?(\\d+\\..*)", "$1");
					}

					System.out.println("Done with the images");
					try {
						db.addImages(result, traceFiles, projectName, projectAddress, videoName, videoAddress, language, updateMode);
					} catch (Exception e) {
						e.printStackTrace();
						MainFrame.printErrorLog(e);
						System.out.println("Error in the update process!");
						JOptionPane.showMessageDialog(null, "There was some error in the update process. Restarting the\napplication might solve the problem.","Error",JOptionPane.ERROR_MESSAGE);
						someError = true;
						return;
					}
					//The textgrid
					//This part should be done after adding the images to the db because we should know the IDs
					System.out.println("Start the textGrid");
					File[] filesInsideVideo = video.listFiles();
					File textGridFile = null;
					for(File file: filesInsideVideo){
						if(file.getName().endsWith(".TextGrid") && !file.getName().startsWith("_")){
							textGridFile = file;
							break;
						}
					}
					if(textGridFile!=null){
						TextGridReader tgr = new TextGridReader(Updater.this);
						tgr.addTextGridData(result, textGridFile);
					}
					videoCounter++;
					System.out.println("Finished with one video file");
				}
			}
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			try{
				get();
				if(progressFrame==null){
					return;
				}
				progressFrame.setVisible(false);
				progressFrame.dispose();
				System.out.println("progress finished: "+this.getProgress());

				//Update the search box combo boxes with the new data
				mainFrame.searchbox.updateData();
				if(!someError){					
					JOptionPane.showMessageDialog(null, "The database was updated successfully.");				
				}
			}
			catch (Exception e){
				e.printStackTrace();
				MainFrame.printErrorLog(e);
			}
		}
		
	}


	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if("progress".equals(evt.getPropertyName())){
			int result = (Integer) evt.getNewValue();
			progressBar.setValue(result);
			progressBar.repaint();
			progressBar.validate();
			progressFrame.repaint();
			progressFrame.validate();

		}
	}

	public void removeProject() {
		String title = "Remove project";
		String message = "Please choose the project you want to delete. All images and\ndata associated with them will be deleted from the database,\nbut they will not be deleted from disk.";
		String userInput = (String)JOptionPane.showInputDialog(null,message,title, JOptionPane.PLAIN_MESSAGE, null, mainFrame.searchbox.projectsList, mainFrame.searchbox.projectsList[0]);
		if(userInput!=null && userInput.length()==0){
			JOptionPane.showMessageDialog(null, "No name was entered.","Error",JOptionPane.ERROR_MESSAGE);
		}
		else if(userInput!=null){

			//Clear the table
			mainFrame.tableData = new ArrayList<ImageData>();
			mainFrame.numberOfPages = 1;
			mainFrame.setCurrentPage(1);
			MainFrame.resultSize = 0;
			String text = MainFrame.resultSize+" results";
			mainFrame.queryResultLabel.setText(text);

			try {
				db.deleteProject(userInput);
			} catch (SQLException e) {
				e.printStackTrace();
				MainFrame.printErrorLog(e);
			}

			//We should update the list of tags, experiments, and tracers after we are done.
			mainFrame.searchbox.updateData();

			JOptionPane.showMessageDialog(null, "The project and its associated entries were deleted successfully.");



		}
	}
}
