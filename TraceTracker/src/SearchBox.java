import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.ToolTipManager;


@SuppressWarnings("serial")
public class SearchBox extends JPanel{
	public JTextField imageTitleTextField;
	public JComboBox projectCombo;
	public JComboBox tracerCombo;
	public JComboBox languageCombo;
	public JComboBox experimentCombo;
	public JCheckBox corruptCheckbox;
	public JComboBox autotraceCombo;
	public JComboBox howManyTracersCombo;
	public JComboBox tagsCombo;
	String[] tagsList;
	String[] experimentsList;
	String[] tracersList;
	String[] projectsList;
	String[] languagesList;
	private DBConnector db;
	private JLabel lblTags;
	public JTextField wordTextField;
	public JTextField segmentTextField;
	private JTextField marginSizeTextField;
	public JComboBox representativeFramesCombo;
	MainFrame mf;
	
	public SearchBox(MainFrame mainFrame) {
		db = mainFrame.db;
		try {
			db.initializeDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		mf = mainFrame;
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(null);
		
		JLabel lblImageTitle = new JLabel("Image Title:");
		lblImageTitle.setBounds(16, 12, 75, 16);
		add(lblImageTitle);
		
		JLabel lblProject = new JLabel("Project:");
		lblProject.setBounds(16, 42, 61, 16);
		add(lblProject);
		
		JLabel lblTracer = new JLabel("Traced by:");
		lblTracer.setBounds(256, 12, 75, 16);
		add(lblTracer);
		
		JLabel lblLanguage = new JLabel("Language:");
		lblLanguage.setBounds(16, 70, 75, 16);
		add(lblLanguage);
		
		JLabel lblExperiment = new JLabel("Experiment:");
		lblExperiment.setBounds(16, 100, 80, 16);
		add(lblExperiment);
		
		imageTitleTextField = new JTextField();
		imageTitleTextField.setBounds(103, 6, 138, 28);
		add(imageTitleTextField);
		imageTitleTextField.setColumns(10);
		
		projectsList = new String[0];
		try {
			projectsList = db.getProjectsList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		projectCombo = new JComboBox(projectsList);
		projectCombo.setBounds(103, 38, 138, 27);
		add(projectCombo);
		
		tracersList = new String[0];
		try {
			tracersList = db.getTracersList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		tracerCombo = new JComboBox(tracersList);
		tracerCombo.setBounds(348, 8, 138, 27);
		add(tracerCombo);
		
		languagesList = new String[0];
		try {
			languagesList = db.getLanguagesList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		languageCombo = new JComboBox(languagesList);
		languageCombo.setBounds(103, 68, 138, 27);
		add(languageCombo);
		
		experimentsList = new String[0];
		try {
			experimentsList = db.getExperimentsList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		experimentCombo = new JComboBox(experimentsList);
		experimentCombo.setBounds(103, 98, 138, 27);
		add(experimentCombo);
		
		corruptCheckbox = new JCheckBox("Corrupt Image");
		corruptCheckbox.setBounds(489, 98, 128, 23);
		add(corruptCheckbox);
		
		JLabel lblAutotraced = new JLabel("AutoTraced:");
		lblAutotraced.setBounds(257, 72, 101, 16);
		add(lblAutotraced);
		
		autotraceCombo = new JComboBox(new String[]{"","Yes","No"});
		autotraceCombo.setBounds(348, 68, 138, 27);
		add(autotraceCombo);
		
		JLabel lblNumberOfTracers = new JLabel("Tracers:");
		lblNumberOfTracers.setBounds(256, 42, 80, 16);
		add(lblNumberOfTracers);
		
		howManyTracersCombo = new JComboBox(new String[]{"","0","1","2","More than 2"});
		howManyTracersCombo.setBounds(348, 38, 138, 27);
		add(howManyTracersCombo);
		
		lblTags = new JLabel("Tag:");
		lblTags.setBounds(257, 104, 61, 16);
		add(lblTags);
		
		tagsList = new String[0];
		try {
			tagsList = db.getTagsList();
		} catch (SQLException e) {
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		final DefaultComboBoxModel model = new DefaultComboBoxModel();
		tagsCombo = new JComboBox(model);
		tagsCombo.setBounds(348, 98, 138, 27);
		add(tagsCombo);
		
		JLabel lblWord = new JLabel("Word:");
		lblWord.setBounds(498, 12, 75, 16);
		add(lblWord);
		
		wordTextField = new JTextField();
		wordTextField.setColumns(10);
		wordTextField.setBounds(577, 6, 138, 28);
		add(wordTextField);
		
		JLabel lblSegment = new JLabel("Segment:");
		lblSegment.setToolTipText("<html>Write your search term in the first box. Indicate the number of\n<br>\npreceding and following frames you need in the second box.\n<br><br>\nExample:\n<br>\n$a (r) a\t&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\t2-1\n<br>\nThis will search for frames belonging to an \"r\" segment that is\n<br>\nsurrounded by an initial \"a\" and another \"a\". The search result\n<br>\nwill also contain the 2 preceding frames and 1 following frame\n<br>\nfor each sequence of frames belonging to one particular segment.\n<html>");
		lblSegment.setBounds(498, 43, 75, 16);
		ToolTipManager.sharedInstance().setDismissDelay(20000);
		add(lblSegment);
		
		segmentTextField = new JTextField();
		segmentTextField.setColumns(10);
		segmentTextField.setBounds(577, 37, 95, 28);
		add(segmentTextField);
		
		marginSizeTextField = new JTextField();
		marginSizeTextField.setBounds(675, 37, 40, 28);
		add(marginSizeTextField);
		marginSizeTextField.setColumns(10);
		
		JLabel showOnlyLabel = new JLabel("Show only:");
		showOnlyLabel.setToolTipText("<html>\nUse this option if you want only the representative frame\n<br>\nfor to each segment. For example, if you are searching for\n<br>\nthe segment \"s\" and you are only interested in the first\n<br>\nframe of each instance of \"s\", choose \"initial\".\n</html>");
		showOnlyLabel.setBounds(498, 72, 75, 16);
		add(showOnlyLabel);
		
		String[] representativesList = {"","Middle","Second","Second to last","Initial","Final"};
		representativeFramesCombo = new JComboBox(representativesList);
		representativeFramesCombo.setBounds(577, 68, 138, 27);
		add(representativeFramesCombo);
		for(String s:tagsList){
			model.addElement(s);
		}
	}

	public Integer getMarginSize(String part) {
		String ms = marginSizeTextField.getText();
		if(ms.contains("-")){
			int dashIndex = ms.indexOf("-");
			if("before".equals(part)){
				ms = ms.substring(0,dashIndex);
			}
			else{
				ms = ms.substring(dashIndex+1);
			}
		}
		Integer marginSize = null;
		if(isInteger(ms)){			
			marginSize = Integer.parseInt(ms);
		}
		if(marginSize==null){
			marginSize=0;
		}
		return marginSize;
	}
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}

	public void updateData() {
		try {
			experimentsList = db.getExperimentsList();
			DefaultComboBoxModel model = (DefaultComboBoxModel) experimentCombo.getModel();
			model.removeAllElements();
			for(String s:experimentsList){
				model.addElement(s);
			}
			
			tagsList = db.getTagsList();
			model = (DefaultComboBoxModel) tagsCombo.getModel();
			model.removeAllElements();
			for(String s:tagsList){
				model.addElement(s);
			}
			
			tracersList = db.getTracersList();
			model = (DefaultComboBoxModel) tracerCombo.getModel();
			model.removeAllElements();
			for(String s:tracersList){
				model.addElement(s);
			}
			
			projectsList = db.getProjectsList();
			model = (DefaultComboBoxModel) projectCombo.getModel();
			model.removeAllElements();
			for(String s:projectsList){
				model.addElement(s);
			}
			
			languagesList = db.getProjectsList();
			model = (DefaultComboBoxModel) projectCombo.getModel();
			model.removeAllElements();
			for(String s:projectsList){
				model.addElement(s);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
	}

	public void clearAllFields() {
		imageTitleTextField.setText("");
		projectCombo.setSelectedIndex(0);
		languageCombo.setSelectedIndex(0);
		experimentCombo.setSelectedIndex(0);
		tracerCombo.setSelectedIndex(0);
		howManyTracersCombo.setSelectedIndex(0);
		autotraceCombo.setSelectedIndex(0);
		tagsCombo.setSelectedIndex(0);
		wordTextField.setText("");
		segmentTextField.setText("");
		marginSizeTextField.setText("");
		representativeFramesCombo.setSelectedIndex(0);
		corruptCheckbox.setSelected(false);
	}
}
