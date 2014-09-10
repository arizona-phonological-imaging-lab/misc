import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.Color;


public class CustomAddProjectFrame extends JFrame{
	private JTextField projectTitleTextField;
	private JTextField languageTextField;
	private JTextField videoTitleTextField;
	private JTextField subjectTextField;
	private JTextField imagesDirTextField;
	private JTextField TracesDirTextField;
	private JTextField textGridPathTextField;
	public CustomAddProjectFrame() {
		setTitle("Add Custom Project");
		getContentPane().setLayout(null);
		
		JLabel projectTitleLabel = new JLabel("Project Title:");
		projectTitleLabel.setBounds(16, 12, 99, 16);
		getContentPane().add(projectTitleLabel);
		
		projectTitleTextField = new JTextField();
		projectTitleTextField.setBounds(114, 6, 134, 28);
		getContentPane().add(projectTitleTextField);
		projectTitleTextField.setColumns(10);
		
		JLabel languageLabel = new JLabel("Language:");
		languageLabel.setBounds(16, 46, 99, 16);
		getContentPane().add(languageLabel);
		
		languageTextField = new JTextField();
		languageTextField.setColumns(10);
		languageTextField.setBounds(114, 40, 134, 28);
		getContentPane().add(languageTextField);
		
		JLabel videosLabel = new JLabel("Videos:");
		videosLabel.setBounds(16, 86, 61, 16);
		getContentPane().add(videosLabel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(16, 106, 227, 209);
		getContentPane().add(scrollPane);
		
		JList list = new JList();
		list.setBackground(new Color(255, 255, 204));
		scrollPane.setViewportView(list);
		
		JButton addNewVideoButton = new JButton("Add New");
		addNewVideoButton.setBounds(131, 327, 117, 29);
		getContentPane().add(addNewVideoButton);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setBounds(12, 327, 117, 29);
		getContentPane().add(deleteButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(260, 9, 248, 347);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblVideoOf = new JLabel("Video 0 of 0");
		lblVideoOf.setBounds(80, 6, 108, 16);
		panel.add(lblVideoOf);
		
		JLabel lblVideoTitle = new JLabel("Video Title:");
		lblVideoTitle.setBounds(6, 40, 99, 16);
		panel.add(lblVideoTitle);
		
		videoTitleTextField = new JTextField();
		videoTitleTextField.setColumns(10);
		videoTitleTextField.setBounds(104, 34, 134, 28);
		panel.add(videoTitleTextField);
		
		JLabel lblSubject = new JLabel("Subject");
		lblSubject.setBounds(6, 74, 99, 16);
		panel.add(lblSubject);
		
		subjectTextField = new JTextField();
		subjectTextField.setColumns(10);
		subjectTextField.setBounds(104, 68, 134, 28);
		panel.add(subjectTextField);
		
		JLabel lblImagesFolder = new JLabel("Images Directory:");
		lblImagesFolder.setBounds(6, 108, 127, 16);
		panel.add(lblImagesFolder);
		
		imagesDirTextField = new JTextField();
		imagesDirTextField.setBounds(6, 132, 143, 28);
		panel.add(imagesDirTextField);
		imagesDirTextField.setColumns(10);
		
		JButton btnChoose = new JButton("Browse...");
		btnChoose.setBounds(148, 133, 94, 29);
		panel.add(btnChoose);
		
		JButton button = new JButton("Browse...");
		button.setBounds(148, 197, 94, 29);
		panel.add(button);
		
		TracesDirTextField = new JTextField();
		TracesDirTextField.setColumns(10);
		TracesDirTextField.setBounds(6, 196, 143, 28);
		panel.add(TracesDirTextField);
		
		JLabel lblTracesDirectory = new JLabel("Traces Directory:");
		lblTracesDirectory.setBounds(6, 172, 127, 16);
		panel.add(lblTracesDirectory);
		
		JButton button_1 = new JButton("Browse...");
		button_1.setBounds(148, 264, 94, 29);
		panel.add(button_1);
		
		textGridPathTextField = new JTextField();
		textGridPathTextField.setColumns(10);
		textGridPathTextField.setBounds(6, 263, 143, 28);
		panel.add(textGridPathTextField);
		
		JLabel lblTextgridFile = new JLabel("TextGrid File:");
		lblTextgridFile.setBounds(6, 239, 127, 16);
		panel.add(lblTextgridFile);
		
		JLabel lblImages = new JLabel("0 images");
		lblImages.setBounds(6, 305, 108, 16);
		panel.add(lblImages);
		
		JLabel lblTraceFiles = new JLabel("0 trace files");
		lblTraceFiles.setBounds(6, 325, 108, 16);
		panel.add(lblTraceFiles);
	}
}
