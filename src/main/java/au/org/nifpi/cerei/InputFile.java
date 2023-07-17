package au.org.nifpi.cerei;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Creates a JPanel containing a JButton and a Label below the button.  The button is used to kick off a dialog to open an input file 
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class InputFile extends JPanel {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Button used to select the input file */
	protected JButton inputFileButton;
	/** Selected input file name*/
	private JLabel inputFileName;
	/** Input File */
	protected File inputFile=null;
	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;

	/**
	 * Create the JPanel
	 * 
	 * @param buttonName Label for Button
	 * @param _UI _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	protected InputFile(String buttonName, CostEffectiveRenewableEnergyInvestments _UI) {
		this.UI = _UI;
		this.setLayout(new BorderLayout());
	    inputFileButton = new JButton(buttonName);
	    inputFileButton.setPreferredSize(new Dimension(CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH,CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT));
	    inputFileName = new JLabel("Please select a file",SwingConstants.CENTER);
	    ActionListener retrieveFile = new RetrieveFileListener();
	    inputFileButton.addActionListener(retrieveFile);

	    this.add(inputFileButton,BorderLayout.NORTH);
	    this.add(inputFileName,BorderLayout.CENTER);
	}

	/**
	 * Reset the input file so that it isn't pointing to any file.
	 */
	protected void resetInputFile() {
		inputFileName.setText("Please select a file");
		inputFile = null;
	}
	/**
	 * Find the file when button is clicked.
	 */
	private class RetrieveFileListener implements ActionListener {
		/**
		 * Default constructor - does nothing.
		 */
		public RetrieveFileListener() {
			
		}
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Open "+inputFileButton.getText()+" file");
			// Only look for .csv
			fileChooser.addChoosableFileFilter(new InputTypeFilter());
			fileChooser.setAcceptAllFileFilterUsed(false);

			int option = fileChooser.showOpenDialog(UI);
			if (option == JFileChooser.APPROVE_OPTION) {
				inputFile = fileChooser.getSelectedFile();
				inputFileName.setText(inputFile.getName());
			}

			fileChooser.setSelectedFile(null);
		}
	}
	
}