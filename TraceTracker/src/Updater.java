import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.DefaultComboBoxModel;
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
	private DBConnector db;
	MainFrame mainFrame;
	public boolean png;
	private JFrame progressFrame;
	private JProgressBar progressBar;
	private int progressResult;
	private int progressCounter;
	private boolean someError;
	String feedback;
	String command;
	Task backgroundTask;

	public Updater(MainFrame mf){
		mainFrame = mf;
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		db = mainFrame.db;
		try {
			db.initializeDB();
		} catch (Exception e) {
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
	}

	protected void updateDB(String theCommand) {
		command = theCommand;
		backgroundTask = new Task();
		backgroundTask.addPropertyChangeListener(this);
		backgroundTask.execute();
	}

	
	public static String getSegmentSpelling(String detailedSpelling){
		detailedSpelling = detailedSpelling.replace("neutral", "neut");
		if(detailedSpelling.contains(" ")){
			String result = detailedSpelling.split("\\s+")[1];
			if(result.matches(".*[ifpncv]") || result.endsWith("f") || result.matches(".*[0-9]")){
				if(result.length()>1){
					result = result.substring(0,result.length()-1);				
				}
			}
			if(result.startsWith("V")){
				result = "V";
			}
			return result;
		}
		else{
			return detailedSpelling;
		}
	}



	class Task extends SwingWorker<Void, Void> {
		private int numberOfVideos;
		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public Void doInBackground() throws Exception{
			doTheUpdate();
			System.out.println("Did the update");
			return null;
		}

		private void doTheUpdate() {
			someError = false;
			boolean updateMode = "updateProject".equals(command);
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
				mainFrame.printErrorLog(e1);
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
				mainFrame.printErrorLog(e1);
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
					mainFrame.printErrorLog(e);
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
					addTextGridData(result, textGridFile);
				}
				videoCounter++;
				System.out.println("Finished with one video file");
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
				mainFrame.printErrorLog(e);
			}
		}
		private void addTextGridData(HashMap<String, ImageData> result, File textGridFile) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(textGridFile,"UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mainFrame.printErrorLog(e);
			}
			int tier = -1;
			double duration = 0;
			double lastxmin = 0;
			double lastxmax = 0;
			double frameDuration = 0;
			//We keep track of the words we have added and then add segments to their sequences.
			WordEntry[] words = new WordEntry[result.size()];
			int wordsSize = 0;
			int wordIter = 0; //This is used for the same purpose!
			//See how long the file names are (since there are a lot of zeros) to be able to create file names using frame numbers
			String sampleName = result.keySet().iterator().next();
			sampleName = sampleName.replace(".png", "");
			sampleName = sampleName.replace(".jpg", "");
			int wordID = -1;
			int segmentID = -1;
			int fileNameLength = sampleName.length();
			int numberOfLines = 0;
			while (scanner.hasNextLine()) {
				numberOfLines++;
				scanner.nextLine();
			}
			int lineCount =-1;
			scanner.close();
			try {
				scanner = new Scanner(textGridFile,"UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mainFrame.printErrorLog(e);
			}
			lineReaderloop:
			while(scanner.hasNextLine()){
				lineCount++;
				int addedProgress = (95*lineCount/numberOfLines)/numberOfVideos;
				if(lineCount%1==0){					
				}
				setProgress(progressResult+addedProgress);
				String line = scanner.nextLine();
				if(line.contains("class =")){
					tier++;
				}
				else if(line.contains("xmax") && tier==0){
					//Find the duration here.
					duration = Double.valueOf(line.substring(line.indexOf("xmax")+7));
					frameDuration = duration/result.size();
				}
				else if(line.contains("xmin")){
					lastxmin = Double.valueOf(line.substring(line.indexOf("xmin")+7));
				}
				else if(line.contains("xmax")){
					lastxmax = Double.valueOf(line.substring(line.indexOf("xmax")+7));
				}
				else if(line.contains("text = ")){
					int beginIndex = line.indexOf("text")+8;
					String rest = line.substring(beginIndex);
					String text = rest.substring(0,rest.indexOf("\""));
					int firstFrame = (int) Math.round(lastxmin/frameDuration)+1;
					int lastFrame = (int) Math.round(lastxmax/frameDuration);
					//We should be careful here because the interval can be so short as to not cover the center of any frame.
					//Such intervals should ignored. (In reality this case mostly happens if something is wrong with our TextGrid file.)
//					if(lastFrame<firstFrame){
//						System.out.println("Some segments were very small: "+firstFrame+"\t"+lastFrame);
//					}

					if(tier==1 && text.length()>0){
						//adding a word. We add the word to the database here.
						//Also later in the same iteration we assign the ID of this word to all the images that belong to it.
						//Adding the relevant segment sequence and segment ID sequence to this word in the database will
						//happen in the iterations where tier==2 (When adding segments).
						//The way it works is that whenever a segment is added to the database, it is also appended
						//to the sequence of its corresponding word in words[]. When the sequence for each word in words[]
						//is complete, the new data about the word are updated in the database.
						try {
							wordID = db.addWord(text);
						} catch (SQLException e) {
							e.printStackTrace();
							mainFrame.printErrorLog(e);
						}
						if(wordsSize>=words.length){
							System.err.println("The number of words in the TextGrid file exceeds the number of frames!");
							continue;
						}
						else{
							words[wordsSize] = new WordEntry(text, firstFrame, lastFrame, lastxmin, lastxmax, wordID);
							wordsSize++;						
						}
					}
					if(tier==2){
						//Adding a segment
						if(text.length()>0){
							try {
								segmentID = db.addSegment(text);
							} catch (SQLException e) {
								e.printStackTrace();
								mainFrame.printErrorLog(e);
							}
						}
						//In addition to adding the segment to the db and assigning it to the images,
						//we should also append the segment to the sequence of the word it belongs to.
						//This is done here:
						WordEntry currentWord = words[wordIter];
						//If it was an empty segment straddling a word boundary, ignore it.
						if(text.length()==0 && lastxmin<currentWord.xmax && lastxmax>currentWord.xmax){
							continue;
						}

						double segmentMidpoint = (lastxmin+lastxmax)/2;
						while(segmentMidpoint>currentWord.xmax){
							wordIter++;
							currentWord = words[wordIter];
							if(currentWord==null){
								System.out.println("Breaking out of the loop.");
								continue lineReaderloop;
							}
						}
						//The segment is inside the word now
						if(segmentMidpoint<currentWord.xmin && text.length()>0){
							System.err.println("There was an error!");
						}
						if(text.length()==0){
							text = "0";
							segmentID = 0;
						}
						currentWord.append(getSegmentSpelling(text), segmentID);
					}

					//No matter is was a word or segment, it is now time to assign it to the images
					//Before assigning to the main images, assign to the half-right images on the borders
					//There might be things before the first frame and after the last frame
					int firstActualFrame = (int) Math.floor(lastxmin/frameDuration)+1;
					int lastActualFrame = (int) Math.floor(lastxmax/frameDuration)+1;
					//Now assign the main ones
					for(int i=Math.max(1, firstFrame-1) ; i<=Math.min(lastFrame+1,result.size()); i++){
						//for each frame in range we are assigning the relevant segments (or words)
						int iDigits = String.valueOf(i).length();

						String name = "";
						for(int j=0; j<fileNameLength-iDigits; j++){
							name +="0";
						}
						name += String.valueOf(i);
						if(png){
							name += ".png";
						}
						else{
							name += ".jpg";
						}
						ImageData image = result.get(name);
						if(tier==1 && text.length()>0){
							//assigning the image a word
							try {
								if(i==firstActualFrame && firstActualFrame<firstFrame){ // i.e. i==firstFrame-1 && i==firstActualFrame
									db.assignStartWord(image.id,wordID);
								}
								if(i==lastActualFrame && lastActualFrame>lastFrame){ // i.e. i==lastFrame+1 && i==lastActualFrame
									db.assignEndWord(image.id,wordID);
								}
								else{
									db.assignWord(image.id,wordID);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								mainFrame.printErrorLog(e);
							}
						}
						if(tier==2 && text.length()>0){
							//assigning the image a segment
							try {
								if(i==firstActualFrame && firstActualFrame<firstFrame){ // i.e. i==firstFrame-1 && i==firstActualFrame
									db.assignStartSegment(image.id,segmentID);
								}
								else if(i==lastActualFrame && lastActualFrame>lastFrame){ // i.e. i==lastFrame+1 && i==lastActualFrame
									db.assignEndSegment(image.id,segmentID);
								}
								else if(i>=firstFrame && i<=lastFrame){
									db.assignSegment(image.id,segmentID);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								mainFrame.printErrorLog(e);
							}
						}
					}
				}
			}
			System.out.println("Finished reading lines of the textGrid file");
			//Now that you have scanned the whole file, it is time to update the segment sequences belonging to the words in the database.
			for(int i=0; i<wordsSize; i++){
				WordEntry word = words[i];
				try {
					db.updateSegmentSequences(word);
				} catch (SQLException e) {
					e.printStackTrace();
					mainFrame.printErrorLog(e);
				}
			}
			System.out.println("End of function addTextGridData");
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
			try {
				db.deleteProject(userInput);
			} catch (SQLException e) {
				e.printStackTrace();
				mainFrame.printErrorLog(e);
			}
			
			//We should update the list of tags, experiments, and tracers after we are done.
			mainFrame.searchbox.updateData();
			
			JOptionPane.showMessageDialog(null, "The project and its associated entries were deleted successfully.");
			
		}
	}
}
