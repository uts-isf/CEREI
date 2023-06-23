package au.org.nifpi.cerei;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;

/**
 * Simple button that triggers Calculation
 * 
 * @author James Sargeant
 */
public class GoButton extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public GoButton(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Calculate");
		setFont(new Font(Font.SANS_SERIF,Font.PLAIN,16));
		setPreferredSize(new Dimension(CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH,CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT*2));
		setForeground(Color.BLUE);
		setBorder(BorderFactory.createLineBorder(Color.BLUE,1));
		addActionListener(this);
	}

	/**
	 * Perform the calculations when button is clicked.
	 * 
	 * @param e Button click.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Calculate button has been clicked.
		
		// Make some sense of the various input combinations.  
		boolean showErrorDialog=false;
		String errorDialog = "";
		
		//If there are no files selected, warn the user
		if (UI.usageFile.inputFile==null && UI.priceFile.inputFile==null
				&& UI.generatedFile.inputFile==null && UI.feedInFile.inputFile==null
				&& UI.bauFile.inputFile == null && UI.lifecycleFile.inputFile == null ) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select some files to use in calculations");
		}
		
		//If any one of usage, spot price, generated or feed-in tarrif file name is set, make sure the network parameter is also set  
		if ((UI.usageFile.inputFile!=null || UI.priceFile.inputFile!=null || UI.generatedFile.inputFile!=null && UI.feedInFile.inputFile!=null) 
				&& UI.networkParameterFile.inputFile == null) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select a Network Parameter File\n");
		}

		// Make sure we have pairs, alert the user if there are no pairs.
		if (UI.usageFile.inputFile!=null && UI.priceFile.inputFile==null) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select an AEMO Spot Price file\n");
		}
		if (UI.usageFile.inputFile==null && UI.priceFile.inputFile!=null) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select an Energy Usage file\n");
		}
		if (UI.generatedFile.inputFile!=null && UI.feedInFile.inputFile==null) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select an Feed-in Tariff file\n");
		}
		if (UI.generatedFile.inputFile==null && UI.feedInFile.inputFile!=null) {
			showErrorDialog=true;
			errorDialog = errorDialog.concat("Please select an Generated Energy file\n");
		}
		// All the errors have been shown.
	
		if (showErrorDialog) {
			JOptionPane.showMessageDialog(UI,errorDialog,"Engery Calculator Error",JOptionPane.ERROR_MESSAGE); 
			return;
		}

		UI.calculate();
	}

}
