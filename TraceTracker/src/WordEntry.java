
public class WordEntry {
	String word;
	int id;
	int	firstFrame;
	int lastFrame;
	private String segments;
	private String segmentIDs;
	double xmin;
	double xmax;
	
	public WordEntry(String w, int f, int l, double min, double max, int wordID){
		word =w;
		firstFrame = f;
		lastFrame = l;
		id = wordID;
		segments = "";
		segmentIDs = "";
		xmin = min;
		xmax = max;
	}

	public void append(String text, int segmentID) {
		segments = segments + text + " ";
		segmentIDs = segmentIDs + segmentID + " ";
	}
	
	public String getSegments(){
		return segments.trim();
	}
	public String getSegmentIDs(){
		return segmentIDs.trim();
	}
}
