package au.org.nifpi.cerei;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Saves the data in the Lifecycle Cost Analysis summary Jtable (displayed on one of the GUI panes) to a .csv file
 *  
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class SaveLifecycleSummary extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;
	
	/** Generates a String with two decimal places and no commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df2 = new DecimalFormat("#.00");


	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public SaveLifecycleSummary(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Save Life-cycle Cost Assessment - Summary");
		// Revised text from client too long to fit in standard button size - so increase to 1.2 button size
		setPreferredSize(new Dimension((int) (CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH*1.2),CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT));
		addActionListener(this);
		this.setEnabled(false);
	}

	/**
	 * Save the Lifecycle Cost Analysis Summary file when button is clicked.
	 * 
	 * @param e Button click.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new InputTypeFilter());
        chooser.setAcceptAllFileFilterUsed(false);

        //Default file name
        String defaultName = UI.lifecylce.investmentName;
        // Add the tariff to the filename if it can be determined.
        if (UI.networkParameters != null) {
        	defaultName = defaultName.concat(" and "+ UI.networkParameters.tariffName);
        }
        
        // Add the year to the filename if it can be determined.  Year from bau file takes precedence over
        // year from usage and generated files.
        if (UI.bau != null) {
        	defaultName = defaultName.concat(" for "+UI.bau.year);
        }
        else if (UI.cost != null) {
        	defaultName = defaultName.concat(" for "+UI.cost.costsPerMonth[0][0].year);
        }

        // Append details.csv to the filename.
        String defaultFileName = defaultName.concat(" summary.csv");
        
        chooser.setSelectedFile(new File(defaultFileName));
        
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
	    		fw.write("Summary for "+defaultName);
	    		fw.newLine();
	            fw.newLine();
	            fw.write("Number of Components," + UI.lifecylce.lifecycleCostComponents.length);
	            fw.newLine();
	            fw.write("Period of Analysis (years)," + UI.lifecylce.lifetimeString);
	            fw.newLine();
	            fw.write("Initial Cost of Investment ($)," + df2.format(-1*UI.lifecylce.costOfInvestment));
	            fw.newLine();
	            fw.write("Present Value of All Costs ($)," + df2.format(UI.lifecylce.npvCost));
	            fw.newLine();
	            fw.write("Present Value of Total Saving ($)," + df2.format(UI.lifecylce.npvRevenue));
	            fw.newLine();
	            fw.write("Net Present Value (NPV) ($)," + df2.format(UI.lifecylce.npvCost + UI.lifecylce.npvRevenue));
	            fw.newLine();
	            fw.write("Annual Life-cycle Cost ($/year)," + df2.format(UI.lifecylce.totalATLCC));
	            fw.newLine();
	            fw.write("Annual Life-cycle Saving ($/year)," + df2.format(UI.lifecylce.npvRevenue/UI.lifecylce.lifetime));
	            fw.newLine();
	            fw.write("Annual Worth (AW) ($/year)," + df2.format(UI.lifecylce.totalATLCC + UI.lifecylce.annualTotalSavings));
	            fw.newLine();
	            fw.write("Annual Energy Demand (kWh/year)," + df2.format(UI.lifecylce.totalGridUsed));
	            fw.newLine();
	            fw.write("Total Life-cycle Energy Generated (kWh)," + df2.format(UI.lifecylce.sumALCCEnergyGenerated));
	            fw.newLine();
	            fw.write("Annual Energy Generated (kWh/year)," + df2.format(UI.lifecylce.totalAnnualEnergyGenerated));
	            fw.newLine();
	            fw.write("Annual Energy Used On-site from the DER (kWh/year)," + df2.format(UI.lifecylce.totalAnnualEnergyGenerated - UI.lifecylce.totalAnnualEnergyExportedToGrid));
	            fw.newLine();
	            fw.write("Annual Energy Exported to the Grid (kWh/year)," + df2.format(UI.lifecylce.totalAnnualEnergyExportedToGrid));
	            fw.newLine();
	            fw.write("Annual Energy Imported from the Grid (kWh/year)," + df2.format(UI.lifecylce.totalGridUsed-(UI.lifecylce.totalAnnualEnergyGenerated-UI.lifecylce.totalAnnualEnergyExportedToGrid)));
	            fw.newLine();
	            
	            if (UI.lifecylce.annualTotalSavings == 0) {
		            fw.write("Payback Period (Years),n/a");
	    		}
	    		else {
	    			fw.write("Payback Period (Years)," + df2.format(UI.lifecylce.paybackPeriod));
	    		}
	            fw.newLine();
	    		if (UI.lifecylce.lcoe == -1) {
	    			fw.write("Levelized Cost of Energy (LCOE) ($/kWh),n/a");
	    		}
	    		else {
	    			fw.write("Levelized Cost of Energy (LCOE) ($/kWh)," + df2.format(UI.lifecylce.lcoe));
	    		}
	            fw.newLine();
	        } 
	    	catch (Exception ex) {
		    	//Need to display a warning dialog with the error.
		    	JOptionPane.showMessageDialog(UI,ex.getMessage(),"CEREI Error Saving Life-cycle Cost Analysis Summary to .csv file",JOptionPane.ERROR_MESSAGE); //
	        }
	    }
	}

}
