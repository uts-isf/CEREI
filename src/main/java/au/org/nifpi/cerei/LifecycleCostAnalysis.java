package au.org.nifpi.cerei;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JTable;

/**
 * Performs the lifecycle cost analysis of a proposal contained in the Lifecycle costs file, 
 * using additional savings and cost information if they have been calculated.
 *  
 * @author James Sargeant
 */
public class LifecycleCostAnalysis {
	
	/** The name of the investment from the Lifecycle Cost Parameters file. */
	protected String investmentName;
	/** The life of the investment from the Lifecycle Cost Parameters file. */
	protected String lifetimeString;
	/** The life of the investment from the Lifecycle Cost Parameters file. */
	protected double lifetime =-1;
	/** The discount rate from the Lifecycle Cost Parameters file. */
	protected double discountRate = -1;
	/** The inflation rate from the Lifecycle Cost Parameters file. */
	protected double inflationRate = -1;
	/** The degradation rate of energy generated, from the Lifecycle Cost Parameters file. */
	protected double degradationRate = 0;
	/** The "j" factor - calculated from the discount and inflation rates. */
	protected double j;

	/** Sum of all ALCC Energy Generated - reported in Lifecyle Cost Analysis pane and Lifecycle Costs Summary .csv file - discounted by the degradation rate. */
	protected double sumALCCEnergyGenerated=0;

	/** NPV of the total project - reported in Lifecyle Cost Analysis pane, the Lifecycle Summary .csv file and the Lifecyle details .csv file. */
	protected double npvCost = 0;

	/** Sum of all ATLCC Capital costs */
	protected double totalATLCCCapital = 0;
	/** Sum of all ATLCC Installation costs */
	protected double totalATLCCInstallation = 0;
	/** Sum of all ATLCC Operation and Maintenance costs */
	protected double totalATLCCFixedOM = 0;
	/** Sum of all ATLCC Replacement costs */
	protected double totalATLCCReplacement = 0;
	/** Sum of all ATLCC Future costs */
	protected double totalATLCCFuture = 0;
	/** Sum of all ATLCC costs */
	protected double totalATLCC = 0;

	/** Sum of all Capital and Installation costs */
	protected double costOfInvestment = 0;

	/** Total savings from "Business as Usual" cost saving analysis, calculated by discounting each month by the inflation and "j factor" rates. */
	protected double annualTotalSavings = 0;
	/** Total revenue of the entire project, calculated by discounting the annual Total Savings by the "j factor". */
	protected double npvRevenue = 0;

	/** Project payback period (years). */
	protected double paybackPeriod;

	/** Project levelised cose of energy (lcoe), calculated by dividing the NPV of the entire project by the Sum of all ALCC Energy Generated. */
	protected double lcoe;
	
	/** Total amount of energy exported to the grid.  0 if there are no costs, otherwise from costs.*/
	protected double totalAnnualEnergyExportedToGrid;
	/** Total amount of energy generated.  0 if there are no costs, otherwise from costs.*/
	protected double totalAnnualEnergyGenerated;
	/** Total amount of energy used from the grid.  0 if there are no costs, otherwise from costs.*/
	protected double totalGridUsed;

	/** Project components */
	protected LifecycleCostComponent[] lifecycleCostComponents;

	/** Jtable column names */
	protected final String[] TABLE_HEADINGS = { 
			"Cost Code", 
			"Component",
			"Unit Cost (AUD)", 
			"Unit",
			"No of Units", 
			"Payment (Years)",
			"Total Cost",
			"Discount Rate (%)", 
			"Inflation Rate",
			"NDR(%)",
			"N (Years)", 
			"NPV (AUD)", 
			"ATLCC (AUD)" };
	
	/** Names of the columns used in the Lifecyle Cost Analysis pane and Lifecycle Cost Summary .csv file. */
	protected String[] columnNames;

	/** Number of the line in the Lifecycle Cost Parameter file - used to report problems with that file.*/
	int lineNumber = 0;
	
	/** Generates a String with no decimal places and commas. Minimum of one whole number */
	private static final DecimalFormat df0 = new DecimalFormat("#,##0");  // Number of decimal places in summary tabs.
	/** Generates a String with two decimal places and commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df2 = new DecimalFormat("#,##0.00");  // Number of decimal places in summary tabs.

	/**
	 * Loads the parameters from the "Lifecycle Cost Parameter" file. 
	 * 
	 * @param lifecycleCostFile "Lifecycle Cost Parameter" file.
	 * 
	 * @throws Exception If there is a problem opening or reading the file.
	 */
	public LifecycleCostAnalysis(InputFile lifecycleCostFile) throws Exception {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(lifecycleCostFile.inputFile)), 16384)) {
			loadLifecycleCostComponents(reader);
		}
	}

	/**
	 * Read and parse the Lifecycle Cost Parameter" file.
	 * 
	 * @param reader Access to the "Lifecycle Cost Parameter" file. 
	 * 
	 * @throws Exception If there is a problem reading the file.
	 */
	private void loadLifecycleCostComponents(BufferedReader reader) throws Exception {
		String lineFromFile;
		String st[];
		List<LifecycleCostComponent> lifcycleCostComponents = new ArrayList<LifecycleCostComponent>();

		// Read the lines until there is a blank line indicating the end of the component.
		while ( (lineFromFile = reader.readLine()) != null) {
			lineNumber++;
			st = parseLineFromFile(lineFromFile);
			if(st.length > 0) {
				switch (st[0].toLowerCase(Locale.ENGLISH)) {
				case "":
					break;
				case "investment name":
					if (st.length < 2) {
						throw new Exception("Lifecyclce Cost Parameters file is missing Investment Name");
					}
					if (st.length >= 2) {
						this.investmentName = st[1];
					}
					break;
				case "lifetime":
					if (st.length >= 2) {
						this.lifetimeString = st[1];
						try {
							this.lifetime = Double.parseDouble(st[1]);
						}
						catch (Exception e) {
							throw new Exception("Lifetime must be a number of at least 1. Line "+lineNumber+" in Lifecycle Cost Parameters file");
						}
						if (this.lifetime < 1) {
							throw new Exception("Lifetime must be at least 1. Line "+lineNumber+" in Lifecycle Cost Parameters file");
						}
					}
					break;
				case "discount rate":
					if (st.length >= 2) {
						try {
							this.discountRate = Double.parseDouble(st[1]); // As a percentage
						}
						catch (Exception e) {
							throw new Exception("Discount Rate must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
						}
					}
					break;
				case "inflation rate":
					if (st.length >= 2) {
						try {
							this.inflationRate = Double.parseDouble(st[1]); // As a percentage
						}
						catch (Exception e) {
							throw new Exception("Inflation Rate must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
						}
					}
					break;
				case "degradation rate":
					if (st.length >= 2) {
						try {
							this.degradationRate = Double.parseDouble(st[1]); // As a percentage
						}
						catch (Exception e) {
							throw new Exception("Degradation Rate must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
						}
					}
					break;
				case "component":
					if (st.length >=2 ) {
						lifcycleCostComponents.add(loadComponent(st[1], reader));
					}
					else {
						throw new Exception("Component is missing a name. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				default:
					// Do nothing
				}
			}
		}
		
		// All paramters loaded.  Now to check that the minimum set is present.
		boolean throwException = false;
		String exceptionError = "";
		if (investmentName == null) {
			throwException = true;
			exceptionError += "Lifecycle Cost Parameter file missing Investment Name\n";
		}
		if (lifetime == -1) {
			throwException = true;
			exceptionError += "Lifecycle Cost Parameter file missing project lifetime\n";
		}
		if (discountRate == -1) {
			throwException = true;
			exceptionError += "Lifecycle Cost Parameter file missing Discount Rate\n";
		}
		if (inflationRate == -1) {
			throwException = true;
			exceptionError += "Lifecycle Cost Parameter file missing Inflation Rate\n";
		}
		if (lifcycleCostComponents.size() == 0) {
			throwException = true;
			exceptionError += "Lifecycle Cost Parameter file missing components\n";
		}
		//Throw an exception and stop
		if (throwException) {
			throw new Exception(exceptionError);
		}
		
		//Add project-wide inflation rate and discount rate if not specifically loaded with a component, check to see all mandatory parameters present
		throwException = false;
		exceptionError = "";

		for (int i=0; i<lifcycleCostComponents.size(); i++) {
			lifcycleCostComponents.get(i).lifetime = this.lifetime;
			lifcycleCostComponents.get(i).lifetimeString = this.lifetimeString;
			
			if (lifcycleCostComponents.get(i).inflationRate == -1) {
				lifcycleCostComponents.get(i).inflationRate = this.inflationRate;
			}
			if (lifcycleCostComponents.get(i).discountRate == -1) {
				lifcycleCostComponents.get(i).discountRate = this.discountRate;
			}

			//Check to see that mandatory component parameters are present
			if (lifcycleCostComponents.get(i).name==null) {
				throwException = true;
				exceptionError += "At least one Lifecycle Cost Parameter component missing a name\n";
			}
			if (lifcycleCostComponents.get(i).captialCost==0 && lifcycleCostComponents.get(i).installationCost==0 && lifcycleCostComponents.get(i).fixedOMCost==0
					&& lifcycleCostComponents.get(i).replacementCost==0 && lifcycleCostComponents.get(i).futureCost==0) {
				throwException = true;
				if (lifcycleCostComponents.get(i).name==null) {
					exceptionError += "At least one Lifecycle Cost Parameter component has no costs\n";
				}
				else {
					exceptionError += "Lifecycle Cost Parameter component \"" + lifcycleCostComponents.get(i).name + "\" has no costs\n";
				}
			}
		}
		//Throw an exception and stop
		if (throwException) {
			throw new Exception(exceptionError);
		}
		
		// Convert EconomicComponent List to an array
		lifecycleCostComponents = new LifecycleCostComponent[lifcycleCostComponents.size()];
		lifcycleCostComponents.toArray(lifecycleCostComponents); //load the array. 
	}

	/**
	 * Read all the parameters associated with an individual component of the project. 
	 * 
	 * @param name Name of the Individual Component.
	 * @param reader Access to the "Lifecycle Cost Parameter" file
	 * 
 	 * @throws Exception from called methods - most likely because of a badly formatted "Lifecycle Cost Parameter" file
 	 * 
	 * @return Project Component
	 */
	private LifecycleCostComponent loadComponent(String name, BufferedReader reader) throws Exception {
		LifecycleCostComponent lifecycleCostComponent = new LifecycleCostComponent();
		lifecycleCostComponent.name = name;
		String lineFromFile;
		String[] st;
		
		// Read the lines until there is a blank line (actually, the first token of the line is empty),
		//indicating the end of the component.
		while ((lineFromFile = reader.readLine()) != null) {
			lineNumber++;
			st = parseLineFromFile(lineFromFile);
			if(st.length == 0) {
				break; // out of the while loop reading the investment info for this component.
			}
			else if (st[0].length() > 0) {
				if (st.length < 2) {
					throw new Exception("Missing value for "+st[0]+" for component \""+name+"\" at line "+lineNumber);
				}
				switch(st[0].toLowerCase(Locale.ENGLISH)) {
				case "cost code":
					lifecycleCostComponent.costCode = st[1];
					break;
				case "number of units":
					lifecycleCostComponent.qtyString = st[1];
					try {
						lifecycleCostComponent.qty = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Number of Units must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "capital cost":
					try {
						lifecycleCostComponent.captialCost = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Capital Cost must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "installation cost":
					try {
						lifecycleCostComponent.installationCost = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Installation Cost must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "fixed o&m cost":
					try {
						lifecycleCostComponent.fixedOMCost = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Fixed O&M Cost must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "replacement cost":
					if (st.length < 3) {
						throw new Exception("Need replacement frequency (in years) at for component \""+name+"\" at line "+lineNumber);
					}
					try {
						lifecycleCostComponent.replacementCost = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Replacement Cost must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					try {
						lifecycleCostComponent.replacementFrequency = Integer.parseInt(st[2]); 
					}
					catch (Exception e) {
						throw new Exception("Replacement Frequency must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "future cost":
					if (st.length < 3) {
						throw new Exception("Need future cost frequency (in years) at for component \""+name+"\" at line "+lineNumber);
					}
					try {
						lifecycleCostComponent.futureCost = Double.parseDouble(st[1]); 
					}
					catch (Exception e) {
						throw new Exception("Future Cost must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					try {
						lifecycleCostComponent.futureFrequency = Integer.parseInt(st[2]); 
					}
					catch (Exception e) {
						throw new Exception("Future Frequency must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "discount rate":
					try {
						lifecycleCostComponent.discountRate = Double.parseDouble(st[1]); // As a percentage
					}
					catch (Exception e) {
						throw new Exception("Discount Rate must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
				case "inflation rate":
					try {
						lifecycleCostComponent.inflationRate = Double.parseDouble(st[1]); // As a percentage
					}
					catch (Exception e) {
						throw new Exception("Inflation Rate must be a number. Line "+lineNumber+" in Lifecycle Cost Parameters file");
					}
					break;
					
				default:
					// do nothing;
				}
			}
		}
		return lifecycleCostComponent;
	}

	/**
	 * Tokenise the line from the .csv file, removing any double quotes (as they mess up converting the string values to numbers).
	 * 
	 * @param lineFromFile Line from the "Lifecycle Cost Parameter" comma delimited input file.
	 * 
	 * @return An array containing all the tokens from the line.
	 */
	private String[] parseLineFromFile(String lineFromFile)  {
		String string[] = lineFromFile.split(",");
		for (int i=0; i < string.length; i++) {
			//discard all double quoutes (") from tokens.
			string[i] = string[i].replaceAll("\"", "");
		}
		return string;
	}


	/**
	 * Financial analysis of all aspects of the project, taking into account all of the costs and potential savings that arise as a result of the project.
	 *  
	 * @param bau Contains the raw savings associated with the project.
	 * @param cost Contains all the raw costs associated with the project.
	 */
	public void calculate(BusinessAsUsual bau, Cost cost) {

		totalAnnualEnergyExportedToGrid = cost==null?0:Math.abs(cost.calculateEnergyExported());
		totalAnnualEnergyGenerated = cost==null?0:Math.abs(cost.calculateEnergyGenerated());
		totalGridUsed = cost==null?0:Math.abs(cost.totalGridUsed);
		
		for (int n=1; n<=lifetime; n++) { //EQROI-1
			sumALCCEnergyGenerated +=  totalAnnualEnergyGenerated*Math.pow(1-(degradationRate/100), (double) n)/Math.pow(1+(discountRate/100), (double) n);
		}
		
		// Do some initial calculations on the lifecycle cost components.
		j = (discountRate/100 + inflationRate/100 + ((discountRate/100)*(inflationRate/100)))*100;  //EQROI-3

		// Make sure economicComponents all have a valid cost code.  Use the cost code from the Lifecycle Cost Analysis input file if there was one, otherwise
		// assign a String starting at "1"
		int defaultCostCode = 1;
		for (int i=0; i<lifecycleCostComponents.length; i++) {
			if (lifecycleCostComponents[i].costCode == null) {
				lifecycleCostComponents[i].costCode = String.valueOf(defaultCostCode);
				defaultCostCode++;
			}
		}
		
		for (int i=0; i<lifecycleCostComponents.length; i++) {
			lifecycleCostComponents[i].totalCapitalCost = lifecycleCostComponents[i].qty * lifecycleCostComponents[i].captialCost;
			lifecycleCostComponents[i].totalInstallationCost = lifecycleCostComponents[i].qty * lifecycleCostComponents[i].installationCost;
			if (lifecycleCostComponents[i].discountRate == -1) {
				lifecycleCostComponents[i].discountRate = this.discountRate;
			}
			if (lifecycleCostComponents[i].inflationRate == -1) {
				lifecycleCostComponents[i].inflationRate = this.inflationRate;
			}
			if (lifecycleCostComponents[i].lifetimeString == null) {
				lifecycleCostComponents[i].lifetimeString = this.lifetimeString;
				lifecycleCostComponents[i].lifetime = this.lifetime;
			}
			lifecycleCostComponents[i].calculateATLCC();
			totalATLCCCapital += lifecycleCostComponents[i].atlccCapital; 
			totalATLCCInstallation += lifecycleCostComponents[i].atlccInstallation; 
			totalATLCCFixedOM += lifecycleCostComponents[i].atlccfixedOMCost; 
			totalATLCCReplacement += lifecycleCostComponents[i].atlccReplacement;
			totalATLCCFuture += lifecycleCostComponents[i].atlccFuture;
			costOfInvestment += lifecycleCostComponents[i].totalCapitalCost + lifecycleCostComponents[i].totalInstallationCost; //EQROI-12
			npvCost += lifecycleCostComponents[i].totalNPV;
		}

		totalATLCC = totalATLCCCapital + totalATLCCInstallation + totalATLCCFixedOM +totalATLCCReplacement + totalATLCCFuture; //EQROI-11
		
		double monthlyj = ((discountRate/100 + inflationRate/100 + ((discountRate/100)*(inflationRate/100)))*100)/12;  //EQROI-14
		double monthlyInflationRate = inflationRate/12; //EQROI-15

		// If there was a "business as usual" bill, Calculate discounted monthly total savings month by month
		if (bau != null) {
			for (int k = 0; k < 12; k++) {
				annualTotalSavings += bau.totalsavingsMeter.monthly[k] * (Math.pow(1 + (monthlyInflationRate / 100), k)/Math.pow(1 + (monthlyj / 100), k)); // EQROI-16
			}
			for (int n = 1; n<= lifetime; n++) {
				npvRevenue += annualTotalSavings*(Math.pow(1+(j/100), lifetime));
			}
		}
		
		if (annualTotalSavings > 0 || annualTotalSavings < 0) {
			paybackPeriod = costOfInvestment / annualTotalSavings; //EQROI-17
		}
		
		if (totalAnnualEnergyGenerated == 0) {
			lcoe = -1;
		}
		else {
			lcoe = Math.abs(npvCost) / sumALCCEnergyGenerated; //EQROI-18
		}
	}

	/**
	 * Convert the information in contained in individual project components into a 2-dimensional array of Strings.
	 * 
	 * @return Two dimensional array containing individual project component details.
	 * 
	 * @deprecated since version 1.0.1.0
	 * @deprecated Use {@link #createLifecycleCostDetailsArray()} instead
	 */
	protected String[][] createEconomicDetailsArray() {
		return createLifecycleCostDetailsArray();
	}

	/**
	 * Convert the information in contained in individual project components into a 2-dimensional array of Strings.
	 * 
	 * @return Two dimensional array containing individual project component details.
	 * 
	 * @deprecated since version 1.0.1.0
	 */
	protected String[][] createLifecycleCostDetailsArray() {
		int numRows = 0;
		columnNames = TABLE_HEADINGS;
		//calculate the number of rows of data
		//Component by component
		for (int i=0; i<lifecycleCostComponents.length; i++) {
			numRows += lifecycleCostComponents[i].getNumOutputRows();
		}
		numRows++; // For "Scenario Equipment Totals
		
		String[][] economicData = new String[numRows][columnNames.length];
		int currentRow=0;
	
		for (int i=0; i< lifecycleCostComponents.length; i++) {
			currentRow = lifecycleCostComponents[i].loadDataIntoArray(economicData,currentRow);
		}
		
		economicData[currentRow][0] = df0.format(lifecycleCostComponents.length+1);
		economicData[currentRow][1] = "Scenario Equipment Totals";
		economicData[currentRow][11] = df2.format(npvCost);
		economicData[currentRow][12] = df2.format(totalATLCC);

		return economicData;
	}

	/**
	 * Create the table model to display the financial analysis of the project  in the "Lifecycle Cost Analysis" pane of the Energy Calculator.
	 * 
	 * @param resultTable The table in the "Potential Savings" pane.
	 * @param lifecycle Financial analysis of the entire project
	 * 
	 * @return Table model containing Financial Analysis.
	 */
	public CEREITableModel createLifecycleCostSummaryTableModel(JTable resultTable, LifecycleCostAnalysis lifecycle) {
		
		String[] columnNames = {"Outcome","Value"};
		//String[][] economicSummaryData = new String[10][2];
		String[][] economicSummaryData = new String[11][2];
		
		economicSummaryData[0][0] = "Number of components";
		economicSummaryData[0][1] = df0.format(lifecycleCostComponents.length);
		economicSummaryData[1][0] = "Period of analysis (years)";
		economicSummaryData[1][1] = lifetimeString;
		economicSummaryData[2][0] = "Cost of Investment ($)";
		economicSummaryData[2][1] = df2.format(-1*costOfInvestment);
		economicSummaryData[3][0] = "Total Revenue (future value of all revenue and savings) ($)";
		economicSummaryData[3][1] = df2.format(npvRevenue);
		economicSummaryData[4][0] = "Net Present Value (NPV) ($)";
		economicSummaryData[4][1] = df2.format(npvCost + npvRevenue);
		economicSummaryData[5][0] = "Annual Worth (AW) ($/year)";
		economicSummaryData[5][1] = df2.format(totalATLCC+annualTotalSavings);
		economicSummaryData[6][0] = "Total life cycle energy generated (kWh)";
		economicSummaryData[6][1] = df2.format(sumALCCEnergyGenerated);
		economicSummaryData[7][0] = "Annual energy generated (kWh/year)";
		economicSummaryData[7][1] = df2.format(totalAnnualEnergyGenerated);
		economicSummaryData[8][0] = "Annual energy exported to the grid (kWh/year)";
		economicSummaryData[8][1] = df2.format(totalAnnualEnergyExportedToGrid);
		
		economicSummaryData[9][0] = "Payback period (Years)";
		if (annualTotalSavings == 0) {
			economicSummaryData[9][1] = "n/a";
		}
		else {
			economicSummaryData[9][1] = df2.format(paybackPeriod);
		}
		economicSummaryData[10][0] = "Levelized Cost of Energy (LCOE) ($/kWh)";
		if (lcoe == -1) {
			economicSummaryData[10][1] = "n/a";
		}
		else {
			economicSummaryData[10][1] = df2.format(lcoe);
		}

		//Nice heading for table
		resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

		resultTable.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		

    	CEREITableModel economicTopTableModel = new CEREITableModel(economicSummaryData,columnNames);

		return economicTopTableModel;
	}

}
