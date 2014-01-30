import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;


public class Updater {
	private JFileChooser fc;
	private DBConnector db;
	MainFrame mainFrame;
	
	public Updater(MainFrame mf){
		mainFrame = mf;
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		db = new DBConnector();
		try {
			db.initializeDB();
		} catch (Exception e) {
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
	}
	
	protected String updateDB() {
		int returnVal = fc.showOpenDialog(null);
		if(returnVal!=JFileChooser.APPROVE_OPTION){
			return "noFileChosen";
		}
		File f = fc.getSelectedFile();
		String projectName = f.getName();
		File[] videos = f.listFiles();
		if(videos==null){
			return "The file you selected was not a valid project directory.";
		}
		String projectAddress = f.getAbsolutePath();
		//Make sure this is a new project, at least by making sure the address is new
		try {
			boolean isNew = db.checkProjectIsNew(projectAddress);
			if(!isNew){
				return "This project has already been added to the database. No operation was performed.";
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			mainFrame.printErrorLog(e1);
			return "There was an error while checking whether the project is new.";
		}
		try {
			boolean isNew = db.checkProjectNameIsNew(projectName);
			if(!isNew){
				return "A project with this name already exists in the database. Make sure the project is new\nand try renaming it.";
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
			mainFrame.printErrorLog(e1);
			return "There was some error while checking whether the project is new.";
		}
		for (File video: videos){
			if(!video.getName().contains("_")){
				return "The video folders were not named correctly.";
			}
//			int underscoreIndex = video.getName().indexOf("_");
//			String subjectName = video.getName().substring(0, underscoreIndex);
			String videoName = video.getName();
			String videoAddress = video.getAbsolutePath();
			File framesDirectory = new File(video.getAbsolutePath()+"/frames");
			if(!framesDirectory.exists()){
				continue;
			}
			File[] theFiles = framesDirectory.listFiles();
			for(File file:theFiles){
				if(file.getName().startsWith("frame-")){
					String newName = file.getParentFile().getAbsolutePath()+"/"+file.getName().substring(6);
					boolean success = file.renameTo(new File(newName));
					System.out.println(success);
				}
			}
			theFiles = framesDirectory.listFiles();
			HashMap<String,ImageData> result = new HashMap<String,ImageData>();
			for(File file:theFiles){
				if(file.getName().endsWith("png")){
					//it is an image
					ImageData image = new ImageData();
//					image.project = projectName;
//					image.subject = subjectName;
					image.title = file.getName();
//					image.video = videoName;
					image.address = file.getAbsolutePath();
					result.put(file.getName(),image);
				}
			}
			ArrayList<Trace> traceFiles = new ArrayList<Trace>();
			for(File file: theFiles){
				if(file.getName().contains("png") && file.getName().endsWith("txt")){
					Trace trace = new Trace();
					trace.address = file.getAbsolutePath();
					int pngIndex = file.getName().indexOf("png");
					String imageName = file.getName().substring(0, pngIndex+3);
					String tracerName = file.getName().substring(pngIndex+4,pngIndex+7);
					trace.tracer = tracerName;
					trace.imageName = imageName;
					traceFiles.add(trace);
				}
			}
			try {
				db.addImages(result, traceFiles, projectName, projectAddress, videoName, videoAddress);
			} catch (Exception e) {
				e.printStackTrace();
				mainFrame.printErrorLog(e);
				return "There was some error in the update process.";
			}
		}
		return "The database was updated successfully.";
	}
}
