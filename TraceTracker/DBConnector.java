import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

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
	}

	synchronized public ArrayList<ImageData> executeQuery(SearchBox sb) throws SQLException{
		if(conn.isClosed()){
			Properties props = new Properties();
			props.setProperty("dontTrackOpenResources","true");
			conn = DriverManager.getConnection("jdbc:sqlite:traceFiles.db",props);
		}
		ArrayList<ImageData> result = new ArrayList<ImageData>();
		String titleEntered = sb.imageTitleTextField.getText(); 
		String projectEntered = (String) sb.projectCombo.getSelectedItem();
		String tracerEntered = (String) sb.tracerCombo.getSelectedItem();
		String languageEntered = (String) sb.languageCombo.getSelectedItem();
		String experimentEntered = (String) sb.experimentCombo.getSelectedItem();
		int autotraceEntered = sb.autotraceCombo.getSelectedIndex();
		int corruptEntered = sb.corruptCheckbox.isSelected()? 1 : 0;
		int howManyTracersEntered = sb.howManyTracersCombo.getSelectedIndex();
		String tag = sb.tagsTextField.getText();
		
		String query = "SELECT image.id AS theid, image.title AS image_title, video.title AS video_title, video.subject AS subject, project.title AS project_title, image.address AS address"
		+" FROM image JOIN video ON image.video_id=video.id JOIN project ON project.id=video.project_id"
		+" WHERE image.id>=0 "; //This last thing after "where" is a dummy condition
		
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
			query += "AND theid IN (SELECT image.id FROM image JOIN experiment_association ON image.id=experiment_association.image_id JOIN experiment ON experiment.id=experiment_association.experiment_id WHERE experiment.title='"+experimentEntered+"') ";
		}
		if(corruptEntered==1){
			query += "AND image.is_bad = 1 ";
		}
		if(howManyTracersEntered==1){
			query += "AND (SELECT COUNT(*) FROM image JOIN trace ON image.id=trace.image_id WHERE image.id = theid)=0 ";
		}
		else if(howManyTracersEntered==2){
			query += "AND (SELECT COUNT(*) FROM image JOIN trace ON image.id=trace.image_id WHERE image.id = theid)=1 ";
		}
		else if(howManyTracersEntered==3){
			query += "AND (SELECT COUNT(*) FROM image JOIN trace ON image.id=trace.image_id WHERE image.id = theid)=2 ";
		}
		else if(howManyTracersEntered==4){
			query += "AND (SELECT COUNT(*) FROM image JOIN trace ON image.id=trace.image_id WHERE image.id = theid)>2 ";
		}
		if(tag != null && tag.length()>0){
			query += "AND (SELECT COUNT(*) FROM tag WHERE tag.content='"+tag+"' AND tag.image_id=theid)>0 "; 
		}
		query += "ORDER BY project_title, video_title, image_title;";
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery(query);
		while(rs.next()){
			ImageData image = new ImageData();
			image.id = rs.getString("theid");
			image.title = rs.getString("image_title");
			image.video = rs.getString("video_title");
			image.subject = rs.getString("subject");
			image.project = rs.getString("project_title");
			image.address = rs.getString("address");
			result.add(image);
		}
		stat.close();
		return result;
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
		return result;
	}
	
	public String[] getExperimentsList() throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM experiment;";
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
		return result;
	}
	
	public String getExperiments(String id) throws SQLException{
		Statement stat = conn.createStatement();
		String query = "SELECT experiment.title FROM experiment_association JOIN image ON experiment_association.image_id=image.id JOIN experiment ON experiment.id=experiment_association.experiment_id WHERE image.id="+id+";";
		ResultSet rs = stat.executeQuery(query);
		String result = "";
		while(rs.next()){
			result += rs.getString(1)+", ";
		}
		if(result.length()>2){			
			result = result.substring(0, result.length()-2);
		}
		return result;
	}
	
	public void addImages(HashMap<String, ImageData> images, ArrayList<Trace> traces, String projectName, String projectAddress, String videoName, String videoAddress) throws Exception{
		Statement stat = conn.createStatement();
		String query = "SELECT id FROM project WHERE title='"+projectName+"';";
		ResultSet rs = stat.executeQuery(query);
		int projectID = -1;
		if(!rs.next()){
			//We should add such a project if it doesn't exist
			String query2 = "INSERT INTO project(title, folder_address) VALUES('"+projectName+"','"+projectAddress+"');";
			Statement stat2 = conn.createStatement();
			stat2.executeUpdate(query2);
			ResultSet rs2 = stat2.getGeneratedKeys();
			rs2.next();
			projectID = rs2.getInt(1);
		}
		else{
			projectID = rs.getInt(1);
		}
		//The project is taken care of. Now add the video.
		int underscoreIndex = videoName.indexOf("_");
		String subject = videoName.substring(0, underscoreIndex-1); 
		Statement stat3 = conn.createStatement();
		String query3 = "INSERT INTO video(title,subject,project_id,folder_address) VALUES('"+videoName+"','"+subject+"',"+projectID+",'"+videoAddress+"');";
		stat3.executeUpdate(query3);
		ResultSet rs4 = stat3.getGeneratedKeys();
		rs4.next();
		int videoID = rs4.getInt(1);
		//The video is added. Now add the images.
		Statement stat4 = conn.createStatement();
		for(ImageData image: images.values()){
			String query4 = "INSERT INTO image(video_id, title, address) VALUES("+videoID+",'"+image.title+"','"+image.address+"');";
			stat4.executeUpdate(query4);
			//Save the db id of the image we just added. We need it when associating trace files with images in the next loop
			ResultSet rs5 = stat4.getGeneratedKeys();
			while(rs5.next()){
				image.id = String.valueOf(rs5.getInt(1));
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
			}
		}
		
		//Because db queries are time-consuming we batch trace files of the same tracer together, so that we
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
						System.err.println("The image and trace names do not match");
						throw new Exception();
					}
					String query7 = "INSERT INTO trace(address, tracer_id, image_id) VALUES('"+trace.address+"',"+tracerID+","+image.id+");";
					stat.executeUpdate(query7);
					//Check "autotraced" if the tracer was MAK
					if("MAK".equals(tracerNames[i])){
						String query8 = "UPDATE image SET autotraced=1 WHERE id="+image.id+";";
						stat.executeUpdate(query8);
					}
				}
			}
		}
	}

	public boolean checkProjectIsNew(String projectAddress) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM project where folder_address='"+projectAddress+"';";
		ResultSet rs = stat.executeQuery(query);
		if(rs.next()){
			return false;
		}
		else{			
			return true;
		}
	}

	public boolean checkProjectNameIsNew(String projectName) throws SQLException {
		Statement stat = conn.createStatement();
		String query = "SELECT title FROM project where title='"+projectName+"';";
		ResultSet rs = stat.executeQuery(query);
		if(rs.next()){
			return false;
		}
		else{			
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
		return addresses;
	}

	public int tagImages(HashMap<Integer, ImageData> buffer, String tagContent) throws SQLException {
		Statement stat = conn.createStatement();
		int counter = 0;
		for(ImageData image: buffer.values()){
			//Check if this image doesn't already have that tag
			Statement stat2 = conn.createStatement();
			String query = "SELECT id FROM tag WHERE image_id="+image.id+";";
			ResultSet rs = stat2.executeQuery(query);
			if(rs.next()){
				continue;
			}
			//Now insert the tag
			String update = "INSERT INTO tag(image_id,content) VALUES("+image.id+",'"+tagContent+"');";
			stat.addBatch(update);
			counter++;
		}
		stat.executeBatch();
		return counter;
	}

	public void untagImages(HashMap<Integer, ImageData> buffer, String tagContent) throws SQLException {
		Statement stat = conn.createStatement();
		for(ImageData image: buffer.values()){
			//Check if this image doesn't already have that tag
			String update = "DELETE FROM tag WHERE image_id="+image.id+" AND content='"+tagContent+"';";
			stat.addBatch(update);
		}
		stat.executeBatch();
	}

}