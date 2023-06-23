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
 * Saves the detailed financial analysis to a .csv file 
 * 
 * @author James Sargeant
 */
public class SaveLifecycleDetails extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;
	
	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/** Generates a String with no decimal places and commas. Minimum of one whole number and no decimal places*/
	private static final DecimalFormat df0 = new DecimalFormat("0");  // Number of decimal places in summary tabs.
	/** Generates a String with two decimal places and no commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df2 = new DecimalFormat("0.00");  // Number of decimal places in summary tabs.
	/** Generates a String with two decimal places and no commas. Minimum of one whole number. */
	private static final DecimalFormat df2optional = new DecimalFormat("0.##");  // Number of decimal places in summary tabs.
	
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public SaveLifecycleDetails(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Save Life-cycle Cost Assessment - Detail");
		// Revised text from client too long to fit in standard button size - so increase to 1.2 button size
		setPreferredSize(new Dimension((int) (CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH*1.2),CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT));
		addActionListener(this);
		this.setEnabled(false);
	}

	/**
	 * Save thefFinancial analysis details file when button is clicked.
	 * 
	 * @param e Button click.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	    JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new InputTypeFilter());
        chooser.setAcceptAllFileFilterUsed(false);
        
        //Default file name
        String defaultFileName = UI.lifecylce.investmentName;
        // Add the tariff to the filename if it can be determined.
        if (UI.networkParameters != null) {
        	defaultFileName = defaultFileName.concat(" and "+ UI.networkParameters.tariffName);
        }
        
        // Add the year to the filename if it can be determined.  Year from bau file takes precedence over
        // year from usage and generated files.
        if (UI.bau != null) {
        	defaultFileName = defaultFileName.concat(" for "+UI.bau.year);
        }
        else if (UI.cost != null) {
        	defaultFileName = defaultFileName.concat(" for "+UI.cost.costsPerMonth[0][0].year);
        }

        // Append details.csv to the filename.
        defaultFileName = defaultFileName.concat(" details.csv");
        
        chooser.setSelectedFile(new File(defaultFileName));
        
	    int retrival = chooser.showSaveDialog(null);
	    if (retrival == JFileChooser.APPROVE_OPTION) {
	    	File summaryFile = new File(chooser.getSelectedFile().toString());
	    	if (summaryFile.exists()) {
	    	    int response = JOptionPane.showConfirmDialog(null, //
	    	            "Do you want to replace the existing file?", //
	    	            "Confirm", JOptionPane.YES_NO_OPTION, //
	    	            JOptionPane.QUESTION_MESSAGE);
	    	    if (response != JOptionPane.YES_OPTION) {
	    	        return;
	    	    }
	    	}
	        try(BufferedWriter fw = new BufferedWriter(new FileWriter(summaryFile))) {
	        	// Create a description that is as detailed as possible.
	            String firstLine = "Details for "+UI.lifecylce.investmentName;
	            // Add the tariff to the filename if it can be determined.
	            if (UI.networkParameters != null) {
	            	firstLine = firstLine.concat(" and "+ UI.networkParameters.tariffName);
	            }
	            
	            // Add the year to the filename if it can be determined.  Year from bau file takes precedence over
	            // year from usage and generated files.
	            if (UI.bau != null) {
	            	firstLine = firstLine.concat(" for "+UI.bau.year);
	            }
	            else if (UI.cost != null) {
	            	firstLine = firstLine.concat(" for "+UI.cost.costsPerMonth[0][0].year);
	            }
	            fw.write(firstLine);
	            fw.newLine();
	            fw.newLine();
	            // Table Header
	            fw.write("Cost Code,Component,Unit Cost (AUD),Unit,No of Units,Payment (Years),Year of Analysis (Years),Total Cost,Discount Rate (%),Inflation Rate,"+
	            		"Inflation adjusted discount rate (j) (%),NPV (AUD),ATLCC (AUD)");
	            fw.newLine();
	    		for (int i=0; i< UI.lifecylce.lifecycleCostComponents.length; i++) {
            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+",");
            		fw.write(UI.lifecylce.lifecycleCostComponents[i].name+",");
	    			fw.newLine();
	    			int costSubCode=1;
		            if(UI.lifecylce.lifecycleCostComponents[i].captialCost != 0) {
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+"."+ costSubCode++ + ",");
	            		fw.write("Captial Cost,");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].captialCost) +",");
	            		fw.write("AUD/Unit,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].qtyString + ",");
	            		fw.write("0,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].lifetimeString +",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].totalCapitalCost)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].discountRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].inflationRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].j)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].npvCapital)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].atlccCapital));
	            		fw.newLine();
	            	}
		            if(UI.lifecylce.lifecycleCostComponents[i].installationCost != 0) {
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+"."+ costSubCode++ + ",");
	            		fw.write("Installation Cost,");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].installationCost) +",");
	            		fw.write("AUD/Unit,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].qtyString + ",");
	            		fw.write("0,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].lifetimeString +",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].totalInstallationCost)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].discountRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].inflationRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].j)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].npvInstallation)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].atlccInstallation));
	            		fw.newLine();
	            	}
		            if(UI.lifecylce.lifecycleCostComponents[i].fixedOMCost != 0) {
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+"."+ costSubCode++ + ",");
	            		fw.write("Fixed O&M Costs,");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].fixedOMCost) +",");
	            		fw.write("AUD/Unit,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].qtyString + ",");
	            		fw.write("1 ... "+df2optional.format(UI.lifecylce.lifecycleCostComponents[i].lifetime - 1)+",");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].lifetimeString +",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].totalFixedOMCost)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].discountRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].inflationRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].j)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].npvFixedOM)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].atlccfixedOMCost));
	            		fw.newLine();
	            	}
		            if(UI.lifecylce.lifecycleCostComponents[i].replacementCost != 0) {
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+"."+ costSubCode++ + ",");
	            		fw.write("Replacement Cost,");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].replacementCost) +",");
	            		fw.write("AUD/Unit,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].qtyString + ",");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].replacementPayments+",");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].lifetimeString +",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].totalReplacementCost)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].discountRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].inflationRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].j)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].npvReplacement)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].atlccReplacement));
	            		fw.newLine();
	            	}
		            if(UI.lifecylce.lifecycleCostComponents[i].futureCost != 0) {
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].costCode+"."+ costSubCode++ + ",");
	            		fw.write("Future Cost,");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].futureCost) +",");
	            		fw.write("AUD/Unit,");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].qtyString + ",");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].futurePayments+",");
	            		fw.write(UI.lifecylce.lifecycleCostComponents[i].lifetimeString +",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].totalFutureCost)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].discountRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].inflationRate)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].j)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].npvFuture)+",");
	            		fw.write(df2.format(UI.lifecylce.lifecycleCostComponents[i].atlccFuture));
	            		fw.newLine();
	            	}
	            }
	    		fw.write(df0.format(UI.lifecylce.lifecycleCostComponents.length+1)+",Scenario Equipment Totals,,,,,,,,,,"+df2.format(UI.lifecylce.npvTotal)+","+df2.format(UI.lifecylce.totalATLCC));
	    		fw.newLine();
	    	} catch (Exception ex) {
		    	//Need to display a warning dialog with the error.
		    	JOptionPane.showMessageDialog(UI,ex.getMessage(),"Engery Calculator Error saving Lifecycle Cost Analysis Summary to .csv file",JOptionPane.ERROR_MESSAGE); //
	        }
	    }
	}

}
