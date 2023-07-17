package au.org.nifpi.cerei;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

/**
 * Cost-Effective Renewable Energy Investments --- Program to process and display Electrical Energy costs,
 * efficiencies, savings and return on investment.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class CostEffectiveRenewableEnergyInvestments extends JFrame {
	/** Serialised Version ID.  For if this class ever needs to be serialised - unlikely*/
	private static final long serialVersionUID = 2023062301L;

	/** Initial width of the GUI */
	public static final int CALCULATOR_WIDTH = 1000;
	/** Initial height of the GUI */
	public static final int CALCULATOR_HEIGHT = 740;
	/** Default width of buttons */
	public static final int BUTTON_WIDTH = 220;
	/** Default height of buttons */
	public static final int BUTTON_HEIGHT = 40;

	/** Input files */
	protected InputFile networkParameterFile, usageFile, priceFile, generatedFile, bauFile, feedInFile, lifecycleFile;

	/**
	 * General Parameters - Holds all parameters gathered from Input files, except
	 * 30 minute spot prices and usages
	 */
	protected NetworkParameter networkParameters;

	/** Cost information for all meters and all months. */
	protected Cost cost = null;

	/** PEI information for all meters and all months. */
	protected PEI pei = null;

	/** Savings information for all meters and all months */
	protected BusinessAsUsual bau = null;

	/** Lifecycle Cost Analysis information for all meters and all months */
	protected LifecycleCostAnalysis lifecylce = null;
	/**
	 * The year for which the data is valid - obtained from the first date in the
	 * usage file.
	 */
	private int year = -1;

	/** Result panels that go into the Result tabs */
	protected ResultsPanel costResults, peiResults, savingsResults, lifecycleResults;

	/** Holds the Jtabbed pane that has all the outcomes of the various calculations. */
	private JPanel results;

	/**
	 * The list of meterNames in the order they appear in the usage file followed by
	 * unknown meters in the generated file (in that order)
	 */
	List<String> meterNames = null;
	
	/** The list of meterNames in the order they appear in the usage file. */
	List<String> usageMeterNames = null;

	/** The list of meterNames in the order they appear in the generated file. */
	List<String> generatedMeterNames = null;

	/** Maps the generated energy meter index to the usage meter index */
	private HashMap<Integer, Integer> generatedMeterMap = null;
	/** Size of the hash map that maps the generated energy meter index to the usage meter index - initially zero */
	private int hashMapSize = 0;

	/** Save cost summary to .csv file */
	private SaveCostSummary saveCostSummary;
	/** Save cost details to .csv file */
	private SaveCostDetails saveCostDetails;
	/** Save Price Efficiency Index summary to .csv file */
	private SavePeiSummary savePeiSummary;
	/** Save Savings summary to .csv file */
	private SaveSavingsSummary saveSavingsSummary;
	/** Save LifeCylce Cost Analysis summary to .csv file */
	private SaveLifecycleSummary saveLifecycleSummary;
	/** Save LifeCylce Cost Analysis details to .csv file */
	private SaveLifecycleDetails saveLifecycleDetails;

	/** Parse the many and varied date time formats */
	private DateParser dateParser;

	/**
	 * Entry point into program
	 * 
	 * @param args A string array containing the command line arguments - none used.
	 */
	public static void main(String[] args) {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		new CostEffectiveRenewableEnergyInvestments();
	}

	/**
	 * GUI for Energy Calculator - setup and layout of GUI
	 */
	public CostEffectiveRenewableEnergyInvestments() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("CEREI");
		setPreferredSize(new Dimension(CALCULATOR_WIDTH, CALCULATOR_HEIGHT));
		
		MenuBar menubar = new MenuBar(this);
		setJMenuBar(menubar);

		// Create the DateParser that is used extensively when processing input files.
		dateParser = new DateParser();

		// Create the buttons used to nominate the various input files.
		networkParameterFile = new InputFile("Network Tariff", this);
		usageFile = new InputFile("Energy Usage", this);
		priceFile = new InputFile("AEMO Spot Price", this);
		generatedFile = new InputFile("Energy Generated", this);
		feedInFile = new InputFile("Feed-in Tariff", this);
		bauFile = new InputFile("Business-as-Usual Bill", this);
		lifecycleFile = new InputFile("Life-cycle Cost Parameters", this);

		// filePanel is used to hold the buttons to open the various input files
		JPanel filePanel;
		// Add the button to filePanel
		filePanel = createInputFilePanel(networkParameterFile, usageFile, priceFile, generatedFile, feedInFile, bauFile,
				lifecycleFile);

		// goPanel holds the "Calculate" button, which does all the work.
		JPanel goPanel = new JPanel();
		GoButton go = new GoButton(this);
		ResetButton reset = new ResetButton(this);
		goPanel.add(go);
		goPanel.add(reset);

		// results holds all the results of the calculations performed by the go button.
		results = new JPanel(new BorderLayout());

		// Each result section gets its own tab on the results panel.
		JTabbedPane resultTablePane = new JTabbedPane();

		// Create the result panels that go into the tabs.
		costResults = new ResultsPanel("Energy Bill");
		peiResults = new ResultsPanel("Price Efficiency Index (PEI)");
		savingsResults = new ResultsPanel("Potential Saving");
		lifecycleResults = new ResultsPanel("Life-cycle Cost Assessment");

		// Buttons to save various panes to .csv files.
		saveCostSummary = new SaveCostSummary(this);
		savePeiSummary = new SavePeiSummary(this);
		saveCostDetails = new SaveCostDetails(this);
		saveSavingsSummary = new SaveSavingsSummary(this);
		saveLifecycleSummary = new SaveLifecycleSummary(this);
		saveLifecycleDetails = new SaveLifecycleDetails(this);

		// Add save buttons to the appropriate panels
		costResults.resultButtonPanel.add(saveCostSummary);
		costResults.resultButtonPanel.add(saveCostDetails);

		peiResults.resultButtonPanel.add(savePeiSummary);

		savingsResults.resultButtonPanel.add(saveSavingsSummary);

		lifecycleResults.resultButtonPanel.add(saveLifecycleSummary);
		lifecycleResults.resultButtonPanel.add(saveLifecycleDetails);

		// Add the results panels to the tabs.
		resultTablePane.add(costResults.resultPanelName, costResults);
		resultTablePane.add(peiResults.resultPanelName, peiResults);
		resultTablePane.add(savingsResults.resultPanelName, savingsResults);
		resultTablePane.add(lifecycleResults.resultPanelName, lifecycleResults);

		// Add the tabbed pane to ther results panel
		results.add(resultTablePane, BorderLayout.CENTER);

		// Add all the top level panels to make the GUI
		this.add(filePanel, BorderLayout.NORTH);
		this.add(goPanel, BorderLayout.CENTER);
		this.add(results, BorderLayout.SOUTH);

		pack();
		setVisible(true);
	}

	/**
	 * Top level method for calculating and displaying costs, price efficiency
	 * indexes, savings and return on investment
	 * 
	 * TO DO - This is now way to long.  Needs some serious refactoring 
	 * 
	 */
	public void calculate() {
		boolean bauError = false, lifecycleError = false;
		
		// Make sure all the "Save" buttons are disabled. They will be enabled
		// individually if the relevant
		// calculations are successful.
		saveCostSummary.setEnabled(false);
		savePeiSummary.setEnabled(false);
		saveCostDetails.setEnabled(false);
		saveSavingsSummary.setEnabled(false);
		saveLifecycleSummary.setEnabled(false);
		saveLifecycleDetails.setEnabled(false);

		// null out all the data structures. They will refill during calculations
		networkParameters = null;
		meterNames = null;
		cost = null;
		pei = null;
		bau = null;
		lifecylce = null;

		// Clear out the result tables. They will refill during calculations
		costResults.clearResultTable();
		peiResults.clearResultTable();
		savingsResults.clearResultTable();
		lifecycleResults.clearResultTable();

		// First try block is about reading in the network parameters and making sure
		// that they are good
		if (networkParameterFile.inputFile != null) {
			try {
				networkParameters = new NetworkParameter(networkParameterFile);
			} catch (Exception e) {
				// Display a warning dialog with the error and give up.
				costResults.resultPanelText.setText("Unable to complete calculations");
				peiResults.resultPanelText.setText("Unable to complete calculations");
				savingsResults.resultPanelText.setText("Unable to complete calculations");
				lifecycleResults.resultPanelText.setText("Unable to complete calculations");
				results.revalidate();
				results.repaint();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error reading Network Parameter file",
						JOptionPane.ERROR_MESSAGE); //
				return;
			}
		}
		
		// Added this section of code to distribute generation acrosss usage meters.  Done this way to avoid confusing hacks to previous blocks - even though
		// there is some code duplication
		// Preconditions for distributing energy:  there *must* be usage files, there *must* be generation files and "distribute energy" from Parameter file must be true.
		if (usageFile.inputFile != null && priceFile.inputFile != null && generatedFile.inputFile != null
				&& feedInFile.inputFile != null && networkParameters.distributeGeneration) {
			try {
				meterNames = getUsageMeterNames(usageFile);
				usageMeterNames = getUsageMeterNames(usageFile);
				generatedMeterNames = getGeneratedMeterNames(generatedFile);
			} catch (Exception e) {
				// Display a warning dialog with the error and give up.
				costResults.resultPanelText.setText("Unable to complete calculations");
				peiResults.resultPanelText.setText("Unable to complete calculations");
				savingsResults.resultPanelText.setText("Unable to complete calculations");
				lifecycleResults.resultPanelText.setText("Unable to complete calculations");
				results.revalidate();
				results.repaint();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error reading meter names",
						JOptionPane.ERROR_MESSAGE); //
				return;
			}
			// if distribution meters weren't named in network parameter file then distribution meters will consist of all meters.
			if (networkParameters.distributionMeters.size() == 0) {
				for (int i=0; i<meterNames.size(); i++) {
					networkParameters.distributionMeters.add(meterNames.get(i));
				}
			}
			// If there was a list in the network parameter file, validate that list against actual usage meters.
			else {
				for (int i = 0; i < networkParameters.distributionMeters.size(); i++) {
					if (!meterNames.contains(networkParameters.distributionMeters.get(i))) {
						costResults.resultPanelText.setText("Unable to complete calculations");
						peiResults.resultPanelText.setText("Unable to complete calculations");
						savingsResults.resultPanelText.setText("Unable to complete calculations");
						lifecycleResults.resultPanelText.setText("Unable to complete calculations");
						results.revalidate();
						results.repaint();
						JOptionPane.showMessageDialog(this, "Not all named energy distribtuion meters in Tariff Parameter File exist", "Energy Distribution across multiple meters",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			// if there is a bau file and we don't have a specified list of distribution meters then we need to reorder our distribution meters so they run from most costly to least costly.
			if (bauFile.inputFile != null && !networkParameters.isSpecifiedDistributionList()) {
				// Load up the bau object from the file.  Yes - this is duplicated!
				try {
					bau = new BusinessAsUsual(bauFile);
				}
				catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error while reading Business as Usual File",
							JOptionPane.ERROR_MESSAGE);
					return;
				}

				// Create a Map containing all the meters in the bau file (with total costs) provided those meters are present in the distribution meter list.
				// Really messy code - probably should be moved to BusinessAsUsual or NetworkParameters.
				Map<String, Double> map = new HashMap<>();
				for (int i=0; i < bau.loadedMeterSummaryArray.length; i++) {
					if(networkParameters.distributionMeters.contains(bau.loadedMeterSummaryArray[i].meterName)) {
						map.put(bau.loadedMeterSummaryArray[i].meterName, bau.loadedMeterSummaryArray[i].yearly);
					}
				}
				List<Entry<String, Double>> nlist = new ArrayList<>(map.entrySet());
				nlist.sort (Entry.comparingByValue(Comparator.reverseOrder()));
				//Extract the list of meter names (in descending order)
				ArrayList<String> orderedDistributionMeters = new ArrayList<String>();
				for (int i = 0; i < nlist.size(); i++) {
					orderedDistributionMeters.add(nlist.get(i).getKey());
				}
				// Now we have a list of real bau meter names in order of descending yearly total costs.  Update the distributionMeters list.
				// Add any distribution meters that were not in the bau file to the end of that list.
				for (int i=0; i<networkParameters.distributionMeters.size(); i++) {
					if(!orderedDistributionMeters.contains(networkParameters.distributionMeters.get(i))) {
						orderedDistributionMeters.add(networkParameters.distributionMeters.get(i));
					}
				}
				// The sorted list of distribution meters!
				networkParameters.distributionMeters = orderedDistributionMeters;
			}
			try {

				// create the cost data structures and load with information from usage and
				// generated files
				cost = new Cost(meterNames, year);
				// add in the monthly meter costs from the parameter data structure
				cost.addMonthlyParameters(networkParameters);

				// If we have usage file we need to calculate PEIs.
				if (usageFile.inputFile != null && priceFile.inputFile != null) {
					pei = new PEI(meterNames, year);
					pei.addMonthlyParameters(networkParameters);
				}

				// 30 minute by 30 minute accumulation of usage, price and PEI data
				processUsageAndPrice(networkParameters, usageFile, priceFile, generatedFile, feedInFile);

				// Calculate monthly results once all data is processed.
				cost.calculateCosts(networkParameters);
				if (pei != null) {
					pei.calculatePEIs();
				}

				// Start preparing output
				// Costs.
				CEREITableModel costTableModel = cost
						.createSummaryCostTableModel();
				costResults.resultPanelText.setText("All values in $ (Positive $ values indicate Cost and Negative $ values indicate Credit)");
				costResults.resultTable.setModel(costTableModel);
				costResults.formatResultTable();
				saveCostSummary.setEnabled(true);
				saveCostDetails.setEnabled(true);

				// PEIs.
				if (pei != null) {
					CEREITableModel peiTableModel = pei
							.createSummaryPEITableModel();
					peiResults.resultPanelText.setText("PEI value > 1 indicates higher operating cost and PEI < 1 indicates the effective economic operation");
					peiResults.resultTable.setModel(peiTableModel);
					peiResults.formatResultTable();
					savePeiSummary.setEnabled(true);
				} else {
					peiResults.resultPanelText.setText("Please select files");
				}

			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error while calculating Costs and PEIs",
						JOptionPane.ERROR_MESSAGE); //
				costResults.resultPanelText.setText("Unable to complete calculations");
				peiResults.resultPanelText.setText("Unable to complete calculations");
				savingsResults.resultPanelText.setText("Unable to complete calculations");
				lifecycleResults.resultPanelText.setText("Unable to complete calculations");
				results.revalidate();
				results.repaint();
				return;
			}
		} 
		else {
			// End of added code - back to no distribution of generated energy across
			// multiple meters.

			// Process costs and PEIs provided that we have loaded Network Parameters and that there is
			// at least one pair of usage and price files
			if (networkParameters != null && (usageFile.inputFile != null && priceFile.inputFile != null)
					|| (generatedFile.inputFile != null && feedInFile.inputFile != null)) {

				// Process costs an PEIs. If something goes wrong don't show the output.
				// network parameters OK. Now try to get some sensible meter names.
				try {
					meterNames = getMeterNames(usageFile, generatedFile);
					if (usageFile.inputFile != null && priceFile.inputFile != null) {
						usageMeterNames = getUsageMeterNames(usageFile);
					}
					if (generatedFile.inputFile != null && feedInFile.inputFile != null) {
						generatedMeterNames = getGeneratedMeterNames(generatedFile);
					}

				} catch (Exception e) {
					// Display a warning dialog with the error and give up.
					costResults.resultPanelText.setText("Unable to complete calculations");
					peiResults.resultPanelText.setText("Unable to complete calculations");
					savingsResults.resultPanelText.setText("Unable to complete calculations");
					lifecycleResults.resultPanelText.setText("Unable to complete calculations");
					results.revalidate();
					results.repaint();
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error reconciling meter names",
							JOptionPane.ERROR_MESSAGE); //
					return;
				}
				try {

					// create the cost data structures and load with information from usage and
					// generated files
					cost = new Cost(meterNames, year);
					// add in the monthly meter costs from the parameter data structure
					cost.addMonthlyParameters(networkParameters);

					// If we have usage file we need to calculate PEIs.
					if (usageFile.inputFile != null && priceFile.inputFile != null) {
						pei = new PEI(usageMeterNames, year);
						pei.addMonthlyParameters(networkParameters);
					}

					// 30 minute by 30 minute accumulation of usage, price and PEI data
					processUsageAndPrice(networkParameters, usageFile, priceFile, generatedFile, feedInFile);

					// Calculate monthly results once all data is processed.
					cost.calculateCosts(networkParameters);
					if (pei != null) {
						pei.calculatePEIs();
					}

					// Start preparing output
					// Costs.
					CEREITableModel costTableModel = cost
							.createSummaryCostTableModel();
					costResults.resultPanelText.setText("All values in $ (Positive $ values indicate Cost and Negative $ values indicate Credit)");
					costResults.resultTable.setModel(costTableModel);
					costResults.formatResultTable();
					saveCostSummary.setEnabled(true);
					saveCostDetails.setEnabled(true);

					// PEIs.
					if (pei != null) {
						CEREITableModel peiTableModel = pei
								.createSummaryPEITableModel();
						peiResults.resultPanelText.setText("PEI value > 1 indicates higher operating cost and PEI < 1 indicates the effective economic operation");
						peiResults.resultTable.setModel(peiTableModel);
						peiResults.formatResultTable();
						savePeiSummary.setEnabled(true);
					} else {
						peiResults.resultPanelText.setText("Please select files");
					}

				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error while calculating Costs and PEIs",
							JOptionPane.ERROR_MESSAGE); //
					costResults.resultPanelText.setText("Unable to complete calculations");
					peiResults.resultPanelText.setText("Unable to complete calculations");
					savingsResults.resultPanelText.setText("Unable to complete calculations");
					lifecycleResults.resultPanelText.setText("Unable to complete calculations");
					results.revalidate();
					results.repaint();
					return;
				}
			}
		}
		
		// If there is a previous bill file opened then process it.
		if (bauFile.inputFile != null) {
			try {
				bau = new BusinessAsUsual(bauFile); 
				CEREITableModel savingsTableModel = bau.createSavingCostTableModel(cost);
				savingsResults.resultPanelText.setText("All values in $ (Positive $ values indicate Saving and Negative $ values indicate extra Cost over the BAU Energy Bill)");
				savingsResults.resultTable.setModel(savingsTableModel);
				savingsResults.formatResultTable();
				saveSavingsSummary.setEnabled(true);
			} catch (Exception e) {
				// When something goes wrong - most likely a missing or poorly formatted file.
				bauError = true;
				savingsResults.resultPanelText.setText("Unable to complete calculations");
				results.revalidate();
				results.repaint();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error while creating Cost Savings",
						JOptionPane.ERROR_MESSAGE); //
				savingsResults.resultPanelText.setText("Unable to complete calculations");
			}
		}
		else {
			savingsResults.resultPanelText.setText("Please select files");
		}

		// If there is a lifecycle cost analysis input file set up then process it.
		if (lifecycleFile.inputFile != null) {
			try {
				lifecylce = new LifecycleCostAnalysis(lifecycleFile);
				lifecylce.calculate(bau, cost);
				CEREITableModel lifecycleCostTableModel = lifecylce
						.createLifecycleCostSummaryTableModel(lifecycleResults.resultTable, lifecylce);
				lifecycleResults.resultPanelText.setText("Negative $ values indicate Cost and Positive $ values indicate Revenue\r\n");
				lifecycleResults.resultTable.setModel(lifecycleCostTableModel);
				saveLifecycleSummary.setEnabled(true);
				saveLifecycleDetails.setEnabled(true);
			} catch (Exception e) {
				// When something goes wrong - most likely a missing or poorly formatted file.
				lifecycleError = true;
				lifecycleResults.resultPanelText.setText("Unable to complete calculations");
				results.revalidate();
				results.repaint();
				JOptionPane.showMessageDialog(this, e.getMessage(), "Error while Calculating Lifecycle Costs",
						JOptionPane.ERROR_MESSAGE); //
				lifecycleResults.resultPanelText.setText("Unable to complete calculations");
			}

		}
		else {
			lifecycleResults.resultPanelText.setText("Please select files");
		}

		// Display
		results.revalidate();
		results.repaint();
		
		// Let the user know we're done
		if( !bauError && !lifecycleError) {
			JOptionPane.showMessageDialog(this,"Calculations Complete.\nResults in relevant tabs","CEREI",JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			String completionString = "Calculations Complete\n";
			if (bauError) {
				completionString = completionString.concat("Errors in Savings calculations\n");
			}
			if (lifecycleError) {
				completionString = completionString.concat("Errors in Lifecycle cost calculations\n");
			}
			completionString = completionString.concat("Other results in relevant tabs");
			JOptionPane.showMessageDialog(this,completionString,"CEREI",JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Read the various energy used and energy generated input files line by line and accumulate all the relevant data in the Cost and PEI objects   
	 * @param networkParameters Energy charge, Network charge, Spot Energy, Market charge and Other Charge Loss ratios and pricing. 
	 * @param usageFile Energy Used file
	 * @param priceFile AEMO Spot Price file
	 * @param generatedFile Energy Generated file
	 * @param feedInFile Feed-In Tariff file
	 * 
	 * @throws Exception If one of the files in the pairs of "Energy Used" or  "Energy Generated" input files are empty.  Rethrows an exception from any called method. 
	 */
	private void processUsageAndPrice(NetworkParameter networkParameters, InputFile usageFile, InputFile priceFile,
			InputFile generatedFile, InputFile feedInFile) throws Exception {
		String usageString=null, priceString=null, generatedString=null, feedInString=null;
		String usageSt[], priceSt[], generatedSt[], feedInSt[];
		int lineNumber = 0;

		// Get ready to read files line by line
		try (BufferedReader usageReader = usageFile.inputFile == null ? null
				: new BufferedReader(new InputStreamReader(new FileInputStream(usageFile.inputFile)), 32768);
				BufferedReader priceReader = priceFile.inputFile == null ? null
						: new BufferedReader(new InputStreamReader(new FileInputStream(priceFile.inputFile)), 32768);
				BufferedReader generatedReader = generatedFile.inputFile == null ? null
						: new BufferedReader(new InputStreamReader(new FileInputStream(generatedFile.inputFile)),
								32768);
				BufferedReader feedInReader = feedInFile.inputFile == null ? null
						: new BufferedReader(new InputStreamReader(new FileInputStream(feedInFile.inputFile)), 32768)) {

			if (usageReader != null && priceReader != null) {
				// discard the first line of the usage file - which is header information
				if ((usageString = usageReader.readLine()) == null) {
					throw new Exception("Energy Generated file is empty");
				}

				// discard the first line of the AEMO Spot Price file - which is header
				// information
				if ((priceString = priceReader.readLine()) == null) {
					throw new Exception("AEMO Spot Price file is empty");
				}
			}

			if (generatedReader != null && feedInReader != null) {
				// discard the first line of the Generated Energy file - which is header
				// information
				if ((generatedString = generatedReader.readLine()) == null) {
					throw new Exception("Generated Energy file is empty");
				}

				// discard the first line of the Feed-in Tariff file - which is header
				// information
				if ((feedInString = feedInReader.readLine()) == null) {
					throw new Exception("Feed-in Tariff file is empty");
				}
			}

			lineNumber++; // Keep track of the line we are reading so we can report the location of any
							// date mismatch

			// This is a bit ugly and could probably be improved, but for now separated
			// while loops depending on whether
			// there is just energy usage, just generated usage, or both energy usage and
			// energy generated.
			
			//Usage and Generation
			if (usageReader != null && priceReader != null && generatedReader != null && feedInReader != null) {
				// Seriously ugly in order to get end of file validation to work
				usageString = usageReader.readLine();
				priceString = priceReader.readLine();
				generatedString = generatedReader.readLine();
				feedInString = feedInReader.readLine();
				while (usageString != null && priceString != null && generatedString != null && feedInString != null) {
					usageSt = usageString.split(",");
					priceSt = priceString.split(",");
					generatedSt = generatedString.split(",");
					feedInSt = feedInString.split(",");
					lineNumber++;
					
					/* Make sure that the lines read in from the files contain valid data */
					validateLineFromFile(usageSt, usageMeterNames.size()+1, usageFile.inputFile.getName(), lineNumber);
					validateLineFromFile(priceSt, 2, priceFile.inputFile.getName(), lineNumber);
					validateLineFromFile(generatedSt, generatedMeterNames.size()+1, generatedFile.inputFile.getName(), lineNumber);
					validateLineFromFile(feedInSt, 2, feedInFile.inputFile.getName(), lineNumber);
					
					/*
					// Debug = stop at a particular line
					if (lineNumber == 254) {
						int w=0;
					}
					*/
					
					// First field is the date.
					String usageDateTimeString = usageSt[0];
					String priceDateTimeString = priceSt[0];
					String generatedDateTimeString = generatedSt[0];
					String feedInDateTimeString = feedInSt[0];

					// Make sure the dates match - validateDateTimeStrings will throw an exception
					// if they don't all match. Otherwise get the common date and time for the lines
					LocalDateTime recordDateTime = validateDateTimeStrings(usageDateTimeString, priceDateTimeString,
							generatedDateTimeString, feedInDateTimeString, lineNumber);

					// Subtract 30 minutes from the time in the input files as the period refers to
					// the previous 30 minutes
					recordDateTime = recordDateTime.minusMinutes(30);
					// Accumulate usage and charges for cost and PEI calculations
					cost.addCharges(networkParameters, recordDateTime, usageSt, priceSt, generatedSt, feedInSt,
							generatedMeterMap);
					pei.addCharges(networkParameters, recordDateTime, usageSt, priceSt, generatedSt, feedInSt,
							generatedMeterMap);
					
					// More ugliness to make end of file validation checks work
					usageString = usageReader.readLine();
					priceString = priceReader.readLine();
					generatedString = generatedReader.readLine();
					feedInString = feedInReader.readLine();
				}
				 
			//Usage Only
			} else if (usageReader != null && priceReader != null && generatedReader == null && feedInReader == null) {
				// Seriously ugly in order to get end of file validation to work
				usageString = usageReader.readLine();
				priceString = priceReader.readLine();
				while (usageString != null && priceString != null) {
					usageSt = usageString.split(",");
					priceSt = priceString.split(",");
					lineNumber++;

					/* Make sure that the lines read in from the files contain valid data */
					validateLineFromFile(usageSt, usageMeterNames.size()+1, usageFile.inputFile.getName(), lineNumber);
					validateLineFromFile(priceSt, 2, priceFile.inputFile.getName(), lineNumber);

					/*
					// Debug = stop at a particular line
					if (lineNumber == 254) {
						int w=0;
					}
					*/
					// First field is the date.
					String usageDateTimeString = usageSt[0];
					String priceDateTimeString = priceSt[0];

					// Make sure the dates match - validateDateTimeStrings will throw an exception
					// if they don't all match. Otherwise get the common date and time for the lines
					LocalDateTime recordDateTime = validateDateTimeStrings(usageDateTimeString, priceDateTimeString,
							usageDateTimeString, priceDateTimeString, lineNumber);

					// Subtract 30 minutes from the time in the input files as the period refers to
					// the previous 30 minutes
					recordDateTime = recordDateTime.minusMinutes(30);
					// Accumulate usage and charges for cost and PEI calculations
					cost.addCharges(networkParameters, recordDateTime, usageSt, priceSt, null, null, null);
					pei.addCharges(networkParameters, recordDateTime, usageSt, priceSt, null, null, null);
					
					// More ugliness to make end of file validation checks work
					usageString = usageReader.readLine();
					priceString = priceReader.readLine();
				}

			//Generation Only
			} else if (usageReader == null && priceReader == null && generatedReader != null && feedInReader != null) {
				generatedString = generatedReader.readLine();
				feedInString = feedInReader.readLine();
				while (generatedString != null && feedInString != null) {
					generatedSt = generatedString.split(",");
					feedInSt = feedInString.split(",");
					lineNumber++;

					/* Make sure that the lines read in from the files contain valid data */
					validateLineFromFile(generatedSt, generatedMeterNames.size()+1, generatedFile.inputFile.getName(), lineNumber);
					validateLineFromFile(feedInSt, 2, feedInFile.inputFile.getName(), lineNumber);

					// First field is the date.
					String generatedDateTimeString = generatedSt[0];
					String feedInDateTimeString = feedInSt[0];

					/*
					// Debug = stop at a particular line
					if (lineNumber == 254) {
						int w=0;
					}
					*/
					
					// Make sure the dates match - validateDateTimeStrings will throw an exception
					// if they don't all match. Otherwise get the common date and time for the lines
					LocalDateTime recordDateTime = validateDateTimeStrings(generatedDateTimeString,
							feedInDateTimeString, generatedDateTimeString, feedInDateTimeString, lineNumber);

					// Subtract 30 minutes from the time in the input files as the period refers to
					// the previous 30 minutes
					recordDateTime = recordDateTime.minusMinutes(30);
					// Accumulate usage and charges for cost and PEI calculations
					cost.addCharges(networkParameters, recordDateTime, null, null, generatedSt, feedInSt,
							generatedMeterMap);

					// More ugliness to make end of file validation checks work
					generatedString = generatedReader.readLine();
					feedInString = feedInReader.readLine();
				}
			}
			else {
				// else this is an illegal combination - do nothing.
			}
			
			//Check if all open files went empty at the same time.  If they didn't, then at least one file had insufficient lines.
			if(usageString != null || priceString != null || generatedString != null || feedInString != null) {
				String warningString = "Warning: Processing finised early beacuse at least one input file had missing lines at the end of the file.\n";
				warningString += "Files with missing lines:\n";
				if (usageFile.inputFile != null && usageString == null) {
					warningString += "\t"+usageFile.inputFile.getName()+"\n";
				}
				if (priceFile.inputFile != null && priceString == null) {
					warningString += "\t"+priceFile.inputFile.getName()+"\n";
				}
				if (generatedFile.inputFile != null && generatedString == null) {
					warningString += "\t"+generatedFile.inputFile.getName()+"\n";
				}
				if (feedInFile.inputFile != null && feedInString == null) {
					warningString += "\t"+feedInFile.inputFile.getName()+"\n";
				}
				// Show the warning
				JOptionPane.showMessageDialog(this, warningString, "Possible missing lines in input files",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Validate that the line from the input file contains a date followed by floats.
	 * @param lineSt Line read in from the energy usage file, spot price file, generated energy file or feed-in tariff file
	 * @param expectedLength Expected number of tokens in the line read from the file.
	 * @param fileName File name used to report errors
	 * @param lineNumber Line number from file used to report any errors
	 * 
	 * @throws Exception Throws exception from called files or if values that should be floats are something else. 
	 */
	private void validateLineFromFile(String[] lineSt, int expectedLength, String fileName, int lineNumber) throws Exception{
		if (lineSt.length < expectedLength) {
			throw new Exception("Missing Data on line " + lineNumber + " of " + fileName);
		}
		if (lineSt.length > expectedLength) {
			throw new Exception("Extra Data on line " + lineNumber + " of " + fileName);
		}

		dateParser.parseDateTime(lineSt[0], fileName, lineNumber);
				
		for (int i=1; i < expectedLength; i ++) {
			try {
				Double.parseDouble(lineSt[i]);
			}
			catch (Exception e) {
				throw new Exception("Non-numerical data on line " + lineNumber + " column " + (i+1) + " of " + fileName);
			}
		}
		
	}

	/**
	 * Makes sure that the timestamps from Energy Used, AEMO Spot Price, Energy Generated and Feed-In Tariff all match.
	 *  
	 * @param usageDateTimeString Timestamp from Energy Used.
	 * @param priceDateTimeString Timestamp from AEMO Spot Price.
	 * @param generatedDateTimeString Timestamp from Energy Generated.
	 * @param feedInDateTimeString Timestamp from Feed-In Tariff.
	 * @param lineNumber Line number of the files currently being processed - used in an Exception if there is a mis-match.
	 * 
	 * @throws Exception If there is a mismatch or if a dateTimeString cannot be parsed.
	 * 
	 * @return dateTime in a form that is easy to process.
	 */
	private LocalDateTime validateDateTimeStrings(String usageDateTimeString, String priceDateTimeString,
			String generatedDateTimeString, String feedInDateTimeString, int lineNumber) throws Exception {

		LocalDateTime usageDateTime = dateParser.parseDateTime(usageDateTimeString, "Energy Usage File", lineNumber);
		LocalDateTime priceDateTime = dateParser.parseDateTime(priceDateTimeString, "Spot Price File", lineNumber);
		LocalDateTime generatedDateTime = dateParser.parseDateTime(generatedDateTimeString, "Energy Generated File",
				lineNumber);
		LocalDateTime feedInDateTime = dateParser.parseDateTime(feedInDateTimeString, "Feed-in Tariff File",
				lineNumber);
		// Make sure the dates are the same. Throw exception if they are not the same.
		if (!(usageDateTime.equals(priceDateTime) && usageDateTime.equals(generatedDateTime)
				&& usageDateTime.equals(feedInDateTime))) {
			throw new Exception("Missing data in Usage, Generated, Feed-in or Spot Price file at line " + lineNumber);
		}
		return usageDateTime;
	}
	
	/**
	 * Create and populate the JPanel that holds all the "file open" buttons.
	 * 
	 * @param networkParameterFile Network Tarrif
	 * @param usageFile Energy Usage
	 * @param priceFile AEMO SPot Price
	 * @param generatedFile Energy Generated
	 * @param feedInFile Fedd-In Tariif
	 * @param bauFile Business as Uusal
	 * @param lifecycleFile Lifecycle Cost Parameters
	 * 
	 * @return All the buttons and associated text for loading up the various input files. 
	 */
	private JPanel createInputFilePanel(InputFile networkParameterFile, InputFile usageFile, InputFile priceFile,
			InputFile generatedFile, InputFile feedInFile, InputFile bauFile, InputFile lifecycleFile) {
		JPanel inputFilePanel = new JPanel(new GridBagLayout());

		// Setting up the grid to be used to located the inputFiles
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 0, 5, 0); // Padding

		// Top row is the network parameter file
		c.gridwidth = 2;
		c.gridy = 0;
		c.gridx = 1;
		inputFilePanel.add(networkParameterFile, c);

		// Second row is the usage information
		c.gridwidth = 1;
		c.gridy = 1;
		c.gridx = 0;
		c.insets = new Insets(5, 15, 5, 0); // Padding
		inputFilePanel.add(usageFile, c); // AEMO spot price file
		c.gridx = 1;
		c.insets = new Insets(5, 0, 5, 15); // Put some space between "Usage" and "Generated"
		inputFilePanel.add(priceFile, c); // Generated energy file
		c.gridx = 2;
		c.insets = new Insets(5, 15, 5, 0); // Put some space between "Usage" and "Generated"
		inputFilePanel.add(generatedFile, c); // Energy usage file
		c.gridx = 3;
		c.insets = new Insets(5, 0, 5, 15); // Padding
		inputFilePanel.add(feedInFile, c); // Feed in price file

		// Thrid row is the Bau summary file and the lifecycle cost parameter file
		c.gridy = 2;
		c.gridx = 1;
		c.insets = new Insets(5, 0, 5, 0); // Padding
		inputFilePanel.add(bauFile, c); // Feed in price file
		c.gridx = 2;
		inputFilePanel.add(lifecycleFile, c); // Feed in price file

		return inputFilePanel;
	}

	/**
	 * Get all the unique meter names that appear in the Energy Used and Energy Generated input files.
	 * 
	 * @param usageFile Energy Used input file.
	 * @param generatedFile Energy Generated input file.
	 * 
	 * @throws Exception If "Energy Used" and "Energy Generated" input files are empty.  Rethrows an exception from any called method.
	 *  
	 * @return List of unique names of all the meters in the "Energy Used" and "Energy Generated" input files.
	 */
	private List<String> getMeterNames(InputFile usageFile, InputFile generatedFile) throws Exception {
		List<String> meterNames = new ArrayList<String>();
		String usageString, generatedString;
		String usageSt[], generatedSt[];
		int usageYear = -1, generatedYear = -1;
		int lineNumber = 0;

		// If there are no input files set then throw an error
		if (usageFile.inputFile == null && generatedFile.inputFile == null) {
			throw new Exception("No Usage File and no Generated File present");
		}
		try (BufferedReader usageReader = 
					usageFile.inputFile == null?null: new BufferedReader(new InputStreamReader(new FileInputStream(usageFile.inputFile)), 32768);
				BufferedReader generatedReader = 
					generatedFile.inputFile == null?null: new BufferedReader(new InputStreamReader(new FileInputStream(generatedFile.inputFile)),
								32768);) {
			if (usageReader != null) {
				usageString = usageReader.readLine();
				lineNumber++;
				usageSt = usageString.split(",");
				hashMapSize += usageSt.length;
				// Read all of the tokens.
				for (int i = 1; i < usageSt.length; i++) {
					String meterName;
					String trimmedRawMeterName = usageSt[i].replaceAll("\\s", "");
					// remove "(kWh)"
					int leftParen = trimmedRawMeterName.indexOf("(");
					if (leftParen != -1) {
						meterName = trimmedRawMeterName.substring(0, leftParen);
					} else {
						meterName = trimmedRawMeterName;
					}
					meterNames.add(meterName);
				}
				// Now read the next line of the usage file to find the start date.
				if ((usageString = usageReader.readLine()) != null) {
					lineNumber++;
					usageSt = usageString.split(",");
					String dateTimeString = usageSt[0];
					LocalDateTime dateTime = dateParser.parseDateTime(dateTimeString, "Energy Usage File", lineNumber);
					usageYear = dateTime.getYear();
				} else {
					throw new Exception("Usage Energy file contains no valid data");
				}
			}
			if (generatedReader != null) {
				lineNumber = 0;
				generatedString = generatedReader.readLine();
				lineNumber++;
				generatedSt = generatedString.split(",");
				hashMapSize += generatedSt.length;
				generatedMeterMap = new HashMap<>(hashMapSize);
				// discard first token as it is the date heading
				// Read the rest of the tokens.
				for (int i = 1; i < generatedSt.length; i++) {
					String meterName;
					String trimmedRawMeterName = generatedSt[i].replaceAll("\\s", "");
					// remove "(kWh)"
					int leftParen = trimmedRawMeterName.indexOf("(");
					if (leftParen != -1) {
						meterName = trimmedRawMeterName.substring(0, leftParen);
					} else {
						meterName = trimmedRawMeterName;
					}
					// If this meter name isn't already in the list of meter names then add it.
					// Ideally, this code will never run.
					// THIS IS WHERE WE MAP THE LOCATION OF THE METER IN THE GENERATED FILE TO THE
					// LOCATION OF THE
					// METER NAME IN THE USAGE FILE.
					if (!meterNames.contains(meterName)) {
						meterNames.add(meterName);
					}
					int index = meterNames.indexOf(meterName);
					generatedMeterMap.put(index, i);
				}

				// Now read the next line of the generated file to find the start date.
				if ((generatedString = generatedReader.readLine()) != null) {
					lineNumber++;
					generatedSt = generatedString.split(",");
					String dateTimeString = generatedSt[0];
					LocalDateTime dateTime = dateParser.parseDateTime(dateTimeString, "Energy Generated File",
							lineNumber);
					generatedYear = dateTime.getYear();
				} else {
					throw new Exception("Generated Energy file contains no valid data");
				}
			}

			//
			if ((usageReader != null && usageYear == -1) || (generatedReader != null && generatedYear == -1)
					|| (usageReader != null && generatedReader != null && usageYear != generatedYear)) {
				throw new Exception("Energy Usage file and Generated Energy file are for different years");
			}
			if (usageReader != null) {
				this.year = usageYear;
			} else if (generatedReader != null) {
				this.year = generatedYear;
			}
		}

		return meterNames;
	}

	/**
	 * Read the names of the meters from the "Energy Used" input file.
	 * 
	 * @param usageFile Energy Used.
	 * 
	 * @throws Exception If "Energy Used" input file is empty.Rethrows an exception from any called method.
	 *  
	 * @return Names of all the meters in the "Energy Used" input file.
	 */
	private List<String> getUsageMeterNames(InputFile usageFile) throws Exception {
		List<String> meterNames = new ArrayList<String>();
		String usageString;
		String usageSt[];
		int usageYear=-1;
		int lineNumber = 0;
		// If there is no usage file set then throw an error
		if (usageFile.inputFile == null) {
			throw new Exception("No Usage File present");
		}
		try (BufferedReader usageReader = 
					usageFile.inputFile == null?null: new BufferedReader(new InputStreamReader(new FileInputStream(usageFile.inputFile)), 32768);)
		{
			usageString = usageReader.readLine();
			lineNumber++;
			usageSt = usageString.split(",");
			hashMapSize += usageSt.length;
			// Read all of the tokens.
			for (int i = 1; i < usageSt.length; i++) {
				String meterName;
				String trimmedRawMeterName = usageSt[i].replaceAll("\\s", "");
				// remove "(kWh)"
				int leftParen = trimmedRawMeterName.indexOf("(");
				if (leftParen != -1) {
					meterName = trimmedRawMeterName.substring(0, leftParen);
				} else {
					meterName = trimmedRawMeterName;
				}
				meterNames.add(meterName);
			}
			// Now read the next line of the usage file to find the start date.
			if ((usageString = usageReader.readLine()) != null) {
				lineNumber++;
				usageSt = usageString.split(",");
				String dateTimeString = usageSt[0];
				LocalDateTime dateTime = dateParser.parseDateTime(dateTimeString, "Energy Usage File", lineNumber);
				usageYear = dateTime.getYear();
			} else {
				throw new Exception("Usage Energy file contains no valid data");
			}
		}
		if (this.year == -1 && usageYear != -1) {
			this.year = usageYear;
		}
		return meterNames;
	}
	
	/**
	 * Read the names of the meters from the "Energy Used" input file.
	 * 
	 * @param generatedFile Energy Used.
	 * 
	 * @throws Exception If "Energy Used" input file is empty.Rethrows an exception from any called method.
	 *  
	 * @return Names of all the meters in the "Energy Used" input file.
	 */
	private List<String> getGeneratedMeterNames(InputFile generatedFile) throws Exception {
		List<String> meterNames = new ArrayList<String>();
		String generatedString;
		String generatedSt[];
		int generatedYear=-1;
		int lineNumber = 0;
		// If there is no usage file set then throw an error
		if (generatedFile.inputFile == null) {
			throw new Exception("No Usage File present");
		}
		try (BufferedReader usageReader = 
					generatedFile.inputFile == null?null: new BufferedReader(new InputStreamReader(new FileInputStream(generatedFile.inputFile)), 32768);)
		{
			generatedString = usageReader.readLine();
			lineNumber++;
			generatedSt = generatedString.split(",");
			// Read all of the tokens.
			for (int i = 1; i < generatedSt.length; i++) {
				String meterName;
				String trimmedRawMeterName = generatedSt[i].replaceAll("\\s", "");
				// remove "(kWh)"
				int leftParen = trimmedRawMeterName.indexOf("(");
				if (leftParen != -1) {
					meterName = trimmedRawMeterName.substring(0, leftParen);
				} else {
					meterName = trimmedRawMeterName;
				}
				meterNames.add(meterName);
			}
			// Now read the next line of the usage file to find the start date.
			if ((generatedString = usageReader.readLine()) != null) {
				lineNumber++;
				generatedSt = generatedString.split(",");
				String dateTimeString = generatedSt[0];
				LocalDateTime dateTime = dateParser.parseDateTime(dateTimeString, "Energy Usage File", lineNumber);
				generatedYear = dateTime.getYear();
			} else {
				throw new Exception("Generated Energy file contains no valid data");
			}
		}
		if (this.year == -1 && generatedYear != -1) {
			this.year = generatedYear;
		}
		return meterNames;
	}

	/**
	 * Disable The .csv save buttons. Used to disable save buttons until there is data that can be saved.
	 */
	public void disableSaveButtons() {
		networkParameterFile.inputFileButton.setEnabled(false);
		usageFile.inputFileButton.setEnabled(false);
		priceFile.inputFileButton.setEnabled(false);
	}
}
