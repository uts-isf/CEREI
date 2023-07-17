package au.org.nifpi.cerei;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * Simple button that triggers reset of input files.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class ResetButton extends JButton implements ActionListener {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Link back to the overall UI to get some titles and centre any warnings. */
	private CostEffectiveRenewableEnergyInvestments UI;
	
	/**
	 * Constructor sets up the preferred button size
	 * 
	 * @param _UI Link back to the overall UI to get some titles and centre any warnings.
	 */
	public ResetButton(CostEffectiveRenewableEnergyInvestments _UI) {
		UI = _UI;
		setText("Reset Input Files");
		setFont(new Font(Font.SANS_SERIF,Font.PLAIN,16));
		setPreferredSize(new Dimension(CostEffectiveRenewableEnergyInvestments.BUTTON_WIDTH,CostEffectiveRenewableEnergyInvestments.BUTTON_HEIGHT*2));
		setForeground(Color.RED);
		setBorder(BorderFactory.createLineBorder(Color.RED,1));
		addActionListener(this);
	}

	/**
	 * Perform the resets when button is clicked.
	 * 
	 * @param e Button click.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// Reset the input files to null.
		UI.networkParameterFile.resetInputFile();
		UI.usageFile.resetInputFile();
		UI.priceFile.resetInputFile();
		UI.generatedFile.resetInputFile();
		UI.feedInFile.resetInputFile();
		UI.bauFile.resetInputFile();
		UI.lifecycleFile.resetInputFile();
		
		// Clear out the result tables.  They will refill during calculations
		UI.costResults.clearResultTable();
		UI.peiResults.clearResultTable();
		UI.savingsResults.clearResultTable();
		UI.lifecycleResults.clearResultTable();
	}

}
