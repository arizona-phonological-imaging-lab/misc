import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.border.EtchedBorder;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;


@SuppressWarnings("serial")
public class MainFrame extends JFrame{
	{
		//Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	SearchBox searchbox;
	BufferPanel bufferPanel;
	public JTable table;
	JPanel bottomPanel;
	DBConnector db;
	ArrayList<ImageData> tableData;
	JLabel queryResultLabel;
	String[] columnNames = {"Image","Video","Subject","Project","Traced","Experiment","Tags","Segment","Word"};
	final int pageLength = 20;
	public int currentPage;
	public int numberOfPages;
	public JButton nextButton;
	public JButton previousButton;
	public JButton firstButton;
	public JButton lastButton;
	public JLabel pageLabel;
	public static String targetSegmentDisplayMode;
	public static int resultSize;
	public MainFrame() {
		getBackup();
		currentPage = 1;
		numberOfPages =1;
		db = new DBConnector();
		try {
			db.initializeDB();
		} catch (Exception e) {
			e.printStackTrace();
			printErrorLog(e);
		}
		targetSegmentDisplayMode = "colors";
		queryResultLabel = new JLabel("");
		queryResultLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		queryResultLabel.setBounds(477, 147, 154, 16);
		getContentPane().add(queryResultLabel);
		
		this.setSize(952,616);
		getContentPane().setLayout(null);
		searchbox = new SearchBox(this);
		searchbox.setLocation(6, 6);
		searchbox.setSize(738, 131);
		getContentPane().add(searchbox);
		
		bufferPanel = new BufferPanel(this);
		bufferPanel.setLocation(756, 6);
		getContentPane().add(bufferPanel);
		
		bottomPanel = new JPanel();
		bottomPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		bottomPanel.setLocation(6,175);
		bottomPanel.setSize(738, 391);
		getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		firstButton = new JButton("<<");
		firstButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(1);
			}
		});
		firstButton.setBounds(126, 359, 75, 29);
		firstButton.setEnabled(false);
		bottomPanel.add(firstButton);
		
		previousButton = new JButton("<");
		previousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(currentPage-1);
			}
		});
		previousButton.setBounds(206, 359, 75, 29);
		previousButton.setEnabled(false);
		bottomPanel.add(previousButton);
		
		nextButton = new JButton(">");
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(currentPage+1);
			}
		});
		nextButton.setBounds(439, 359, 75, 29);
		nextButton.setEnabled(false);
		bottomPanel.add(nextButton);
		
		lastButton = new JButton(">>");
		lastButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(numberOfPages);
			}
		});
		lastButton.setBounds(519, 359, 75, 29);
		lastButton.setEnabled(false);
		bottomPanel.add(lastButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 6, 726, 341);
		bottomPanel.add(scrollPane);
		
		table = new JTable(new MyTableModel()){
			public Component prepareRenderer(TableCellRenderer renderer, int index_row, int index_col) {
		        // get the current row
		        Component comp = super.prepareRenderer(renderer, index_row, index_col);
		        JComponent jc = (JComponent) comp;
		        int index = (currentPage-1)*pageLength+index_row;
				if(tableData == null || tableData.size()<index+1){
					return jc;
				}
				ImageData image = tableData.get(index);
				if(image !=null && image.isLastInSet){					
					jc.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,Color.decode("#aabbff")));
				}
		        return jc;
		    }
		};
		scrollPane.setViewportView(table);
		table.setBorder(new LineBorder(Color.decode("#aabbff")));
		table.setShowGrid(true);
		table.setGridColor(Color.decode("#aabbff"));
		
		pageLabel = new JLabel("Page 1 of 1");
		pageLabel.setBounds(291, 364, 136, 16);
		pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bottomPanel.add(pageLabel);
		table.getColumnModel().getColumn(4).setPreferredWidth(100);
		table.getColumnModel().getColumn(5).setPreferredWidth(100);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(630, 142, 117, 29);
		btnSearch.addActionListener(new ActionListener(){


			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					tableData = db.executeQuery(searchbox);
					numberOfPages = tableData.size()/pageLength+1;
					if(tableData.size()!=0 && tableData.size()%20==0){
						numberOfPages--;
					}
					setCurrentPage(1);
					String text = resultSize+" results";
					queryResultLabel.setText(text);
					//queryResultLabel.paintImmediately(queryResultLabel.getVisibleRect());
				} catch (SQLException e) {
					e.printStackTrace();
					printErrorLog(e);
				}
			}
			
		});
		getContentPane().add(btnSearch);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		JMenu collectMenu = new JMenu("Export");
		JMenu tagMenu = new JMenu("Tag");
		JMenu updateMenu = new JMenu("Add data");
		JMenu viewMenu = new JMenu("View");
		menuBar.add(collectMenu);
		menuBar.add(tagMenu);
		menuBar.add(updateMenu);
		menuBar.add(viewMenu);
		JMenuItem addProject = new JMenuItem("Add new project...");
		addProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Updater updater = new Updater(MainFrame.this);
				updater.updateDB("addProject");
			}
		});
		updateMenu.add(addProject);
		JMenuItem updateProject = new JMenuItem("Update existing project...");
		updateProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Updater updater = new Updater(MainFrame.this);
				updater.updateDB("updateProject");
			}
		});
		updateMenu.add(updateProject);
		JMenuItem collectImages = new JMenuItem("Export buffer images...");
		collectImages.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				bufferPanel.collect("image");
			}
		});
		collectMenu.add(collectImages);
		
		JMenuItem collectImagesWithTraces = new JMenuItem("Export buffer images with traces...");
		collectImagesWithTraces.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				bufferPanel.collect("both");
			}
		});
		collectMenu.add(collectImagesWithTraces);
		
		JMenuItem tagImages = new JMenuItem("Add tag to buffer images...");
		tagImages.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				bufferPanel.tag();
			}
		});
		tagMenu.add(tagImages);
		
		JMenuItem untagImages = new JMenuItem("Remove tag from buffer images...");
		untagImages.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				bufferPanel.untag();
			}
		});
		tagMenu.add(untagImages);
		
		
		JMenuItem segmentDisplayMenuItem = new JMenu("Segment display method");
		ButtonGroup segmentDisplayGroup = new ButtonGroup();
		JRadioButtonMenuItem colorMenuItem = new JRadioButtonMenuItem("Colors", true);
		JRadioButtonMenuItem bracketsMenuItem = new JRadioButtonMenuItem("Brackets");
		segmentDisplayGroup.add(colorMenuItem);
		segmentDisplayGroup.add(bracketsMenuItem);
		segmentDisplayMenuItem.add(colorMenuItem);
		segmentDisplayMenuItem.add(bracketsMenuItem);
		viewMenu.add(segmentDisplayMenuItem);
		colorMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				targetSegmentDisplayMode = "colors";
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
			}
		});
		bracketsMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				targetSegmentDisplayMode = "brackets";
				((AbstractTableModel) table.getModel()).fireTableDataChanged();
			}
		});
		
		//The columns view
		JMenu columnDisplayMenu = new JMenu("Columns");
		JCheckBoxMenuItem videoMenuItem = new JCheckBoxMenuItem("video");
		
	}



	public static void main(String[] args) {
		MainFrame mf = new MainFrame();
		mf.setResizable(false);
		mf.setVisible(true);
		mf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void getBackup() {
		File source = new File("traceFiles.db");
		if(!source.exists()){
			JOptionPane.showMessageDialog(null, "No database file could be found. The application will now close.","Error",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		String date = new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime());
		File dest = new File("backup "+date+" traceFiles.db");
		if(!dest.exists()){
			try {
				BufferPanel.copy(source, dest);
			} catch (IOException e) {
				e.printStackTrace();
				printErrorLog(e);
			}
		}
	}

	public void setCurrentPage(int i) {
		//We assume nobody calls this function with a bad argument
		currentPage = i;
		if(currentPage==1){
			previousButton.setEnabled(false);
			firstButton.setEnabled(false);
		}
		else{
			previousButton.setEnabled(true);
			firstButton.setEnabled(true);
		}
		if(currentPage==numberOfPages){
			nextButton.setEnabled(false);
			lastButton.setEnabled(false);
		}
		else{
			nextButton.setEnabled(true);
			lastButton.setEnabled(true);
		}
		pageLabel.setText("Page "+currentPage+" of "+numberOfPages);
		((AbstractTableModel) table.getModel()).fireTableDataChanged();
	}
	
	public void printErrorLog(Exception e){
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String error = sw.toString();
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("error log.txt", true)));
			String timeStamp = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			out.print(timeStamp+"\n");
			out.print(error);
			out.println("------------------------------------------------------------\n");
			out.close();
		} catch (Exception e1) {
			System.err.println("begandad namak");
			e1.printStackTrace();
		}
	}
	
	public class MyTableModel extends DefaultTableModel{
		public int getColumnCount() {
			return 9;
		}
		
		public int getRowCount() {
			return pageLength;
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}
		
		public Object getValueAt(int row, int col) {
			int index = (currentPage-1)*pageLength+row;
			if(tableData == null || tableData.size()<index+1){
				return " ";
			}
			ImageData image = tableData.get(index);
			switch(col){
			case 0:
				return image.title;
			case 1:
				return image.video;
			case 2:
				return image.subject;
			case 3:
				return image.project;
			case 4:
				try {
					return db.getTracers(image.id);
				} catch (SQLException e) {
					e.printStackTrace();
					printErrorLog(e);
					return "";
				}
			case 5:
				try {
					return db.getExperiments(image.id);
				} catch (SQLException e) {
					e.printStackTrace();
					printErrorLog(e);
					return "";
				}
			case 6:
				try {
					return db.getTags(image.id);
				} catch (Exception e) {
					e.printStackTrace();
					printErrorLog(e);
					return "";
				}
			case 7:
				try {
					return db.getEnvironment(image.id);
				} catch (Exception e) {
					e.printStackTrace();
					printErrorLog(e);
					return "";
				}
			case 8:
				try {
					return db.getWord(image.id);
				} catch (Exception e) {
					e.printStackTrace();
					printErrorLog(e);
					return "";
				}
			}
			
			return " ";
		}
		
		public Class getColumnClass(int c) {
			return "salam".getClass();	
		}
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		public void setValueAt(Object value, int row, int col) {
			try{
				//prizePortions[row][col]=(Integer) value;
				fireTableCellUpdated(row, col);
			}catch(NumberFormatException n){
				JOptionPane.showMessageDialog(null,"bad number");
			}
		}
	}
}
