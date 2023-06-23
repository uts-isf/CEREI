package au.org.nifpi.cerei;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Simply calculates the PEI from the relevant information.
 * TO DO - Refactor. Contains a lot of duplicated code here and also in Cost.  
 * 
 * @author James Sargeant
 */
public class PEI {

	/** An array that contains the costs for each meter for each month */
	protected PEIPerMonth peisPerMonth[][]; // Costs are done per meter per month
	/** A list of meter names that is the "source of truth" for the calculator */
	protected List<String> meterNames;

	/** Summary information for PEI's for all real, physical meters */
	protected List<MeterSummary> realMeterPeiSummaries = new ArrayList<MeterSummary>(); // Summary information for pei
	/** Summary information for PEI's for all meters, including sub totals and grand totals.  */
	protected List<MeterSummary> allMeterPeiSummaries = new ArrayList<MeterSummary>();

	/** Jtable column names */
	protected String[] columnNames;
	/** PEI summary data in rows */
	private String peiData[][];

	/** Generates a String with two decimal places and commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df2 = new DecimalFormat("#,###.00"); // Number of decimal places in summary tabs.

	/** Months of the year for output of summary information */
	public static final String[] MONTH_NAMES = { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };

	// New formula from Ibrahim - 31 July 2022
	// PEI = X/Y,
	// where X = (SUM(TotalCharges30min ($/kWh) * Usage30min (kWh))/SUM(Usage30min
	// (kWh)))
	// = sumTotalChargeUsage/Usage.
	// where Y = MEAN(TotalCharges30min ($/kWh))
	// = sumTotalCharges30min / numberOfMeasurements

	/**
	 * Creates the per meter per month data structure and loads metre names from the
	 * usage file. Usage file is source of truth for meter names.
	 * 
	 * @param meterNames List of meter names
	 * @param year       Year for which data is being processed.
	 */
	public PEI(List<String> meterNames, int year) {
		this.meterNames = meterNames;

		// Initialise the array
		peisPerMonth = new PEIPerMonth[meterNames.size()][12];

		// Create the CostPerMonth objects
		for (int i = 0; i < 12; i++) {
			for (int j = 0; j < meterNames.size(); j++) {
				peisPerMonth[j][i] = new PEIPerMonth(meterNames.get(j), i, year);
			}
		}
	}

	/**
	 * Read the tariff data structure (which contains more information than just
	 * tariff information) to insert loss ratios, Demand Critical Peak Usage and
	 * Demand Capacity Usage into the relevant PEIPerMonth object.
	 * 
	 * @param networkParameters Network Parameter data structure
	 */
	protected void addMonthlyParameters(NetworkParameter networkParameters) {
		// Add Meter monthly parameters to relevant CostPerMonth
		for (int i = 0; i < meterNames.size(); i++) { // process all meters
			// Match the relevant meter.
			for (int j = 0; j < networkParameters.meters.size(); j++) {
				if (networkParameters.meters.get(j).meterName.compareTo(this.peisPerMonth[i][0].meterName) == 0) {
					for (int k = 0; k < 12; k++) {
						this.peisPerMonth[i][k].spotPriceLossRatio = networkParameters.meters
								.get(j).monthlyParameters[k].spotPriceLossRatio;
						this.peisPerMonth[i][k].feedInLossRatio = networkParameters.meters
								.get(j).monthlyParameters[k].feedInLossRatio;
					}
					break;
				}
			}
		}
	}

	/**
	 * Allocate usage energy and generation power contained in a single line (and matched) line of the Energy Usage, Spot Price, Energy Generated 
	 * and Feed-in Tarrif files to each meter and month. 
	 * TO DO - Refactor.  Has grown to be difficult to follow with repeated code.
	 * 
	 * @param networkParameters Energy charge, Network charge, Spot Energy, Market charge and Other Charge Loss ratios and pricing.
	 * @param recordDateTime Date and Time of the lines in the Energy Usage, Spot Price, Energy Generated and Feed-in Tarrif files.
	 * @param usageSt Line from the Energy Usage file.
	 * @param priceSt Line from the Spot Price file.
	 * @param generatedSt Line from the Energy Generated file.
	 * @param feedInSt Line from the Feed-in Tariff file.
	 * @param generatedMeterMap Maps the column in the Energy Generated file to the right column of the Energy Usage file.
	 */
	public void addCharges(NetworkParameter networkParameters, LocalDateTime recordDateTime, String[] usageSt,
			String[] priceSt, String[] generatedSt, String[] feedInSt, HashMap<Integer, Integer> generatedMeterMap) {

		double usedEnergy = 0, generatedEnergy = 0;
		// Get the month the usage date stamp.
		int month = recordDateTime.getMonthValue() - 1; // Minus 1 to turn month into our array index

		// If we are redistributing generated energy charges across mulitple meters we have to make adjustments to usageSt and generatedSt
		if (networkParameters.distributeGeneration && usageSt != null && generatedSt != null) {
			double adjustedUsageSt[] = new double[usageSt.length-1]; // -1 because usageSt has an additional datestamp at
																	// the start.
			// Load adjustedUsageSt[] from usageSt. Needs to be done so that when generatedEnergy is exhausted,
			// meters that have not been adjusted still have their initial values.
			for (int i = 0; i < usageSt.length - 1; i++) {
				adjustedUsageSt[i] = Double.parseDouble(usageSt[i + 1]); // +1 as first entry in usageSt is the datestamp
			}
			double adjustedGeneratedSt[] = new double[usageSt.length - 1]; // -1 because usageSt has an additional datestamp at the start.
			// first determine the total amount of generated energy to distributed
			for (int i = 1; i < generatedSt.length; i++) { // i=1 as first entry in generatedSt is the datestamp
				generatedEnergy += Double.parseDouble(generatedSt[i]) / 2;  //Divide by 2 because generated energy is in kW - not kWh
			}

			// Use the list of distribution meters to adjust that meter's usageSt
			for (int i = 0; i < networkParameters.distributionMeters.size(); i++) {
				// find the index of the usageSt meter
				int meterNameIndex = meterNames.indexOf(networkParameters.distributionMeters.get(i));
				if (meterNameIndex != -1) {
					double initialUsage = Double.parseDouble(usageSt[meterNameIndex + 1]); // +1 as first entry in
					// generatedSt is the
					// datestamp
					double deduction = Math.min(generatedEnergy, initialUsage);
					// overwrite original usageSt value.
					adjustedUsageSt[meterNameIndex] = initialUsage - deduction;
					generatedEnergy -= deduction;
					if (generatedEnergy <= 0) {
						break; // break out of for loop - we've used up all the generated energy
					}
				}
			}

			// If there is any generated energy left add it to the adjustedGeneratedSt of the first meter in the network Parameters distribution list
			if (generatedEnergy > 0) {
				adjustedGeneratedSt[meterNames.indexOf(networkParameters.distributionMeters.get(0))] = generatedEnergy;
			}
			// Now iterate over meters calling peisPerMonth[i][month].addUnitOfCharges.
			for (int i = 0; i < meterNames.size(); i++) {
				peisPerMonth[i][month].addUnitOfCharges(networkParameters, recordDateTime, adjustedUsageSt[i],
						priceSt == null ? 0.0 : Double.parseDouble(priceSt[1]), adjustedGeneratedSt[i],
						feedInSt == null ? 0.0 : Double.parseDouble(feedInSt[1]));
			}

		} else {// No distribution over multiple meters.
				// Now process each meter in turn.
			for (int i = 0; i < meterNames.size(); i++) {
				// If the meter index (i) is bigger than the number of meter tokens in generated
				// string, it means
				// that this meter does not have a corresponding usage meter, so usage is zero.
				// -1 because the first token in usageSt is the date.
				if (usageSt == null || i >= usageSt.length - 1) {
					usedEnergy = 0;
				} else {
					usedEnergy = Double.parseDouble(usageSt[i + 1]);
				}
				// Find the relevant generated energy meter reading (if any) for this meter
				// If this meter has a generated energy component
				if (generatedMeterMap != null && generatedMeterMap.containsKey(i)) {
					generatedEnergy = Double.parseDouble(generatedSt[generatedMeterMap.get(i)]) / 2;
				}
				else  {// the generated energy will be zero
					generatedEnergy=0;
				}
				// Spot Price and Feed in tariff always second item in priceSt and feedInSt
				peisPerMonth[i][month].addUnitOfCharges(networkParameters, recordDateTime, usedEnergy,
						priceSt == null ? 0.0 : Double.parseDouble(priceSt[1]), generatedEnergy,
						feedInSt == null ? 0.0 : Double.parseDouble(feedInSt[1]));
			}
		}
	}

	/**
	 * Calculate the per month PEIs once all the data has been read in from the usage and price files and processed
	 * 
	 * @throws Exception Rethrows an exception from any called method. 
	 */
	public void calculatePEIs() throws Exception {
		for (int i = 0; i < 12; i++) { // Month by month
			for (int j = 0; j < meterNames.size(); j++) { // Meter by meter
				peisPerMonth[j][i].calcuateCharges();
			}
		}
	}

	/**
	 * Calculate the PEI
	 * 
	 * @param charge          Sum of (each 30 minute charge)
	 * @param chargeUsage     Sum of (each 30 minute charge multiplied by each 30 minute usage)
	 * @param usage           Sum of (each 30 minute usage)
	 * @param numMeasurements Number of measurements in each month.
	 * 
	 * @return Price Efficiency Index
	 */
	public static double calculatePEI(double charge, double chargeUsage, double usage, int numMeasurements) {
		double pei;
		if (charge == 0 || usage == 0) {
			pei = -1;
		}

		else {
			pei = (chargeUsage * numMeasurements) / (charge * usage);
		}

		return pei;

	}

	/**
	 * Create the table model to display the costs in the "Energy Costs" pane of the Energy Calculator.
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs
	 * 
	 * @return Table model containing PEIs
	 */
	public CEREITableModel createSummaryPEITableModel() throws Exception {
		// Trawl through the per Month data and generate the summary information.
		realMeterPeiSummaries = createRealMeterPEISummaries();
		// Client no longer want a Total PEI displayed
		// MeterSummary totalPeiMeter =
		// createTotalMeterPEISummary(realMeterPeiSummaries);

		// Add subtotal and total processing here; build allMeterSummaries - not
		// currently implemented

		// Quick and dirty hack to get some output without column order or sub-totals.
		allMeterPeiSummaries = realMeterPeiSummaries;
		// Client no longer want a Total PEI displayed
		// allMeterPeiSummaries.add(totalPeiMeter);

		return (createSummaryTableModel(allMeterPeiSummaries));

	}

	/**
	 * Convert the information in MeterSummary form to a TableModel form.
	 * 
	 * @param allMeterSummaries PEIs in MeterSummary Form. 
	 * 
	 * @return Table model containing PEIs.
	 */
	public CEREITableModel createSummaryTableModel(List<MeterSummary> allMeterSummaries) {
		// Create the column names for the JTable.
		columnNames = new String[allMeterSummaries.size() + 3]; // Add three for extra columns for year, quarter and
																// month

		// First three column names are fixed
		columnNames[0] = "Year";
		columnNames[1] = "Quarter";
		columnNames[2] = "Month";

		// Then the names of the meters
		for (int i = 0; i < allMeterSummaries.size(); i++) {
			columnNames[i + 3] = allMeterSummaries.get(i).meterName;
		}

		// Now the data for the JTable
		peiData = createSummaryOutput(allMeterSummaries);

		CEREITableModel peiTableModel = new CEREITableModel(peiData, columnNames);

		return peiTableModel;
	}

	/**
	 * Create a summary of the monthly PEI data that can be eventually displayed on the PEI Summary pane of the UI
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs
	 * 
	 * @return Meter by Meter list of PEIs for each month, quarter and year.
	 */
	public List<MeterSummary> createRealMeterPEISummaries() throws Exception {
		List<MeterSummary> realMeterSummaries = new ArrayList<MeterSummary>();

		// Meter by meter
		for (int i = 0; i < meterNames.size(); i++) {
			MeterSummary monthlyMeterSummary = new MeterSummary();
			monthlyMeterSummary.addEei(peisPerMonth[i]);
			realMeterSummaries.add(monthlyMeterSummary);
		}
		return realMeterSummaries;
	}

	/**
	 * Create a "Grand Total" PEI that calculates "total PEI" for all meters
	 * monthly, quarterly and yearly. Easily modified so that it can be used to
	 * generate sub-total PEI summaries.
	 * 
	 * @param realMeterSummaries List of meters to be included in total PEI.
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs
	 *                   
	 * @return Grand total PEI information in the same format as real or summary meters, null if no meters in input list.
	 */
	protected MeterSummary createTotalMeterPEISummary(List<MeterSummary> realMeterSummaries) throws Exception {
		MeterSummary totalMeter = null;

		double monthlyTotalCharge = 0;
		double monthlyTotalChargeUsage = 0;
		double monthlyTotalUsage = 0;
		int monthlyNumberOfMeasurements = 0;

		double quarterlyTotalCharge = 0;
		double quarterlyTotalChargeUsage = 0;
		double quarterlyTotalUsage = 0;
		int quarterlyNumberOfMeasurements = 0;

		double yearlyTotalCharge = 0;
		double yearlyTotalChargeUsage = 0;
		double yearlyTotalUsage = 0;
		int yearlyNumberOfMeasurements = 0;

		if (realMeterSummaries.size() > 0) {
			totalMeter = new MeterSummary();

			totalMeter.meterName = "Grand Total";
			totalMeter.meterType = MeterSummary.TOTAL_COST;
			totalMeter.year = realMeterSummaries.get(0).year;

			// Quarter by Quarter
			for (int i = 0; i < 4; i++) {
				quarterlyTotalCharge = 0;
				quarterlyTotalChargeUsage = 0;
				quarterlyTotalUsage = 0;
				quarterlyNumberOfMeasurements = 0;

				// Month by Month
				for (int j = 0; j < 3; j++) {
					monthlyTotalCharge = 0;
					monthlyTotalChargeUsage = 0;
					monthlyTotalUsage = 0;
					monthlyNumberOfMeasurements = 0;

					// Meter by Meter
					for (int k = 0; k < realMeterSummaries.size(); k++) {
						monthlyTotalCharge += realMeterSummaries.get(k).sumTotalCharge[3 * i + j];
						monthlyTotalChargeUsage += realMeterSummaries.get(k).sumTotalChargeUsage[3 * i + j];
						monthlyTotalUsage += realMeterSummaries.get(k).monthlyUsage[3 * i + j];
						monthlyNumberOfMeasurements += realMeterSummaries.get(k).numberOfMeasurements[3 * i + j];

						quarterlyTotalCharge += realMeterSummaries.get(k).sumTotalCharge[3 * i + j];
						quarterlyTotalChargeUsage += realMeterSummaries.get(k).sumTotalChargeUsage[3 * i + j];
						quarterlyTotalUsage += realMeterSummaries.get(k).monthlyUsage[3 * i + j];
						quarterlyNumberOfMeasurements += realMeterSummaries.get(k).numberOfMeasurements[3 * i + j];

						yearlyTotalCharge += realMeterSummaries.get(k).sumTotalCharge[3 * i + j];
						yearlyTotalChargeUsage += realMeterSummaries.get(k).sumTotalChargeUsage[3 * i + j];
						yearlyTotalUsage += realMeterSummaries.get(k).monthlyUsage[3 * i + j];
						yearlyNumberOfMeasurements += realMeterSummaries.get(k).numberOfMeasurements[3 * i + j];
					}
					totalMeter.monthly[3 * i + j] = PEI.calculatePEI(monthlyTotalCharge, monthlyTotalChargeUsage,
							monthlyTotalUsage, monthlyNumberOfMeasurements);
				}
				totalMeter.quarterly[i] = PEI.calculatePEI(quarterlyTotalCharge, quarterlyTotalChargeUsage,
						quarterlyTotalUsage, quarterlyNumberOfMeasurements);
			}
			totalMeter.yearly = calculatePEI(yearlyTotalCharge, yearlyTotalChargeUsage, yearlyTotalUsage,
					yearlyNumberOfMeasurements);
		}

		return totalMeter;
	}

	/**
	 * Convert the information in MeterSummary form to a 2-dimensional array of Strings.
	 * 
	 * @param meterSummaries PEIs in MeterSummary Form.
	 * 
	 * @return Two dimensional array containing PEIs.
	 */
	private String[][] createSummaryOutput(List<MeterSummary> meterSummaries) {
		String outputData[][];
		int year;

		year = meterSummaries.get(0).year;
		// number of rows in table will be 17, 12 months plus 4 quarters plus grand
		// total
		outputData = new String[17][meterSummaries.size() + 3];
		// load data into table row by row
		// Quarter by quarter
		int i;
		for (i = 0; i < 4; i++) {
			// Month by month in quarter
			int j;
			// i*4 because there is 3 months in a quarter plus the quarter row
			for (j = 0; j < 3; j++) {
				outputData[i * 4 + j][0] = ""; // No Year in this row
				outputData[i * 4 + j][1] = ""; // No quarter in this row
				outputData[i * 4 + j][2] = MONTH_NAMES[i * 3 + j];
				for (int k = 0; k < meterSummaries.size(); k++) {
					double pei = meterSummaries.get(k).monthly[i * 3 + j];
					if (pei == -1) {
						outputData[i * 4 + j][k + 3] = df2.format(0.0);
					} else {
						outputData[i * 4 + j][k + 3] = df2.format(pei);
					}
				}
			}
			outputData[i * 4 + j][0] = ""; // No year in this row
			outputData[i * 4 + j][1] = "Q" + Integer.toString(i + 1);
			outputData[i * 4 + j][2] = ""; // No month in this row
			for (int k = 0; k < meterSummaries.size(); k++) {
				double pei = meterSummaries.get(k).quarterly[i];
				if (pei == -1) {
					outputData[i * 4 + j][k + 3] = df2.format(0.0);
				} else {
					outputData[i * 4 + j][k + 3] = df2.format(pei);
				}
			}
		}
		outputData[16][0] = Integer.toString(year); // Year in last row
		outputData[16][1] = ""; // No quarter in this row
		outputData[16][2] = ""; // No month in this row
		for (int k = 0; k < meterSummaries.size(); k++) {
			double pei = meterSummaries.get(k).yearly;
			if (pei == -1) {
				outputData[16][k + 3] = df2.format(0.0);
			} else {
				outputData[16][k + 3] = df2.format(pei);
			}
		}
		return outputData;
	}

	/**
	 * Alow other classes to get hold of the column names but NOT change them.
	 * 
	 * @return Names of columns in summary Jtables
	 */
	public String[] getColumnNames() {
		return (this.columnNames.clone());
	}

	/**
	 * Allow other classes to get hold of data in the JTable PEI summary but NOT
	 * change it.
	 * 
	 * @return A copy of the JTable PEI summary data.
	 */
	public String[][] getPeiData() {
		return (this.peiData.clone());
	}
}
