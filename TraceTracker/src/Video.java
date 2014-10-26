import java.io.File;


public class Video {
	public String title;
	public File textGridFile;
	private File tracesDirectory;
	private File imagesDirectory;
	private int numberOfImages;
	public boolean	ispng;
	public String subject;
	public String project;
	public String language;
	
	public Video(){
		numberOfImages = 0;
		ispng = false;
	}
	public File getTracesDirectory() {
		return tracesDirectory;
	}
	public void setTracesDirectory(File tracesDirectory) {
		this.tracesDirectory = tracesDirectory;
	}
	public File getImagesDirectory() {
		return imagesDirectory;
	}
	public void setImagesDirectory(File imagesDirectory) {
		//Sets the images directory for this video. Also sets the number of images for this video.
		//For finding the number of images it should be careful that the valid frames are either
		//all jpg or all png.
		this.imagesDirectory = imagesDirectory;
		if(imagesDirectory==null || imagesDirectory.getAbsolutePath().length()==0){
			numberOfImages = 0;
			return;
		}
		File[] theFiles = imagesDirectory.listFiles();
		int pngCounter = 0;
		int jpgCounter = 0;
		for(File f:theFiles){
			if(f.getName().toLowerCase().endsWith("png")){
				pngCounter++;
			}
			else if(f.getName().toLowerCase().endsWith("jpg")){
				jpgCounter++;
			}
		}
		if(pngCounter>jpgCounter){
			ispng = true;
		}
		else{
			ispng = false;
		}
		numberOfImages = Math.max(pngCounter, jpgCounter);
	}
	public int getNumberOfImages(){
		return numberOfImages;
	}
	
	@Override
	public String toString() {
		return title;
	}
}
