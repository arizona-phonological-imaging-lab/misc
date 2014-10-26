import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;


public class CustomAddProjectFrame extends JFrame{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTextField projectTitleTextField;
	public JTextField languageTextField;
	public JTextField videoTitleTextField;
	public JTextField subjectTextField;
	public JTextField imagesDirTextField;
	public JTextField tracesDirTextField;
	public JTextField textGridPathTextField;
	public JButton btnApplyChanges;
	private DefaultListModel model;
	public JList list;
	private JFileChooser jfc;
	private ArrayList<Video> videos;
	private MainFrame mainFrame;
	private JPanel coverPanel;
	private JPanel panel;
	private JLabel lblVideoOf;
	private JLabel lblImages;
	private DBConnector db;
	//These three are used when we don't want to change the selection of the user in the list because she has unapplied changes:
	public boolean listForceSelectionFlag = false;
	public int lastSelectedIndex, secondToLastSelectedIndex;			 
	
	public CustomAddProjectFrame(MainFrame mf) {
		mainFrame = mf;
		db = new DBConnector();
		try {
			db.initializeDB();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		list = new JList();
		model = new DefaultListModel();
		list.setModel(model);
		list.setDragEnabled(false);
	    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getValueIsAdjusting()){
					return;
				}
				Video selectedVideo = (Video)list.getSelectedValue();
				int selectedIndex = list.getSelectedIndex();
				secondToLastSelectedIndex = lastSelectedIndex;
				lastSelectedIndex = selectedIndex; 
				if(listForceSelectionFlag){
					listForceSelectionFlag = false;
					return;
				}
				updateVideoFrame(selectedVideo, selectedIndex);
			}
		});
		videos = new ArrayList<Video>();
		jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setTitle("Add Custom Project");
		getContentPane().setLayout(null);
		
		JLabel projectTitleLabel = new JLabel("Project Title:");
		projectTitleLabel.setBounds(21, 18, 99, 16);
		getContentPane().add(projectTitleLabel);
		
		projectTitleTextField = new JTextField();
		projectTitleTextField.setBounds(119, 12, 134, 28);
		getContentPane().add(projectTitleTextField);
		projectTitleTextField.setColumns(10);
		
		JLabel languageLabel = new JLabel("Language:");
		languageLabel.setBounds(21, 52, 99, 16);
		getContentPane().add(languageLabel);
		
		languageTextField = new JTextField();
		languageTextField.setColumns(10);
		languageTextField.setBounds(119, 46, 134, 28);
		getContentPane().add(languageTextField);
		
		JLabel videosLabel = new JLabel("Image sequences:");
		videosLabel.setBounds(21, 92, 130, 16);
		getContentPane().add(videosLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(21, 112, 227, 222);
		getContentPane().add(scrollPane);
		
		list.setBackground(new Color(255, 255, 204));
		scrollPane.setViewportView(list);
		
		JButton addNewVideoButton = new JButton("Add New");
		addNewVideoButton.setBounds(136, 346, 117, 29);
		addNewVideoButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				jfc.setDialogTitle("Select image sequence folder");
				int returnVal = jfc.showOpenDialog(null);
				if(returnVal!=JFileChooser.APPROVE_OPTION){
					System.out.println("Nothing chosen");
					return;
				}
				File f = jfc.getSelectedFile();
				createNewVideo(f);
			}
		});
		getContentPane().add(addNewVideoButton);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setBounds(17, 346, 117, 29);
		deleteButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int index = list.getSelectedIndex();
				listForceSelectionFlag = true;
				lastSelectedIndex = 0;
				secondToLastSelectedIndex = 0;
				model.remove(index);
				videos.remove(index);
				coverPanel.setVisible(true);
				panel.setVisible(false);
				btnApplyChanges.setEnabled(false);
			}
		});
		getContentPane().add(deleteButton);
		
		panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(274, 12, 266, 367);
		getContentPane().add(panel);
		panel.setLayout(null);

		
		lblVideoOf = new JLabel("Image Sequence 0 of 0");
		lblVideoOf.setBounds(62, 6, 166, 16);
		panel.add(lblVideoOf);
		
		JLabel lblVideoTitle = new JLabel("Sequence Title:");
		lblVideoTitle.setBounds(17, 40, 99, 16);
		panel.add(lblVideoTitle);
		
		videoTitleTextField = new JTextField();
		videoTitleTextField.setColumns(10);
		videoTitleTextField.setBounds(115, 34, 134, 28);
		//We name the documents so that we can keep track of them in the event handler
		videoTitleTextField.getDocument().putProperty("name", "title");
		panel.add(videoTitleTextField);
		
		JLabel lblSubject = new JLabel("Subject:");
		lblSubject.setBounds(17, 74, 99, 16);
		panel.add(lblSubject);
		
		subjectTextField = new JTextField();
		subjectTextField.setColumns(10);
		subjectTextField.setBounds(115, 68, 134, 28);
		subjectTextField.getDocument().putProperty("name", "subject");
		panel.add(subjectTextField);
		
		JLabel lblImagesFolder = new JLabel("Images Directory:");
		lblImagesFolder.setBounds(17, 108, 127, 16);
		panel.add(lblImagesFolder);
		
		imagesDirTextField = new JTextField();
		imagesDirTextField.setBounds(17, 132, 143, 28);
		panel.add(imagesDirTextField);
		imagesDirTextField.setColumns(10);
		imagesDirTextField.getDocument().putProperty("name", "imagesDir");
		
		JButton browseImageButton = new JButton("Browse...");
		browseImageButton.setBounds(159, 133, 94, 29);
		browseImageButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jfc.setDialogTitle("Select image folder");
				int returnVal = jfc.showOpenDialog(null);
				if(returnVal!=JFileChooser.APPROVE_OPTION){
					System.out.println("Nothing chosen");
					return;
				}
				File f = jfc.getSelectedFile();
				imagesDirTextField.setText(f.getAbsolutePath());
			}
		});
		panel.add(browseImageButton);
		
		JButton browseTraceButton = new JButton("Browse...");
		browseTraceButton.setBounds(159, 197, 94, 29);
		browseTraceButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				jfc.setDialogTitle("Select traces folder");
				int returnVal = jfc.showOpenDialog(null);
				if(returnVal!=JFileChooser.APPROVE_OPTION){
					System.out.println("Nothing chosen");
					return;
				}
				File f = jfc.getSelectedFile();
				tracesDirTextField.setText(f.getAbsolutePath());
			}
		});
		panel.add(browseTraceButton);
		
		tracesDirTextField = new JTextField();
		tracesDirTextField.setColumns(10);
		tracesDirTextField.setBounds(17, 196, 143, 28);
		tracesDirTextField.getDocument().putProperty("name", "tracesDir");
		panel.add(tracesDirTextField);
		
		JLabel lblTracesDirectory = new JLabel("Traces Directory:");
		lblTracesDirectory.setBounds(17, 172, 127, 16);
		panel.add(lblTracesDirectory);
		
		JButton browseTextgridButton = new JButton("Browse...");
		browseTextgridButton.setBounds(159, 264, 94, 29);
		browseTextgridButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser jfc2 = new JFileChooser();
				jfc2.setFileFilter(new FileFilter() {
					
					@Override
					public String getDescription() {
						return null;
					}
					
					@Override
					public boolean accept(File f) {
						String extension = f.getName().substring(f.getName().lastIndexOf(".")+1);
						if("textgrid".equals(extension.toLowerCase())){
							return true;
						}
						else{
							return false;
						}
					}
				});
				jfc2.setDialogTitle("Select TextGrid file");
				int returnVal = jfc2.showOpenDialog(null);
				if(returnVal!=JFileChooser.APPROVE_OPTION){
					System.out.println("Nothing chosen");
					return;
				}
				File f = jfc2.getSelectedFile();
				textGridPathTextField.setText(f.getAbsolutePath());
			}
		});
		panel.add(browseTextgridButton);
		
		textGridPathTextField = new JTextField();
		textGridPathTextField.setColumns(10);
		textGridPathTextField.setBounds(17, 263, 143, 28);
		textGridPathTextField.getDocument().putProperty("name", "textGrid");
		panel.add(textGridPathTextField);
		
		JLabel lblTextgridFile = new JLabel("TextGrid File:");
		lblTextgridFile.setBounds(17, 239, 127, 16);
		panel.add(lblTextgridFile);
		
		lblImages = new JLabel("0 images");
		lblImages.setBounds(17, 305, 108, 16);
		panel.add(lblImages);
		
		btnApplyChanges = new JButton("Apply changes");
		btnApplyChanges.setBounds(69, 332, 127, 29);
		btnApplyChanges.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateVideoInfo();
				btnApplyChanges.setEnabled(false);
			}
		});
		btnApplyChanges.setEnabled(false);
		panel.add(btnApplyChanges);
		
		imagesDirTextField.setEditable(false);
		tracesDirTextField.setEditable(false);
		textGridPathTextField.setEditable(false);
		
		coverPanel = new JPanel();
		coverPanel.setBounds(274, 12, 266, 367);
		getContentPane().add(coverPanel);
		coverPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		coverPanel.setOpaque(true);
		coverPanel.setLayout(null);
		
		JLabel lblSelectVideoTo = new JLabel("<html><p align=center>Select image sequence to view\nproperties</p></html>");
		lblSelectVideoTo.setBounds(33, 165, 203, 32);
		coverPanel.add(lblSelectVideoTo);
		coverPanel.setVisible(true);
		panel.setVisible(false);
		
		JButton btnOk = new JButton("Load Project");
		btnOk.setBounds(215, 400, 117, 29);
		btnOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(projectTitleTextField.getText().length()==0){
					JOptionPane.showMessageDialog(null, "Please enter a title for the project.","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				String[] projects = {};
				try {
					projects = db.getProjectsList();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<String> projectsArray = new ArrayList<String>(Arrays.asList(projects));
				if(projectsArray.contains(projectTitleTextField.getText())){
					JOptionPane.showMessageDialog(null, "A project with this title already exists.","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(btnApplyChanges.isEnabled()){					
					int reply = JOptionPane.showConfirmDialog(null,"Are you sure you want to discard the changes you have\nmade in the selected video?","Message",JOptionPane.YES_NO_OPTION);
					if(reply != JOptionPane.YES_OPTION){
						return;
					}
				}
				if(videos.size()<1){
					JOptionPane.showMessageDialog(null, "There are no videos in this project.","Error",JOptionPane.ERROR_MESSAGE);
					return;
				}
				Updater updater = new Updater(mainFrame);
				setVideosLanguageAndProject();
				updater.updateDB("addCustomProject",videos);
				CustomAddProjectFrame.this.setVisible(false);
			}
		});
		getContentPane().add(btnOk);
		
		//For each component of the right hand panel if it was a textField add an myKeyListener to it.
		for(Component c: panel.getComponents()){
			if(c.getClass().equals(imagesDirTextField.getClass())){
				JTextField tf = (JTextField) c;
				tf.getDocument().addDocumentListener(new MyKeyListener());
			}
		}
	}
	
	private void createNewVideo(File f){
		Video video = new Video();
		video.title = f.getName();
		video.subject = "";
		video.setImagesDirectory(f);
		if(video.getNumberOfImages()==0){
			JOptionPane.showMessageDialog(null, "The folder you selected does not contain any images.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		videos.add(video);
		model.addElement(video);
	}
	
	private void updateVideoFrame(Video video, int index){
		//Updates the left hand frame to show the information of the video that was just selected by the user
		
		//First we might want to ask the user if she wants to discard changes
		if(btnApplyChanges.isEnabled()){
			int reply = JOptionPane.showConfirmDialog(null,"Are you sure you want to discard the changes you have\nmande in the selected video?","Message",JOptionPane.YES_NO_OPTION);
			if(reply != JOptionPane.YES_OPTION){
				listForceSelectionFlag = true;
				list.setSelectedIndex(secondToLastSelectedIndex);
				return;
			}
		}
		lblVideoOf.setText("Image Sequence "+(index+1)+" of "+(videos.size()));
		videoTitleTextField.setText(video.title);
		subjectTextField.setText(video.subject);
		if(video.getImagesDirectory()!=null && video.getImagesDirectory().length()>0){
			imagesDirTextField.setText(video.getImagesDirectory().getAbsolutePath());
		}
		else{
			imagesDirTextField.setText("");
		}
		if(video.getTracesDirectory()!=null && video.getTracesDirectory().length()>0){
			tracesDirTextField.setText(video.getTracesDirectory().getAbsolutePath());
		}
		else{
			tracesDirTextField.setText("");
		}
		if(video.textGridFile!=null && video.textGridFile.length()>0){
			textGridPathTextField.setText(video.textGridFile.getAbsolutePath());
		}
		else{
			textGridPathTextField.setText("");
		}
		lblImages.setText(video.getNumberOfImages()+" images");
		coverPanel.setVisible(false);
		panel.setVisible(true);
	}
	
	private void updateVideoInfo(){
		//Updates the information of the Video object based on the changes the user has made in the GUI
		if(imagesDirTextField.getText().length()==0){
			JOptionPane.showMessageDialog(null, "The image directory cannot be empty.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(videoTitleTextField.getText().length()==0){
			JOptionPane.showMessageDialog(null, "Please enter a title for the image sequence.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		Video video = (Video) list.getSelectedValue();
		if(videoTitleTextField.getText().length()==0){
			video.title = "";
		}
		else{
			video.title = videoTitleTextField.getText();
		}
		if(subjectTextField.getText().length()==0){
			video.subject = "";
		}
		else{
			video.subject = subjectTextField.getText();
		}
		if(video.getImagesDirectory()!=null && video.getImagesDirectory().length()>0){
			video.setImagesDirectory(new File(imagesDirTextField.getText()));
		}
		else{
			video.setImagesDirectory(null);
		}
		if(video.getTracesDirectory()!=null && video.getTracesDirectory().length()>0){
			video.setTracesDirectory(new File(tracesDirTextField.getText()));
		}
		else{
			video.setTracesDirectory(null);
		}
		if(video.textGridFile!=null && video.textGridFile.length()>0){
			video.textGridFile = new File(textGridPathTextField.getText());
		}
		else{
			video.textGridFile = null;
		}
		
		list.repaint();
	}
	
	private void setVideosLanguageAndProject(){
		//Reads the project title and language textboxes the moment the user presses OK
		//and adds the data to all of the videos.
		String title = projectTitleTextField.getText();
		String lang = languageTextField.getText();
		for(Video v: videos){
			v.project = title;
			v.language = lang;
		}
	}
	
	private class MyKeyListener implements DocumentListener{

		@Override
		public void changedUpdate(DocumentEvent e) {
			//
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			Document doc = e.getDocument();
			String imagesDirectory = "";
			String tracesDirectory = "";
			String textGrid = "";
			String text = "";
			try {
				text = doc.getText(0, doc.getLength());
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				MainFrame.printErrorLog(e1);
			}
			Video selectedVideo = (Video) list.getSelectedValue();
			System.out.println(selectedVideo.title+", "+selectedVideo.getTracesDirectory());
			if(selectedVideo.getImagesDirectory()!=null){
				imagesDirectory = selectedVideo.getImagesDirectory().getAbsolutePath();
			}
			if(selectedVideo.getTracesDirectory()!=null){
				tracesDirectory = selectedVideo.getTracesDirectory().getAbsolutePath();
			}
			if(selectedVideo.textGridFile!=null){
				textGrid = selectedVideo.textGridFile.getAbsolutePath();
			}
			if(doc.getProperty("name").equals("title")){
				if(text.equals(selectedVideo.title)){
					btnApplyChanges.setEnabled(false);
				}
				else{
					btnApplyChanges.setEnabled(true);
				}
			}
			else if(doc.getProperty("name").equals("subject")){
				if(text.equals(selectedVideo.subject)){
					btnApplyChanges.setEnabled(false);
				}
				else{
					System.out.println("subject: \""+text+"\""+" \""+selectedVideo.subject+"\"");
					btnApplyChanges.setEnabled(true);
				}
			}
			else if(doc.getProperty("name").equals("imagesDir")){
				if(text.equals(imagesDirectory)){
					btnApplyChanges.setEnabled(false);
				}
				else{
					btnApplyChanges.setEnabled(true);
				}
			}
			else if(doc.getProperty("name").equals("tracesDir")){
				if(text.equals(tracesDirectory)){
					btnApplyChanges.setEnabled(false);
				}
				else{
					System.out.println("tracesDir: \""+text+"\""+" \""+tracesDirectory+"\"");
					btnApplyChanges.setEnabled(true);
				}
			}
			else if(doc.getProperty("name").equals("textGrid")){
				if(text.equals(textGrid)){
					btnApplyChanges.setEnabled(false);
				}
				else{
					System.out.println("textGrid: \""+text+"\""+" \""+textGrid+"\"");
					btnApplyChanges.setEnabled(true);
				}
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			insertUpdate(e);
		}


		
	}
}
