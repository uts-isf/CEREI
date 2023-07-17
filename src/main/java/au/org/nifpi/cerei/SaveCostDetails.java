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
 * Saves the detailed cost information to a .csv file 
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class SaveCostDetails extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;
	
	/** Format summary tables to the required number of decimal places */
	private static final DecimalFormat df = new DecimalFormat("#.00");
	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public SaveCostDetails(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Save Energy Bill - Detail");
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
        chooser.setSelectedFile(new File("Cost Details "+UI.cost.costsPerMonth[0][0].year + " "+UI.networkParameters.tariffName+".csv"));
        
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
	            fw.write("Details for "+UI.cost.costsPerMonth[0][0].year + " using tariff "+UI.networkParameters.tariffName);
	            fw.newLine();
	            fw.newLine();

	            int numberOfMeters = UI.meterNames.size();
	            //Dump details meter by meter
	            for (int i=0; i<numberOfMeters; i++) {
		            fw.newLine();
	            	fw.write("Details for meter "+UI.cost.meterNames.get(i));
	            	fw.newLine();
	            	// 12 months in a year
	            	for (int j=0;j<12;j++) {
		            	fw.newLine();
	            		fw.write( Cost.MONTH_NAMES[UI.cost.costsPerMonth[i][j].month] + " " +UI.cost.costsPerMonth[i][j].year + " for meter "+ UI.cost.meterNames.get(i));
	            		fw.newLine();
	            		fw.write("Energy Charges,Rate,Rate (Inc. Loss),Unit,Usage,Unit,Loss ratio,Loss ratio (%),Price($)");
	            		fw.newLine();
	            		fw.write("Pool Pass Through Charges import to site,Spot Price,Spot price + (Spot price x Loss ratio),c/kWh,"
	    	            		+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed)+",kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].spotPriceLossRatio) +","
           						+ df.format(UI.cost.costsPerMonth[i][j].spotPriceLossRatio*100) +","
	            				+ df.format(UI.cost.costsPerMonth[i][j].poolPassThroughCharge));
	            		fw.newLine();
	            		fw.write("Feed-in Charges from energy generated,Feed-in Tariff Rate,Feed-in Tariff Rate + (Feed-in Tariff Rate x Loss ratio),c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettExported)+",kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].feedInLossRatio) +","
           						+ df.format(UI.cost.costsPerMonth[i][j].feedInLossRatio*100) +","
	            				+ df.format(UI.cost.costsPerMonth[i][j].feedInCharge));
	            		fw.newLine();
	            		fw.write("Service and Admin Charge,,"+ df.format(UI.networkParameters.parameters[j].serviceAdminRate)+",$/Day,"
	            				+ UI.cost.costsPerMonth[i][j].daysInMonth + ",days,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].serviceAdminCharge));
	            		fw.newLine();
	            		fw.write("Network Charges");
	            		fw.newLine();
	            		fw.write("Standing Charge,,"+df.format(UI.networkParameters.parameters[j].standingRate)+",$/Yr,"
	            				+ UI.cost.costsPerMonth[i][j].daysInMonth + ",days,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].standingCharge));
	            		fw.newLine();
	            		if (UI.networkParameters.parameters[i].peakRate != 0) {
		            		fw.write("Peak Energy,,"+df.format(UI.networkParameters.parameters[j].peakRate)+",c/kWh,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].peakUsage) + ",kWh,,,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].peakEnergyCharge));
		            		fw.newLine();
	            		}
	            		if (UI.networkParameters.parameters[i].shoulderRate != 0) {
		            		fw.write("Shoulder Energy,,"+df.format(UI.networkParameters.parameters[j].shoulderRate)+",c/kWh,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].shoulderUsage) + ",kWh,,,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].shoulderEnergyCharge));
		            		fw.newLine();
	            		}
	            		if (UI.networkParameters.parameters[i].offpeakRate != 0) {
		            		fw.write("Off Peak Energy,,"+df.format(UI.networkParameters.parameters[j].offpeakRate)+",c/kWh,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].offpeakUsage) + ",kWh,,,"
		            				+ df.format(UI.cost.costsPerMonth[i][j].offpeakEnergyCharge));
		            		fw.newLine();
	            		}
	            		fw.write("Demand Critical Peak,,"
	            				+ df.format(UI.networkParameters.parameters[i].demandCriticalPeakRate)
	            				+ ",$/kVA/Mth,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].demandCriticalPeakUsage)+",kVA,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].demandCriticalPeakCharge));
	            		fw.newLine();
	            		fw.write("Demand Capacity,,"
	            				+ df.format(UI.networkParameters.parameters[i].demandCapacityRate)
	            				+ ",$/kVA/Mth,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].demandCapacityUsage)+",kVA,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].demandCapacityCharge));
	            		fw.newLine();
	            		fw.write("Market Charges");
	            		fw.newLine();
	            		fw.write("VEET Charge,"+df.format(UI.networkParameters.parameters[j].veetRate)+","
	            				+ df.format(UI.networkParameters.parameters[j].veetRate + UI.networkParameters.parameters[j].veetLossRatio*UI.networkParameters.parameters[j].veetRate)+ ",c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed) + ",kWh,"
	            				+ df.format(UI.networkParameters.parameters[j].veetLossRatio) + ','
	            				+ df.format(UI.networkParameters.parameters[j].veetLossRatio*100) + ','
	            				+ df.format(UI.cost.costsPerMonth[i][j].veetCharge));
	            		fw.newLine();
	            		fw.write("SRES Charge,"+df.format(UI.networkParameters.parameters[j].sresRate)+","
	            				+ df.format(UI.networkParameters.parameters[j].sresRate + UI.networkParameters.parameters[j].sresLossRatio*UI.networkParameters.parameters[j].sresRate)+ ",c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed) + ",kWh,"
	            				+ df.format(UI.networkParameters.parameters[j].sresLossRatio) + ','
	            				+ df.format(UI.networkParameters.parameters[j].sresLossRatio*100) + ','
	            				+ df.format(UI.cost.costsPerMonth[i][j].sresCharge));
	            		fw.newLine();
	            		fw.write("LRET Charge,"+df.format(UI.networkParameters.parameters[j].lretRate)+","
	            				+ df.format(UI.networkParameters.parameters[j].lretRate + UI.networkParameters.parameters[j].lretLossRatio*UI.networkParameters.parameters[j].lretRate)+ ",c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed) + ",kWh,"
	            				+ df.format(UI.networkParameters.parameters[j].lretLossRatio) + ','
	            				+ df.format(UI.networkParameters.parameters[j].lretLossRatio*100) + ','
	            				+ df.format(UI.cost.costsPerMonth[i][j].lretCharge));
	            		fw.newLine();
	            		fw.write("AEMO Pool Charge (GST Exempt)+AEMO RERT,"+df.format(UI.networkParameters.parameters[j].aemoPoolRertRate)+","
	            				+ df.format(UI.networkParameters.parameters[j].aemoPoolRertRate + UI.networkParameters.parameters[j].aemoPoolRertLossRatio*UI.networkParameters.parameters[j].aemoPoolRertRate)+ ",c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed) + ",kWh,"
	            				+ df.format(UI.networkParameters.parameters[j].aemoPoolRertLossRatio) + ','
	            				+ df.format(UI.networkParameters.parameters[j].aemoPoolRertLossRatio*100) + ','
	            				+ df.format(UI.cost.costsPerMonth[i][j].aemoPoolRertCharge));
	            		fw.newLine();
	            		fw.write("Ancillary Services,"+df.format(UI.networkParameters.parameters[j].ancilliaryServicesRate)+","
	            				+ df.format(UI.networkParameters.parameters[j].ancilliaryServicesRate + UI.networkParameters.parameters[j].ancilliaryServicesLossRatio*UI.networkParameters.parameters[j].ancilliaryServicesRate)+ ",c/kWh,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].monthlyNettGridUsed) + ",kWh,"
	            				+ df.format(UI.networkParameters.parameters[j].ancilliaryServicesLossRatio) + ','
	            				+ df.format(UI.networkParameters.parameters[j].ancilliaryServicesLossRatio*100) + ','
	            				+ df.format(UI.cost.costsPerMonth[i][j].ancilliaryServicesCharge));
	            		fw.newLine();
	            		fw.write("Other Charges");
	            		fw.newLine();
	            		fw.write("Meter Charge,,"+df.format(UI.networkParameters.parameters[j].meterRate)+",$/Yr,"
	            				+ UI.cost.costsPerMonth[i][j].daysInMonth + ",days,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].meterCharge));
	            		fw.newLine();
	            		fw.write("CT Compliance Testing Levy,,"+df.format(UI.networkParameters.parameters[j].ctComplianceTestingRate)+",$/Yr,"
	            				+ UI.cost.costsPerMonth[i][j].daysInMonth + ",days,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].ctComplianceTestingLevy));
	            		fw.newLine();
	            		fw.write("Total (Ex GST),,,,,,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].totalChargeExGST));
	            		fw.newLine();
	            		fw.write("GST,,,,,,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].gst));
	            		fw.newLine();
	            		fw.write("Total (Inc GST),,,,,,,,"
	            				+ df.format(UI.cost.costsPerMonth[i][j].totalChargeIncGST));
	            		fw.newLine();
	            	}
	            }
	        } catch (Exception ex) {
		    	//Need to display a warning dialog with the error.
		    	JOptionPane.showMessageDialog(UI,ex.getMessage(),"CEREI Error",JOptionPane.ERROR_MESSAGE); //
	        }
	    }
	}

}
