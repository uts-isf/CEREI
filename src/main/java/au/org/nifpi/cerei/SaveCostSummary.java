package au.org.nifpi.cerei;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Saves the data in the Cost summary Jtable (displayed on one of the GUI panes) to a .csv file 
 * 
 * @author James Sargeant
 */
public class SaveCostSummary extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public SaveCostSummary(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Save Energy Bill - Summary");
		setPreferredSize(new Dimension(CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH,CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT));
		addActionListener(this);
		this.setEnabled(false);
	}

	/**
	 * Save the Cost Summary file when button is clicked.
	 * 
	 * @param e Button click.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new InputTypeFilter());
        chooser.setAcceptAllFileFilterUsed(false);

        // Default file name
        chooser.setSelectedFile(new File("Cost Summary "+UI.cost.costsPerMonth[0][0].year + " "+UI.networkParameters.tariffName+".csv"));
        
	    int retrival = chooser.showSaveDialog(null);
	    //Only proceed if user clicks on "Save"
	    if (retrival == JFileChooser.APPROVE_OPTION) {
	    	File summaryFile = new File(chooser.getSelectedFile().toString());
	    	// Check to see if file exists.
	    	if (summaryFile.exists()) {
	    	    int response = JOptionPane.showConfirmDialog(null, //
	    	            "Do you want to replace the existing file?", //
	    	            "Confirm", JOptionPane.YES_NO_OPTION, //
	    	            JOptionPane.QUESTION_MESSAGE);
	    	    //Bail out of method if user does not want to overwrite existing file
	    	    if (response != JOptionPane.YES_OPTION) {
	    	        return;
	    	    }
	    	}
	    	
	    	// Output the contents of the Cost Summary JTable data structure.
	    	try(BufferedWriter fw = new BufferedWriter(new FileWriter(summaryFile))) {
	            fw.write("Cost Summary for "+UI.cost.costsPerMonth[0][0].year + " using tariff "+UI.networkParameters.tariffName);
	            fw.newLine();
	            fw.newLine();
	            String[] summaryHeader = UI.cost.getColumnNames();
	            fw.write(summaryHeader[0]);
	            for (int i=1;i<summaryHeader.length; i++ ) {
	            	fw.write(",");
	            	fw.write(summaryHeader[i]);
	            }
	            fw.newLine();
	            
	            //Get the JTable data
	            String[][] priceData = UI.cost.createSummaryOutput(UI.cost.allMeterCostSummaries);

	            //Get the JTable data
	            for (int i=0; i<priceData.length; i++) {
	            	// Remove any extraneous ","'s
	            	fw.write(priceData[i][0].replaceAll(",", ""));
	            	// Column by column, removing any extraneous ","'s 
	            	for (int j=1; j<priceData[i].length; j++) {
		            	fw.write(",");
		            	fw.write(priceData[i][j].replaceAll(",", ""));
	            	}
	            	// New row.
	            	fw.newLine();
	            }
	        } catch (Exception ex) {
		    	//Need to display a warning dialog with the error.
		    	JOptionPane.showMessageDialog(UI,ex.getMessage(),"Engery Calculator Error",JOptionPane.ERROR_MESSAGE); //
	        }
	    }
	}

}
