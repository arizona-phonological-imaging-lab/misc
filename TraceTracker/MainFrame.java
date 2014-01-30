import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.plaf.FileChooserUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.border.EtchedBorder;
import javax.swing.JScrollPane;
import javax.swing.JMenuBar;


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
	String[] columnNames = {"Image","Video","Subject","Project","Traced","Experiment"};
	final int pageLength = 20;
	public int currentPage;
	public int numberOfPages;
	public JButton nextButton;
	public JButton previousButton;
	public JButton firstButton;
	public JButton lastButton;
	public JLabel pageLabel;
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

		queryResultLabel = new JLabel("");
		queryResultLabel.setBounds(135, 127, 139, 16);
		getContentPane().add(queryResultLabel);
		
		this.setSize(952,596);
		getContentPane().setLayout(null);
		searchbox = new SearchBox(this);
		searchbox.setLocation(6, 6);
		searchbox.setSize(738, 111);
		getContentPane().add(searchbox);
		
		bufferPanel = new BufferPanel(this);
		bufferPanel.setLocation(756, 6);
		getContentPane().add(bufferPanel);
		
		bottomPanel = new JPanel();
		bottomPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		bottomPanel.setLocation(6,154);
		bottomPanel.setSize(738, 394);
		getContentPane().add(bottomPanel);
		bottomPanel.setLayout(null);
		
		firstButton = new JButton("<<");
		firstButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(1);
			}
		});
		firstButton.setBounds(164, 359, 75, 29);
		firstButton.setEnabled(false);
		bottomPanel.add(firstButton);
		
		previousButton = new JButton("<");
		previousButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(currentPage-1);
			}
		});
		previousButton.setBounds(244, 359, 75, 29);
		previousButton.setEnabled(false);
		bottomPanel.add(previousButton);
		
		nextButton = new JButton(">");
		nextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(currentPage+1);
			}
		});
		nextButton.setBounds(406, 359, 75, 29);
		nextButton.setEnabled(false);
		bottomPanel.add(nextButton);
		
		lastButton = new JButton(">>");
		lastButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				setCurrentPage(numberOfPages);
			}
		});
		lastButton.setBounds(486, 359, 75, 29);
		lastButton.setEnabled(false);
		bottomPanel.add(lastButton);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(6, 6, 726, 341);
		bottomPanel.add(scrollPane);
		
		table = new JTable(new MyTableModel());
		scrollPane.setViewportView(table);
		table.setBorder(new LineBorder(Color.decode("#aabbff")));
		table.setShowGrid(true);
		table.setGridColor(Color.decode("#aabbff"));
		
		pageLabel = new JLabel("Page 1 of 1");
		pageLabel.setBounds(315, 364, 94, 16);
		pageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		bottomPanel.add(pageLabel);
		table.getColumnModel().getColumn(4).setPreferredWidth(150);
		table.getColumnModel().getColumn(5).setPreferredWidth(150);
		
		JButton btnSearch = new JButton("Search");
		btnSearch.setBounds(6, 122, 117, 29);
		btnSearch.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					tableData = db.executeQuery(searchbox);
					numberOfPages = tableData.size()/pageLength+1;
					setCurrentPage(1);
					queryResultLabel.setText(tableData.size()+" results");
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
		JMenu collectMenu = new JMenu("Collect");
		JMenu tagMenu = new JMenu("Tag");
		JMenu updateMenu = new JMenu("Update");
		menuBar.add(collectMenu);
		menuBar.add(tagMenu);
		menuBar.add(updateMenu);
		JMenuItem updateDatabase = new JMenuItem("Update database from folder...");
		updateDatabase.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Updater updater = new Updater(MainFrame.this);
				String feedback = updater.updateDB();
				if(!"noFileChosen".equals(feedback)){
					JOptionPane.showMessageDialog(null, feedback);
				}
			}
		});
		updateMenu.add(updateDatabase);
		JMenuItem collectImages = new JMenuItem("Collect buffer images...");
		collectImages.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				bufferPanel.collect("image");
			}
		});
		collectMenu.add(collectImages);
		
		JMenuItem collectImagesWithTraces = new JMenuItem("Collect buffer images with traces...");
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
				// TODO Auto-generated catch block
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
		String error = "";
		for(StackTraceElement ste: e.getStackTrace()){
			error += ste.toString()+"\n";
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("error log.txt", true)));
			String timeStamp = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
			out.print(timeStamp+"\n");
			out.print(error);
			out.println("------------------------------------------------------------\n");
			out.close();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			System.err.println("Khayyat oftad tu kuze");
			e1.printStackTrace();
		}
	}
	
	public class MyTableModel extends DefaultTableModel{
		public int getColumnCount() {
			return 6;
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
