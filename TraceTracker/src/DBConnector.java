import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.JOptionPane;

public class DBConnector {
	Connection conn;
	public void initializeDB() throws Exception {
		Class.forName("org.sqlite.JDBC");
		Properties props = new Properties();
		props.setProperty("dontTrackOpenResources","true");
		conn = DriverManager.getConnection("jdbc:sqlite:traceFiles.db",props);
		PreparedStatement pragmaPrep = conn.prepareStatement("PRAGMA synchronous=OFF;");
		pragmaPrep.execute();
		PreparedStatement pragmaPrep2 = conn.prepareStatement("PRAGMA journal_mode=OFF;");
		pragmaPrep2.execute();
		PreparedStatement pragmaPrep3 = conn.prepareStatement("PRAGMA temp_store=2;");
		pragmaPrep3.execute();
		pragmaPrep.close();
		pragmaPrep2.close();
		pragmaPrep3.close();
	}

	synchronized public ArrayList<ImageData> executeQuery(SearchBox sb) throws SQLException{
		long t1 = System.currentTimeMillis();
		if(conn.isClosed()){
			Properties props = new Properties();
			props.setProperty("dontTrackOpenResources","true");
			conn = DriverManager.getConnection("jdbc:sqlite:traceFiles.db",props);
		}
		ArrayList<ImageData> result = new ArrayList<ImageData>();
		String titleEntered = sb.imageTitleTextField.getText().trim(); 
		String projectEntered = (String) sb.projectCombo.getSelectedItem();
		String tracerEntered = (String) sb.tracerCombo.getSelectedItem();
		String languageEntered = (String) sb.languageCombo.getSelectedItem();
		String experimentEntered = (String) sb.experimentCombo.getSelectedItem();
		int autotraceEntered = sb.autotraceCombo.getSelectedIndex();
		int corruptEntered = sb.corruptCheckbox.isSelected()? 1 : 0;
		int howManyTracersEntered = sb.howManyTracersCombo.getSelectedIndex();
		String tag = (String) sb.tagsCombo.getSelectedItem();
		String wordEntered = sb.wordTextField.getText().trim();
		String segmentEntered = sb.segmentTextField.getText().trim();
		String targetSegment = findTargetSegment(segmentEntered);
//		System.out.println(targetSegment);
		if(segmentEntered.length()>0 && targetSegment.length()==0){
			JOptionPane.showMessageDialog(null, "Segment sequence not valid. Use brackets to indicate target segment.","Error",JOptionPane.ERROR_MESSAGE);
			return result;
		}
		String representativeFrame = (String) sb.representativeFramesCombo.getSelectedItem();
		Integer marginSizeBefore = sb.getMarginSize("before");
		Integer marginSizeAfter= sb.getMarginSize("after");
		
		String query = "SELECT image.id AS theid, image.title AS image_title, video.title AS video_title, video.subject AS subject, project.title AS project_title, image.address AS address, image.segment_id AS segment_id, image.video_id AS video_id"
		+" FROM image JOIN video ON image.video_id=video.id JOIN project ON project.id=video.project_id ";
		
		//Do a preliminary segment filtering here to speed up the segment search (part 1)
		if(segmentEntered.length()>0){
			query += "JOIN segment ON image.segment_id=segment.id ";
		}
		
		if(tag != null && tag.length()>0){
			query += "JOIN tag ON image.id=tag.image_id "; 
		}
		
		if(experimentEntered != null && experimentEntered.length()>0){
			query += "JOIN experiment ON image.id=experiment.image_id ";
		}
		
		query +="WHERE 1=1 "; //This last thing after "where" is a dummy condition
		
		if(titleEntered != null && titleEntered.length()>0){
			query += "AND image.title LIKE '%"+titleEntered+"%' "; 
		}
		if(projectEntered != null && projectEntered.length()>0){
			query += "AND project.title = '"+projectEntered+"' ";
		}
		if(tracerEntered != null && tracerEntered.length()>0){
			query += "AND theid IN (SELECT image.id FROM image JOIN trace ON image.id=trace.image_id JOIN tracer ON trace.tracer_id=tracer.id WHERE tracer.first_name='"+tracerEntered+"') ";
		}
		if(autotraceEntered==1){
			query += "AND image.autotraced=1 ";
		}
		else if(autotraceEntered==2){
			query += "AND (image.autotraced is null or image.autotraced=0)";
		}
		if(languageEntered!=null && languageEntered.length()!=0 && !"(Not Entered)".equals(languageEntered)){
			query += "AND project.language = '"+languageEntered+"' ";
		}
		else if("(Not Entered)".equals(languageEntered)){
			query += "AND (project.language = '' OR project.language is null) ";
		}
		if(experimentEntered != null && experimentEntered.length()>0){
			query += "AND experiment.content='"+experimentEntered+"' ";
		}
		if(corruptEntered==1){
			query += "AND image.is_bad = 1 ";
		}
		if(howManyTracersEntered>0 && howManyTracersEntered<4){
			int actualRequiredNumber = howManyTracersEntered-1;
			query += "AND image.trace_count="+actualRequiredNumber+" ";
		}
		else if(howManyTracersEntered==4){
			query += "AND image.trace_count>2 ";
		}
		if(tag != null && tag.length()>0){
			query += "AND tag.content='"+tag+"' "; 
		}
		if(wordEntered != null && wordEntered.length()>0){
			query += "AND theid IN (SELECT image.id FROM image JOIN word ON image.word_id=word.id WHERE word.spelling='"+wordEntered+"') "; 
		}
		//Do a preliminary segment filtering here to speed up the segment search (part 2)
		if(segmentEntered.length()>0){
			query += "AND segment.spelling='"+targetSegment+"' ";
		}
		query += "ORDER BY image.sorting_code ";
		//If we dont't need to fetch more than what we will show, we can limit the query.
		boolean weAreLimiting = false;
		if( segmentEntered.length()==0 ){
			weAreLimiting = true;
			query += "LIMIT "+MainFrame.fetchLimit;
		}
		query += ";";
//		System.out.println(query);
		Statement stat = conn.createStatement();
		long t_beforeQuery = System.currentTimeMillis();
		ResultSet rs = stat.executeQuery(query);
		long t_med = System.currentTimeMillis();
//		System.out.println("Query time: "+(t_med-t_beforeQuery));
		HashSet<Integer> segmentIDs = null;
		if(segmentEntered.length()>0){
			segmentIDs = findSegmentIDs(segmentEntered);
		}
		int lastVideoID = 0;
		ImageData image = new ImageData();
		int prevSegId = -1;
		while(rs.next()){
			image = new ImageData();
			image.id = rs.getString("theid");
			image.title = rs.getString("image_title");
			image.video = rs.getString("video_title");
			image.subject = rs.getString("subject");
			image.project = rs.getString("project_title");
			image.address = rs.getString("address");
			image.video_id = rs.getInt("video_id");
			String segment_id = "0";
			if(segmentIDs!=null){
				//see if it belongs to any of the segments we found. Otherwise we don't want it.
				segment_id = rs.getString("segment_id");
				if (segment_id==null || segment_id.length()<1 || "0".equals(segment_id)){
					segment_id = "-1";
				}
				int segid = Integer.valueOf(segment_id);
				image.segment_id = segid;
				if(!segmentIDs.contains(segid)){
					continue;
				}
			}
			if(image.segment_id!=prevSegId && marginSizeAfter==0 && prevSegId!=-1){
				result.get(result.size()-1).isLastInSet = true;
			}
			prevSegId = image.segment_id;
			result.add(image);
		}
		//If we are limiting, we want to know the actual number of results too.
		if(weAreLimiting){
			int orderIndex = query.indexOf("ORDER BY");
			String countQuery = "SELECT COUNT(*) FROM ("+query.substring(0,orderIndex-1)+");";
			Statement countStat = conn.createStatement();
			ResultSet rsCount = countStat.executeQuery(countQuery);
			rsCount.next();
			int count = rsCount.getInt(1);
			MainFrame.resultSize = count;
			countStat.close();
		}
		//
		
		ArrayList<ImageData> refinedResult = new ArrayList<ImageData>();
		ArrayList<ImageData> currentSeries = new ArrayList<ImageData>();
		int lastSegmentId = -10;
		//For showing only the representative frames:
		if(!"".equals(representativeFrame)){
			for(int j=0; j<result.size(); j++){
				ImageData i = result.get(j);
				if(i.segment_id==lastSegmentId){
					currentSeries.add(i);
				}
				//A frame belonging to a new set:
				else{
					if(currentSeries.size()==1){
						refinedResult.add(currentSeries.get(0));
					}
					else if(currentSeries.size()>0){
						if("Middle".equals(representativeFrame)){
							refinedResult.add(currentSeries.get(currentSeries.size()/2));
						}
						else if("Second".equals(representativeFrame)){
							refinedResult.add(currentSeries.get(1));
						}
						else if("Second to last".equals(representativeFrame)){
							refinedResult.add(currentSeries.get(currentSeries.size()-2));
						}
						else if("Initial".equals(representativeFrame)){
							refinedResult.add(currentSeries.get(0));
						}
						else if("Final".equals(representativeFrame)){
							refinedResult.add(currentSeries.get(currentSeries.size()-1));
						}
					}
					currentSeries = new ArrayList<ImageData>();
					currentSeries.add(i);
				}	
				lastSegmentId = i.segment_id;
			}
			stat.close();
//			System.out.println(refinedResult.size()+"\t"+currentSeries.size());
			result = refinedResult;
		}
		refinedResult = new ArrayList<ImageData>();
		String lastTitle = null;
		//We may want the peripheral frames too:
		if( segmentEntered.length()>0 && (marginSizeBefore>0 || marginSizeAfter>0) ){
			int lastSegmentID = -1;
			for(int i=0; i<result.size(); i++){
				ImageData theImage = result.get(i);
				if(theImage.segment_id != lastSegmentID){
					//We are entering a new frame set (frames belonging to a segment)
					//First add the ones belonging to the last segment
					if(lastSegmentID!=-1){
						addPeripheralImageToResult(refinedResult,lastVideoID, lastTitle,marginSizeAfter);
						refinedResult.get(refinedResult.size()-1).isLastInSet = true;
					}
					//Then add the ones belonging to the new segment
					addPeripheralImageToResult(refinedResult,theImage.video_id, theImage.title,-marginSizeBefore);
				}
				lastSegmentID = theImage.segment_id;
				lastTitle = theImage.title;
				lastVideoID = theImage.video_id;
				refinedResult.add(theImage);
			}
			result = refinedResult;
		}
		//Add the ending peripherals for the last frame set
		if(marginSizeAfter!=null && marginSizeAfter>0 && result.size()>0){			
			addPeripheralImageToResult(result,lastVideoID, lastTitle,marginSizeAfter);
		}
		stat.close();
		long t2 = System.currentTimeMillis();
//		System.out.println("Time: "+(t2-t1));
		if(!weAreLimiting){
			MainFrame.resultSize = result.size();
		}
		return result;
	}

	private String findTargetSegment(String input) {
		//Gets an input segment search string like "$ a [r] ch m" and reutrns "r" as the segment that is the main target
		//of the search
		
		if(input.matches(".*\\[\\w+\\].*")){
			String result = input.replaceAll(".*\\[(.*)\\].*", "$1");
			return result;
		}
		else{
			if(input.matches("^(\\W)*(\\w)+(\\W)*$")){
				return input.replaceAll("\\W", "");
			}
			return "";
		}
	}

	private void addPeripheralImageToResult(ArrayList<ImageData> result, int video_id, String fullTitle, int domain) throws SQLException {
		if(domain==0){
			return;
		}
		Statement stat = conn.createStatement();
		String query = "SELECT image.id AS theid, image.title AS image_title, video.title AS video_title, video.subject AS subject, project.title AS project_title, image.address AS address ";
		query += " FROM image JOIN video ON image.video_id=video.id JOIN project ON project.id=video.project_id WHERE video.id="+video_id+" AND (";
		int title = Integer.valueOf(fullTitle.substring(0,fullTitle.length()-4));
		String suffix = fullTitle.substring(fullTitle.length()-4);
		if(domain>0){
			for(int i=title+1;i<=title+domain; i++){
				Statement stat2 = conn.createStatement();
				int iDigits = String.valueOf(i).length();
				String prefix = "";
				for(int j=0; j<fullTitle.length()-4-iDigits; j++){
					prefix +="0";
				}
				String query2 = "SELECT image.id FROM image WHERE image.title='"+prefix+i+suffix+"' AND image.video_id="+video_id+" ORDER BY image.title ASC;";
				ResultSet rs2 = stat2.executeQuery(query2);
				int id= 0;
				if(rs2.next()){
					id = rs2.getInt(1);
				}
				stat2.close();
				query += " image.id="+id+" OR";
			}
		}
		else{
			for(int i=title+domain;i<title; i++){
				Statement stat2 = conn.createStatement();
				int iDigits = String.valueOf(i).length();
				String prefix = "";
				for(int j=0; j<fullTitle.length()-4-iDigits; j++){
					prefix +="0";
				}
				String query2 = "SELECT image.id FROM image WHERE image.title='"+prefix+i+suffix+"' AND image.video_id="+video_id+" ORDER BY image.title ASC;";
				ResultSet rs2 = stat2.executeQuery(query2);
				int id= 0;
				if(rs2.next()){
					id = rs2.getInt(1);
				}
				stat2.close();
				query += " image.id="+id+" OR";
			}
		}
		//Now we have a query that looks for the desired image IDs.
		query = query.substring(0, query.length()-3)+") ORDER BY image.title ASC;";
		ResultSet rs = stat.executeQuery(query);
		while(rs.next()){
			ImageData image = new ImageData();
			image.id = rs.getString("theid");
			image.title = rs.getString("image_title");
			image.video = rs.getString("video_title");
			image.subject = rs.getString("subject");
			image.project = rs.getString("project_title");
			image.address = rs.getString("address");
			image.isPeripheral = true;
			result.add(image);
		}
		stat.close();
	}

	private HashSet<Integer> findSegmentIDs(String segmentEntered) throws SQLException {
		Statement stat = conn.createStatement();
		//These two lines are here because initially the system worked with parantheses instead of the brackets
		segmentEntered = segmentEntered.replace("[", "(");
		segmentEntered = segmentEntered.replace("]", ")");
		
		//First find the words that have the desired sequence
		String searchTerm = segmentEntered;
		boolean hasPrefix = false;
		boolean hasSuffix = false;
		if(searchTerm.startsWith("$")){
			searchTerm = searchTerm.substring(1);
		}
		else{
			hasPrefix = true;
		}
		if(searchTerm.endsWith("$")){
			searchTerm = searchTerm.substring(0,searchTerm.length()-1);
		}
		else{
			hasSuffix = true;
		}
		searchTerm = searchTerm.replace("(", "");
		searchTerm = searchTerm.replace(")", "");
		searchTerm = searchTerm.trim();
		String query1 = "";
		if(!hasPrefix && !hasSuffix){
			query1 = "SELECT segment_sequence,segment_id_sequence FROM word WHERE segment_sequence = '"+searchTerm+"'";
		}
		else if(!hasPrefix){
			query1 = "SELECT segment_sequence,segment_id_sequence FROM word WHERE segment_sequence LIKE '"+searchTerm+" %' OR segment_sequence LIKE '"+searchTerm+"'";
		}
		else if(!hasSuffix){
			query1 = "SELECT segment_sequence,segment_id_sequence FROM word WHERE segment_sequence LIKE '% "+searchTerm+"' OR segment_sequence LIKE '"+searchTerm+"'";
		}
		else{
			//If it had to be like %searchTerm%:
			query1 = "SELECT segment_sequence,segment_id_sequence FROM word WHERE segment_sequence LIKE '% "+searchTerm+" %' OR segment_sequence LIKE '% "+searchTerm+"' or segment_sequence LIKE '"+searchTerm+" %' or segment_sequence LIKE '"+searchTerm+"'";
		}
//		System.out.println("***"+query1);
		ResultSet rs = stat.executeQuery(query1);
		//For each word that has such a pattern:
		HashSet<Integer> resultIDs = new HashSet<Integer>();
		while(rs.next()){
			String segments = rs.getString(1);
			String ids = rs.getString(2);
			//Now let's find the target segment
			//The search string should be divided into three groups with round brackets
			String st2 = segmentEntered;
			if(st2.startsWith("$")){
				st2 = "^"+st2.substring(1);
			}
			if(!st2.contains("(")){
				st2 = st2.replaceAll("(\\w+)", "\\($1\\)");
			}
			int openBracketIndex = st2.indexOf("(");
			int closedBracketIndex = st2.indexOf(")");
			st2 = "("+st2.substring(0,openBracketIndex)+")"+st2.substring(openBracketIndex,closedBracketIndex+1)+"("+st2.substring(closedBracketIndex+1)+")";
			// st2 = ($a )(r)( o) 
			st2 = st2.replaceAll("\\%", "\\.\\+");
			//Replace "(^ )" with "^"
			st2 = st2.replaceAll("\\(\\^ \\)", "\\^()");
			//Replace "( $)" with "$"
			st2 = st2.replaceAll("\\(\\ $\\)", "\\()$");
			segments = segments.replaceAll(st2, "$1($2)$3");
			// segments = m a (r) o n 0 z
			//Since we don't want "sh" when we search for "s":
			//(c) m (c)h ea r V		$1:"(c) m "		$2:"("		$3:"c"		$4:")"		$5:"h ea r V" 
			segments = segments.replaceAll("(.*)(\\()(\\w)(\\))(\\w.*)", "$1$3$5");
			//Similarly:
			segments = segments.replaceAll("(.*\\w)(\\()(\\w)(\\))(.*)", "$1$3$5");
			if(!segments.matches(".*\\(.+\\).*")){
				continue;
			}
			//
			
			String[] segmentsList = segments.split(" ");
			String[] idsList = ids.split(" ");
			for(int i=0; i< segmentsList.length; i++){
				if(segmentsList[i].startsWith("(")){
					//Add the id of this segment as one of the target segments to the result list
					resultIDs.add(Integer.valueOf(idsList[i]));
				}
			}
		}
		stat.close();
		return resultIDs;
	}

	public void closeDB() throws SQLException{
		conn.close();
	}

	public String[] getProjectsList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM project;";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> titles = new ArrayList<String>();
		while(rs.next()){
			titles.add(rs.getString(1));
		}
		String[] result = new String[titles.size()+1];
		result[0] = "";
		int i=1;
		for(String s:titles){
			result[i] = s;
			i++;
		}
		stat.close();
		return result;
	}
	public String[] getLanguagesList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT DISTINCT language FROM project;";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> titles = new ArrayList<String>();
		while(rs.next()){
			titles.add(rs.getString(1));
		}
		String[] result = new String[titles.size()+1];
		result[0] = "";
		int i=1;
		for(String s:titles){
			if(s==null || s.length()==0){
				s = "(Not Entered)";
			}
			result[i] = s;
			i++;
		}
		stat.close();
		return result;
	}

	public String[] getTracersList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT first_name FROM tracer;";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> titles = new ArrayList<String>();
		while(rs.next()){
			titles.add(rs.getString(1));
		}
		String[] result = new String[titles.size()+1];
		result[0] = "";
		int i=1;
		for(String s:titles){
			result[i] = s;
			i++;
		}
		stat.close();
		return result;
	}
	
	public String[] getExperimentsList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT DISTINCT content FROM experiment;";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> titles = new ArrayList<String>();
		while(rs.next()){
			titles.add(rs.getString(1));
		}
		String[] result = new String[titles.size()+1];
		result[0] = "";
		int i=1;
		for(String s:titles){
			result[i] = s;
			i++;
		}
		stat.close();
		return result;
	}

	public String getTracers(String id) throws SQLException{
		Statement stat = conn.createStatement();
		String query = "SELECT first_name FROM trace JOIN image ON trace.image_id=image.id JOIN tracer ON trace.tracer_id=tracer.id WHERE image.id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String result = "";
		while(rs.next()){
			result += rs.getString(1)+", ";
		}
		if(result.length()>2){			
			result = result.substring(0, result.length()-2);
		}
		stat.close();
		return result;
	}
	
	public String getExperiments(String id) throws SQLException{
		Statement stat = conn.createStatement();
		String query = "SELECT experiment.content FROM experiment WHERE image_id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String result = "";
		while(rs.next()){
			result += rs.getString(1)+", ";
		}
		if(result.length()>2){			
			result = result.substring(0, result.length()-2);
		}
		stat.close();
		return result;
	}
	
	public void addImages(HashMap<String, ImageData> images, ArrayList<Trace> traces, String projectName, String projectAddress, String videoName, String videoAddress, String language, String subj, boolean updateMode) throws Exception{
		Statement stat = conn.createStatement();
		int projectID = -1;
		String query = "SELECT id FROM project WHERE title='"+projectName+"';";
		ResultSet rs = stat.executeQuery(query);
		if(!rs.next() && !updateMode){
			//We should add such a project if it doesn't exist
			String query2 = "INSERT INTO project(title, folder_address, language) VALUES('"+projectName+"','"+projectAddress+"','"+language+"');";
			Statement stat2 = conn.createStatement();
			stat2.executeUpdate(query2);
			ResultSet rs2 = stat2.getGeneratedKeys();
			rs2.next();
			projectID = rs2.getInt(1);
			stat2.close();
		}
		else{
			projectID = rs.getInt(1);
		}
		//The project is taken care of. Now add the video.
		String subject = subj; 
		Statement stat3 = conn.createStatement();
		int videoID = -1;
		if(updateMode){
			String querya = "SELECT id FROM video WHERE project_id="+projectID+" AND title='"+videoName+"';";
			ResultSet rsa = stat3.executeQuery(querya);
			if(rsa.next()){
				videoID = rsa.getInt(1);
			}
		}
		if(videoID==-1){
			String query3 = "INSERT INTO video(title,subject,project_id,folder_address) VALUES('"+videoName+"','"+subject+"',"+projectID+",'"+videoAddress+"');";
			stat3.executeUpdate(query3);
			ResultSet rs4 = stat3.getGeneratedKeys();
			rs4.next();
			videoID = rs4.getInt(1);
		}
		//The video is taken care of. Now add the images.
		Statement stat4 = conn.createStatement();
		for(ImageData image: images.values()){
			image.id = "-1";
			if(updateMode){
				String querya = "SELECT id FROM image WHERE video_id="+videoID+" AND title='"+image.title+"';";
				ResultSet rsa = stat3.executeQuery(querya);
				if(rsa.next()){
					image.id = String.valueOf(rsa.getInt(1));
				}
			}
			if("-1".equals(image.id)){
				String query4 = "INSERT INTO image(video_id, title, address, sorting_code, trace_count) VALUES("+videoID+",'"+image.title+"','"+image.address+"','"+(projectName+videoName+image.title)+"', 0);";
				stat4.executeUpdate(query4);
				//Save the db id of the image we just added. We need it when associating trace files with images in the next loop
				ResultSet rs5 = stat4.getGeneratedKeys();
				while(rs5.next()){
					image.id = String.valueOf(rs5.getInt(1));
				}
			}
		}

		//The images are added. Now add the trace info.
		String[] tracerNames = getTracersList();
		//First add the new tracers to the db
		for(Trace trace: traces){
			//Does this tracer even exist in TracerNames?
			boolean found = false;
			for(int j=0; j<tracerNames.length; j++){
				if(tracerNames[j].equals(trace.tracer)){
					found = true;
					break;
				}
			}
			if(!found){
				//So we should add this new tracer to the db ourselves:
				Statement stat5 = conn.createStatement();
				String query6 = "INSERT INTO tracer(first_name) VALUES('"+trace.tracer+"');";
				stat5.executeUpdate(query6);
				ResultSet rs7 = stat5.getGeneratedKeys();
				rs7.next();
				//Append the tracer name:
				String[] newTracerList = new String[tracerNames.length+1];
				for(int i=0; i< tracerNames.length; i++){
					newTracerList[i] = tracerNames[i];
				}
				newTracerList[tracerNames.length] = trace.tracer;
				tracerNames = newTracerList;
				stat5.close();
			}
		}
		
		//Because db queries are time-consuming. we batch trace files of the same tracer together, so that we
		//we won't need to check the id of the tracer every time.
		for(int i=1; i<tracerNames.length; i++){
			String query5 = "SELECT id FROM tracer WHERE first_name='"+tracerNames[i]+"';";
			ResultSet rs6 = stat.executeQuery(query5);
			int tracerID = -1;
			tracerID = rs6.getInt(1);
			for(Trace trace:traces){
				if(trace.tracer.equals(tracerNames[i])){
					ImageData image = images.get(trace.imageName);
					if(image==null){
						MainFrame.printErrorLog("Unmatched trace file: "+trace.imageName);
						continue;
					}
					boolean traceFileExists = false;
					if(updateMode){
						//We might not need to add this trace file if we are in update mode. Let's see if it is new.
						String querya = "SELECT id FROM trace WHERE image_id='"+image.id+"' AND tracer_id='"+tracerID+"';";
						ResultSet rsa = stat.executeQuery(querya);
						if(rsa.next()){
							traceFileExists = true;
						}
					}
					if(!traceFileExists){
						String query7 = "INSERT INTO trace(address, tracer_id, image_id) VALUES('"+trace.address+"',"+tracerID+","+image.id+");";
						stat.executeUpdate(query7);
						//Check "autotraced" if the tracer was network
						if(tracerNames[i].contains("network")){
							String query8 = "UPDATE image SET autotraced=1 WHERE id="+image.id+";";
							stat.executeUpdate(query8);
						}
						//Also increment the number of traces for the corresponding image
						String query8 = "SELECT trace_count FROM image WHERE id='"+image.id+"';";
						ResultSet rsb = stat.executeQuery(query8);
						rsb.next();
						int traceCount = rsb.getInt(1);
						traceCount++;
						String query9 = "UPDATE image SET trace_count='"+traceCount+"' WHERE id='"+image.id+"'";
						stat.execute(query9);
					}
				}
			}
		}
		stat3.close();
		stat4.close();
		stat.close();
	}

	public boolean checkProjectIsNew(String projectAddress) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM project where folder_address='"+projectAddress+"';";
		ResultSet rs = stat.executeQuery(query);
		if(rs.next()){
			stat.close();
			return false;
		}
		else{		
			stat.close();
			return true;
		}
	}

	public boolean checkProjectNameIsNew(String projectName) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM project where title='"+projectName+"';";
		ResultSet rs = stat.executeQuery(query);
		if(rs.next()){
			stat.close();
			return false;
		}
		else{			
			stat.close();
			return true;
		}
	}

	public ArrayList<String> getTraceAddresses(ImageData image) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT trace.address AS address FROM image JOIN trace ON image.id=trace.image_id where image.id="+image.id+";";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> addresses = new ArrayList<String>();
		while(rs.next()){
			String address = rs.getString("address");
			if(address!=null){
				addresses.add(address);
			}
		}
		stat.close();
		return addresses;
	}

	public int tagImages(Collection<ImageData> images, String tagContent, boolean isExperiment) throws SQLException {
		Statement stat = conn.createStatement();
		int counter = 0;
		for(ImageData image: images){
			//Check if this image doesn't already have that tag
			Statement stat2 = conn.createStatement();
			String query;
			query = "SELECT id FROM";
			if(isExperiment){
				query += " experiment ";
			}
			else{
				query += " tag ";
			}
			query+= "WHERE image_id="+image.id+" AND content='"+tagContent+"';";
			ResultSet rs = stat2.executeQuery(query);
			if(rs.next()){
				continue;
			}
			//Now insert the tag
			String update = "INSERT INTO";
			if(isExperiment){
				update += " experiment";
			}
			else{
				update += " tag";
			}
			update += "(image_id,content) VALUES("+image.id+",'"+tagContent+"');";
			stat.addBatch(update);
			counter++;
		}
		stat.executeBatch();
		stat.close();
		return counter;
	}

	public void untagImages(Collection<ImageData> images, String tagContent, boolean isExperiment) throws SQLException {
		Statement stat = conn.createStatement();
		for(ImageData image: images){
			//Check if this image doesn't already have that tag
			String update = "DELETE FROM";
			if (isExperiment){
				update += " experiment ";
			}
			else{
				update += " tag ";
			}
			update += "WHERE image_id="+image.id+" AND content='"+tagContent+"';";
			stat.addBatch(update);
		}
		stat.close();
		stat.executeBatch();
	}

	public String getTags(String id) throws Exception{
		Statement stat = conn.createStatement();
		String query = "SELECT content FROM tag JOIN image ON tag.image_id=image.id WHERE image.id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String result = "";
		while(rs.next()){
			result += rs.getString(1)+", ";
		}
		if(result.length()>2){			
			result = result.substring(0, result.length()-2);
		}
		stat.close();
		return result;
	}
	
	public String[] getTagsList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT DISTINCT content FROM tag;";
		ResultSet rs = stat.executeQuery(query);
		ArrayList<String> titles = new ArrayList<String>();
		while(rs.next()){
			titles.add(rs.getString(1));
		}
		String[] result = new String[titles.size()+1];
		result[0] = "";
		int i=1;
		for(String s:titles){
			result[i] = s;
			i++;
		}
		stat.close();
		return result;
	}

	public String getEnvironment(String id) throws SQLException {
		Statement stat = conn.createStatement();
		String otherQuery = "SELECT start_segment_id,end_segment_id FROM image WHERE id="+id+";";
		ResultSet otherRS = stat.executeQuery(otherQuery);
		otherRS.next();
		String startSegmentID = String.valueOf(otherRS.getInt(1));
		String endSegmentID = String.valueOf(otherRS.getInt(2));
		String query = "SELECT word.segment_sequence,word.segment_id_sequence,segment.id FROM word JOIN image ON word.id=image.word_id JOIN segment ON segment.id=image.segment_id  WHERE image.id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String segmentSequence;
		String result ="";
		String idSequence = "";
		String[] ids = {""};	//Just so that it's not null
		String[] segments = {""};
		int segmentID = -1;
		if(rs.next()){
			segmentSequence = rs.getString(1);
			idSequence = rs.getString(2);
			segmentID = rs.getInt(3);
			ids = idSequence.split(" ");
			segments = segmentSequence.split(" ");
		}
		for(int i=0; i<ids.length; i++){
			if(ids[i].equals(String.valueOf(segmentID))){
				if("brackets".equals(MainFrame.targetSegmentDisplayMode)){
					segments[i] = "["+segments[i]+"]";
				}
				else{
					segments[i] = "<font color=\"#ff0022\">"+segments[i]+"</font>";
				}
			}
			else if(ids[i].equals(startSegmentID) && !"".equals(startSegmentID) && !"0".equals(startSegmentID)){
				if("brackets".equals(MainFrame.targetSegmentDisplayMode)){
					segments[i] = "("+segments[i]+")";
				}
				else{
					segments[i] = "<font color=\"#aa55ff\">"+segments[i]+"</font>";
				}
			}
			else if(ids[i].equals(endSegmentID) && !"".equals(endSegmentID) && !"0".equals(endSegmentID)){
				if("brackets".equals(MainFrame.targetSegmentDisplayMode)){
					segments[i] = "("+segments[i]+")";					
				}
				else{
					segments[i] = "<font color=\"#aa55ff\">"+segments[i]+"</font>";
				}
			}
			result += segments[i]+" ";
		}
		stat.close();
		if("brackets".equals(MainFrame.targetSegmentDisplayMode)){
			return result;
		}
		else{
			return "<html>"+result+"</html>";
		}
	}

	public int addWord(String word) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "INSERT INTO word(spelling) VALUES('"+word+"');";
		stat.executeUpdate(update);
		ResultSet rs = stat.getGeneratedKeys();
		int id = -1;
		if(rs.next()){
			 id = rs.getInt(1);
		}
		stat.close();
		return id;
	}

	public void assignWord(String id, int wordID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET word_id="+wordID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}

	public String getWord(String id) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT word.spelling FROM word JOIN image ON word.id=image.word_id WHERE image.id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String result = "";
		if(rs.next()){
			result = rs.getString(1);
		}
		stat.close();
		return result;
	}

	public int addSegment(String text) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "INSERT INTO segment(spelling, detailed_spelling) VALUES('"+TextGridReader.getSegmentSpelling(text)+"','"+text+"');";
		stat.executeUpdate(update);
		ResultSet rs = stat.getGeneratedKeys();
		int id = -1;
		if(rs.next()){
			 id = rs.getInt(1);
		}
		stat.close();
		return id;
	}

	public void assignSegment(String id, int segmentID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET segment_id="+segmentID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}

	public void updateSegmentSequences(WordEntry word) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE word SET segment_sequence='"+word.getSegments()+"', segment_id_sequence='"+word.getSegmentIDs()+"' WHERE id="+word.id+";";
		stat.executeUpdate(update);
		stat.close();
	}

	public void assignStartWord(String id, int wordID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET start_word_id="+wordID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}
	
	public void assignEndWord(String id, int wordID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET end_word_id="+wordID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}
	
	public void assignStartSegment(String id, int segmentID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET start_segment_id="+segmentID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}
	
	public void assignEndSegment(String id, int segmentID) throws SQLException {
		Statement stat = conn.createStatement();
		String update = "UPDATE image SET end_segment_id="+segmentID+" WHERE id="+id+";";
		stat.executeUpdate(update);
		stat.close();
	}

	public void deleteProject(String title) throws SQLException{
		Statement stat = conn.createStatement();
		String deleteTags = "DELETE FROM tag WHERE id IN (SELECT tag.id FROM tag JOIN image ON tag.image_id=image.id JOIN video ON video.id=image.video_id JOIN project ON project.id=video.project_id WHERE project.title='"+title+"');";
		String deleteExps =  "DELETE FROM experiment WHERE id IN (SELECT experiment.id FROM experiment JOIN image ON experiment.image_id=image.id JOIN video ON video.id=image.video_id JOIN project ON project.id=video.project_id WHERE project.title='"+title+"');";
		String deleteWords =  "DELETE FROM word WHERE id IN (SELECT word.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN word ON word.id=image.word_id WHERE project.title='"+title+"');";
		String deleteWordsStart =  "DELETE FROM word WHERE id IN (SELECT word.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN word ON word.id=image.start_word_id WHERE project.title='"+title+"');";
		String deleteWordsEnd =  "DELETE FROM word WHERE id IN (SELECT word.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN word ON word.id=image.end_word_id WHERE project.title='"+title+"');";
		String deleteSegments =  "DELETE FROM segment WHERE id IN (SELECT segment.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN segment ON segment.id=image.segment_id WHERE project.title='"+title+"');";
		String deleteSegmentsStart =  "DELETE FROM segment WHERE id IN (SELECT segment.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN segment ON segment.id=image.start_segment_id WHERE project.title='"+title+"');";
		String deleteSegmentsEnd =  "DELETE FROM segment WHERE id IN (SELECT segment.id FROM project JOIN video ON project.id=video.project_id JOIN image ON video.id=image.video_id JOIN segment ON segment.id=image.end_segment_id WHERE project.title='"+title+"');";
		String deleteTraces =  "DELETE FROM trace WHERE id IN (SELECT trace.id FROM trace JOIN image ON trace.image_id=image.id JOIN video ON video.id=image.video_id JOIN project ON project.id=video.project_id WHERE project.title='"+title+"');";
		String deleteTracers =  "DELETE FROM tracer WHERE tracer.id NOT IN (SELECT tracer_id FROM trace);";
		String deleteImages = "DELETE FROM image WHERE id IN (SELECT image.id FROM image JOIN video ON video.id=image.video_id JOIN project ON project.id=video.project_id WHERE project.title='"+title+"');";
		String deleteVideos = "DELETE FROM video WHERE id IN (SELECT video.id FROM video JOIN project ON project.id=video.project_id WHERE project.title='"+title+"');";
		String deleteProject= "DELETE FROM project WHERE project.title='"+title+"';";
		
		stat.execute(deleteTags);
		stat.execute(deleteExps);
		stat.execute(deleteWords);
		stat.execute(deleteWordsStart);
		stat.execute(deleteWordsEnd);
		stat.execute(deleteSegments);
		stat.execute(deleteSegmentsStart);
		stat.execute(deleteSegmentsEnd);
		stat.execute(deleteTraces);
		stat.execute(deleteTracers);
		stat.execute(deleteImages);
		stat.execute(deleteVideos);
		stat.execute(deleteProject);
		
		stat.close();
	}

	public void setCAPWarningOn() throws SQLException {
		Statement stat = conn.createStatement();
		String command = "UPDATE settings SET value='0' WHERE parameter='showCAPWarning'";
		stat.execute(command);
		stat.close();
	}
	
	public boolean isCAPWarningOn() throws SQLException {
		Statement stat = conn.createStatement();
		String command = "SELECT value FROM settings WHERE parameter='showCAPWarning'";
		ResultSet rs = stat.executeQuery(command);
		rs.next();
		String result = rs.getString(1);
		stat.close();
		if(result.equals("1")){
			stat.close();
			return true;
		}
		else{
			if(!result.equals("0")){
				System.err.println("Settings value is neither 1 nor 0!");
			}
			stat.close();
			return false;
		}
	}

	public String getAudioAddressForVideoID(String id) throws SQLException {
		Statement stat = conn.createStatement();
		System.out.println(id);
		String command = "SELECT audio_address FROM video WHERE id="+id+";";
		ResultSet rs = stat.executeQuery(command);
		rs.next();
		String result = rs.getString(1);
		stat.close();
		return result;
	}
	
}