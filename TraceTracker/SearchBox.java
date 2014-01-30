import java.sql.SQLException;

import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;


public class SearchBox extends JPanel{
	public JTextField imageTitleTextField;
	public JComboBox projectCombo;
	public JComboBox tracerCombo;
	public JComboBox languageCombo;
	public JComboBox experimentCombo;
	public JCheckBox corruptCheckbox;
	public JComboBox autotraceCombo;
	public JComboBox howManyTracersCombo;
	public JTextField tagsTextField;
	private DBConnector db;
	private JLabel lblTags;
	public SearchBox(MainFrame mainFrame) {
		db = new DBConnector();
		try {
			db.initializeDB();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
		
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setLayout(null);
		
		JLabel lblImageTitle = new JLabel("Image Title:");
		lblImageTitle.setBounds(16, 12, 75, 16);
		add(lblImageTitle);
		
		JLabel lblProject = new JLabel("Project:");
		lblProject.setBounds(16, 42, 61, 16);
		add(lblProject);
		
		JLabel lblTracer = new JLabel("Traced by:");
		lblTracer.setBounds(16, 72, 75, 16);
		add(lblTracer);
		
		JLabel lblLanguage = new JLabel("Language:");
		lblLanguage.setBounds(257, 12, 75, 16);
		add(lblLanguage);
		
		JLabel lblExperiment = new JLabel("Experiment:");
		lblExperiment.setBounds(257, 42, 101, 16);
		add(lblExperiment);
		
		imageTitleTextField = new JTextField();
		imageTitleTextField.setBounds(103, 6, 138, 28);
		add(imageTitleTextField);
		imageTitleTextField.setColumns(10);
		
		String[] projectsList = new String[0];
		try {
			projectsList = db.getProjectsList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
		projectCombo = new JComboBox(projectsList);
		projectCombo.setBounds(103, 38, 138, 27);
		add(projectCombo);
		
		String[] tracersList = new String[0];
		try {
			tracersList = db.getTracersList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
		tracerCombo = new JComboBox(tracersList);
		tracerCombo.setBounds(103, 68, 138, 27);
		add(tracerCombo);
		
		String[] languagesList = new String[0];
		try {
			languagesList = db.getLanguagesList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
		languageCombo = new JComboBox(languagesList);
		languageCombo.setBounds(348, 8, 138, 27);
		add(languageCombo);
		
		String[] experimentsList = new String[0];
		try {
			experimentsList = db.getExperimentsList();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mainFrame.printErrorLog(e);
		}
		experimentCombo = new JComboBox(experimentsList);
		experimentCombo.setBounds(348, 38, 138, 27);
		add(experimentCombo);
		
		corruptCheckbox = new JCheckBox("Corrupt Image");
		corruptCheckbox.setBounds(498, 68, 128, 23);
		add(corruptCheckbox);
		
		JLabel lblAutotraced = new JLabel("AutoTraced:");
		lblAutotraced.setBounds(257, 72, 101, 16);
		add(lblAutotraced);
		
		autotraceCombo = new JComboBox(new String[]{"","Yes","No"});
		autotraceCombo.setBounds(348, 68, 138, 27);
		add(autotraceCombo);
		
		JLabel lblNumberOfTracers = new JLabel("Tracers:");
		lblNumberOfTracers.setBounds(498, 12, 101, 16);
		add(lblNumberOfTracers);
		
		howManyTracersCombo = new JComboBox(new String[]{"","0","1","2","More than 2"});
		howManyTracersCombo.setBounds(589, 8, 138, 27);
		add(howManyTracersCombo);
		
		lblTags = new JLabel("Tag:");
		lblTags.setBounds(498, 42, 61, 16);
		add(lblTags);
		
		tagsTextField = new JTextField();
		tagsTextField.setBounds(593, 36, 128, 28);
		add(tagsTextField);
		tagsTextField.setColumns(10);
	}
}
