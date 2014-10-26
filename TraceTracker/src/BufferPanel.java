import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;


@SuppressWarnings("serial")
public class BufferPanel extends JPanel{
	MainFrame mainFrame;
	JList bufferList;
	DefaultListModel model;
	HashMap<Integer, ImageData> buffer;
	JLabel bufferSizeLabel;
	DBConnector db;
	
	public BufferPanel(MainFrame mf){
		buffer = new HashMap<Integer, ImageData>();
		db = mf.db;
		mainFrame = mf;
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setSize(190, 560);
		setLayout(null);
		
		model = new DefaultListModel();
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(6, 69, 178, 404);
		add(scrollPane_1);
		
		JButton collectButton = new JButton("Clear buffer");
		collectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				model.clear();
				buffer.clear();
				bufferSizeLabel.setText(String.valueOf(buffer.size()));
			}
		});
		collectButton.setBounds(38, 507, 117, 29);
		add(collectButton);
		
		JButton addSelectionButton = new JButton("Add selection");
		addSelectionButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int[] selectedIndices = mainFrame.table.getSelectedRows();
				for(int i=0; i<selectedIndices.length; i++){
					int index = selectedIndices[i];
					int realIndex = (mainFrame.currentPage-1)*mainFrame.pageLength+index;
					ImageData image = mainFrame.tableData.get(realIndex);
					buffer.put(Integer.valueOf(image.id), image);
				}
				int i = model.getSize()+1;
				model.addElement("Selection "+i+"  ("+selectedIndices.length+")");
				bufferSizeLabel.setText(String.valueOf(buffer.size()));
			}
		});
		addSelectionButton.setBounds(38, 6, 117, 29);
		add(addSelectionButton);
		
		JButton addAllButton = new JButton("Add all");
		addAllButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				for(ImageData image: mainFrame.tableData){
					buffer.put(Integer.parseInt(image.id), image);
				}
				int i = model.getSize()+1;
				model.addElement("Selection "+i+"  ("+mainFrame.tableData.size()+")");
				bufferSizeLabel.setText(String.valueOf(buffer.size()));
			}
		});
		addAllButton.setBounds(38, 36, 117, 29);
		add(addAllButton);
		
		bufferList = new JList();
		bufferList.setModel(model);
		bufferList.setBackground(new Color(248, 248, 255));
		scrollPane_1.setViewportView(bufferList);
		
		JLabel lblTotalInBuffer = new JLabel("Total in buffer:");
		lblTotalInBuffer.setBounds(6, 485, 94, 16);
		add(lblTotalInBuffer);
		
		bufferSizeLabel = new JLabel("0");
		bufferSizeLabel.setBounds(107, 485, 61, 16);
		add(bufferSizeLabel);
	}

	public void collect(String duty) {
		if(buffer.isEmpty()){
			JOptionPane.showMessageDialog(null, "The buffer is empty","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		String folderName = (String) JOptionPane.showInputDialog(null, "Please choose the name of your export folder.");
		if(folderName==null){
			return;
		}
		File theFolder = new File(folderName);
		boolean result = theFolder.mkdir();
		if(!result){
			JOptionPane.showMessageDialog(null, "A folder with this name already exists in the working directory!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		int counter = 0;
		String[] badAddresses = new String[buffer.size()];
		int badAddressesCounter = 0;
		for(ImageData image: buffer.values()){
			counter++;
			if(image.address==null){
				System.out.println("no address: "+image.title);
				counter--;
				continue;
			}
			File source = new File(image.address);
			if(!source.exists()){
				badAddresses[badAddressesCounter] = image.address;
				badAddressesCounter++;
				counter--;
			}
			int underscoreIndex = image.video.indexOf("_");
			String shortVideoName = image.video.substring(0,underscoreIndex);
			File dest = new File(theFolder.getAbsolutePath()+"/"+image.project+"_"+shortVideoName+"_"+image.title);
			try {
				copy(source,dest);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "There was some problem in copying the image files.\nSee the log file.","Error",JOptionPane.ERROR_MESSAGE);
				MainFrame.printErrorLog(e);
				return;
			}
			//Now copy the trace files too if necessary
			if(duty.equals("both")){
				ArrayList<String> traceAddresses;
				try {
					traceAddresses = db.getTraceAddresses(image);
				} catch (SQLException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "There was an error in retrieving the trace files. See the log file.","Error",JOptionPane.ERROR_MESSAGE);
					MainFrame.printErrorLog(e1);
					return;
				}
				for(String address : traceAddresses){
					source = new File(address);
					int pngIndex = source.getName().indexOf("png");
					String traceSuffix = source.getName().substring(pngIndex+3);
					dest = new File(theFolder.getAbsolutePath()+"/"+image.project+"_"+shortVideoName+"_"+image.title+traceSuffix);
					try {
						copy(source,dest);
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, "There was some problem in copying the trace files. See the log file.","Error",JOptionPane.ERROR_MESSAGE);
						MainFrame.printErrorLog(e);
						return;
					}
				}
			}
		}
		String errorMessage = badAddressesCounter+" addresses were incorrect:";
		for(int i=0; i<badAddressesCounter & i<10; i++){
			errorMessage += "\n"+badAddresses[i];
		}
		if(badAddressesCounter>=10){
			errorMessage += "\n,...";
		}
		if(badAddressesCounter>0){			
			JOptionPane.showMessageDialog(null, errorMessage,"Error",JOptionPane.ERROR_MESSAGE);
		}

		JOptionPane.showMessageDialog(null,counter+" images were copied successfully.");
	}
	public static void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

	public void tag(boolean isExperiment, boolean isRightClick) {
		ArrayList<ImageData> images = new ArrayList<ImageData>();
		if(!isRightClick){
			images = new ArrayList<ImageData>(buffer.values());
		}
		else{
			//Find a list of selected images (rows)
			int[] selectedIndices = mainFrame.table.getSelectedRows();
			for(int i=0; i<selectedIndices.length; i++){
				int index = selectedIndices[i];
				int realIndex = (mainFrame.currentPage-1)*mainFrame.pageLength+index;
				ImageData image = mainFrame.tableData.get(realIndex);
				images.add(image);
			}
		}
		if(!isRightClick && images.isEmpty()){
			JOptionPane.showMessageDialog(null, "The buffer is empty!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(isRightClick && images.isEmpty()){
			JOptionPane.showMessageDialog(null, "No rows are selected!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String tagContent = (String) JOptionPane.showInputDialog(null, "Please write the content of the tag.");
		if(tagContent==null){
			return;
		}
		if(tagContent.length()==0){
			JOptionPane.showMessageDialog(null, "No name was entered.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		int taggedCounter = 0;
		try {
			taggedCounter = db.tagImages(images, tagContent, isExperiment);
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "There was an error. See the log file.","Error",JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			MainFrame.printErrorLog(e);
		}
		JOptionPane.showMessageDialog(null,taggedCounter+" images were tagged '"+tagContent+"'.");
		mainFrame.searchbox.updateData();
	}

	public void untag(boolean isExperiment, boolean isRightClick) {
		ArrayList<ImageData> images = new ArrayList<ImageData>();
		if(!isRightClick){
			images = new ArrayList<ImageData>(buffer.values());
		}
		else{
			//Find a list of selected images (rows)
			int[] selectedIndices = mainFrame.table.getSelectedRows();
			for(int i=0; i<selectedIndices.length; i++){
				int index = selectedIndices[i];
				int realIndex = (mainFrame.currentPage-1)*mainFrame.pageLength+index;
				ImageData image = mainFrame.tableData.get(realIndex);
				images.add(image);
			}
		}
		if(!isRightClick && images.isEmpty()){
			JOptionPane.showMessageDialog(null, "The buffer is empty!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(isRightClick && images.isEmpty()){
			JOptionPane.showMessageDialog(null, "No rows are selected!","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] tagsList = mainFrame.searchbox.tagsList;
		String title;
		String message;
		if(isExperiment){
			title = "Choose the experiment you want to remove";
			message = "Choose experiment";
		}
		else{
			title = "Choose the tag you want to remove";
			message = "Choose tag";
		}
		String[] theAppropriateList = tagsList;
		if(isExperiment){
			theAppropriateList = mainFrame.searchbox.experimentsList; 
		}
		
		String userInput = (String)JOptionPane.showInputDialog(null,message,title, JOptionPane.PLAIN_MESSAGE, null, theAppropriateList, theAppropriateList[0]);

		//If a string was returned, say so.
		if ((userInput != null) && (userInput.length() > 0)) {
			try {
				db.untagImages(images, userInput, isExperiment);
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "There was an error. See the log file.","Error",JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				MainFrame.printErrorLog(e);
			}
			if(isExperiment){
				JOptionPane.showMessageDialog(null,"The experiment was successfully removed from the images.");
			}
			else{
				JOptionPane.showMessageDialog(null,"The images were successfully untagged.");
			}
		}

		if(userInput!=null && userInput.length()==0){
			JOptionPane.showMessageDialog(null, "No name was entered.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		mainFrame.searchbox.updateData();
	}
}
