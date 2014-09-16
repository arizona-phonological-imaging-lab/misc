import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;


public class TextGridReader {
	private Updater updater;
	
	public TextGridReader(Updater u) {
		updater = u;
	}
	
	public void addTextGridData(HashMap<String, ImageData> result, File textGridFile) {
		Scanner scanner = null;
		try {
			scanner = new Scanner(textGridFile,"UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.printErrorLog(e);
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
			MainFrame.printErrorLog(e);
		}
		lineReaderloop:
			while(scanner.hasNextLine()){
				lineCount++;
				int addedProgress = (95*lineCount/numberOfLines)/updater.numberOfVideos;
				if(lineCount%1==0){					
				}
				updater.backgroundTask.updateProgress(updater.progressResult+addedProgress);
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
							wordID = updater.db.addWord(text);
						} catch (SQLException e) {
							e.printStackTrace();
							MainFrame.printErrorLog(e);
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
								segmentID = updater.db.addSegment(text);
							} catch (SQLException e) {
								e.printStackTrace();
								MainFrame.printErrorLog(e);
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
						if(updater.png){
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
									updater.db.assignStartWord(image.id,wordID);
								}
								if(i==lastActualFrame && lastActualFrame>lastFrame){ // i.e. i==lastFrame+1 && i==lastActualFrame
									updater.db.assignEndWord(image.id,wordID);
								}
								else{
									updater.db.assignWord(image.id,wordID);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								MainFrame.printErrorLog(e);
							}
						}
						if(tier==2 && text.length()>0){
							//assigning the image a segment
							try {
								if(i==firstActualFrame && firstActualFrame<firstFrame){ // i.e. i==firstFrame-1 && i==firstActualFrame
									updater.db.assignStartSegment(image.id,segmentID);
								}
								else if(i==lastActualFrame && lastActualFrame>lastFrame){ // i.e. i==lastFrame+1 && i==lastActualFrame
									updater.db.assignEndSegment(image.id,segmentID);
								}
								else if(i>=firstFrame && i<=lastFrame){
									updater.db.assignSegment(image.id,segmentID);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								MainFrame.printErrorLog(e);
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
				updater.db.updateSegmentSequences(word);
			} catch (SQLException e) {
				e.printStackTrace();
				MainFrame.printErrorLog(e);
			}
		}
		System.out.println("End of function addTextGridData");
	}

	public static String getSegmentSpelling(String detailedSpelling){
		detailedSpelling = detailedSpelling.replace("neutral", "neut");
		detailedSpelling = detailedSpelling.trim();
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
}
