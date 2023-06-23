package au.org.nifpi.cerei;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generate the savings associated with moving to a different tariff plan and from generating own energy.
 *
 * @author James Sargeant
 */
public class BusinessAsUsual {

	/** Generates a String with two decimal places and commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df = new DecimalFormat("#,##0.00");

	/** Months of the year for output of summary information. */
	public static final String[] MONTH_NAMES = { "January", "February", "March", "April", "May", "June", "July",
			"August", "September", "October", "November", "December" };

	/** Jtable column names. */
	protected String[] columnNames;

	/** A list of meter names from the BAU bill. */
	protected List<String> meterNames;

	/** The year for which the bill is valid, obtained from the first cell of the fourth line of the BAU bill. */
	protected int year;

	/** Meter information loaded from the "Business as Usual" file. */
	protected MeterSummary[] loadedMeterSummaryArray;

	/** Holds the savings - difference between calculated costs and business as usual. */
	List<MeterSummary> allbauSavings;

	/** Holds the total saving - sum of savings for each meter. */
	protected MeterSummary totalsavingsMeter;

	/**
	 * Loads the business as usual costs from the "Business as Usual" bill.
	 * 
	 * @param bauFile InputFile containing name of file to read.
	 * 
	 * @throws Exception If there is a problem opening or reading the file.
	 */
	public BusinessAsUsual(InputFile bauFile) throws Exception {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(bauFile.inputFile)),
				16384)) {
			loadbau(reader, bauFile.inputFile.getName());
		}
	}

	/**
	 * Read and parse the "Business as Usual" file.
	 * 
	 * @param reader Access to the "Business as Usual" file. 
	 * @param filename Name of the Business as Usual file.
	 * 
	 * @throws Exception If there is a problem reading the file.
	 */
	private void loadbau(BufferedReader reader, String filename) throws Exception {
		String st[];
		String lineFromFile;
		int lineNumber = 0;

		// A cumulative error string so we can find all of the problems with the BAU file in one pass.
		String cumulativeErrorString = "";
		
		// First two lines of file contain no usefull information
		reader.readLine();
		lineNumber++;
		reader.readLine();
		lineNumber++;

		// Read the next line, which should contain meter names
		if ((lineFromFile = reader.readLine()) == null) {
			throw new Exception("Business as Usual File "+filename+" is empty");
		}

		st = parseLineFromFile(lineFromFile);
		lineNumber++;
		//Make sure there is at least one meter name
		if (st.length<5) {
			throw new Exception("No meter names in Business as Usual File "+filename);
		}
		// Used to make sure there are the right number of values on each line.
		int correctNumberOfTokens = st.length;
		
		// Create an array of meters (to be converted to a list later on)
		// First four cells in input file are empty.
		loadedMeterSummaryArray = new MeterSummary[st.length - 4];

		meterNames = new ArrayList<String>();
		// Grab the meterNames
		for (int i = 4; i < st.length; i++) {
			meterNames.add(st[i]);
			loadedMeterSummaryArray[i - 4] = new MeterSummary();
			loadedMeterSummaryArray[i - 4].meterName = st[i];
			loadedMeterSummaryArray[i - 4].meterType = MeterSummary.REAL_COST;
			loadedMeterSummaryArray[i - 4].meterProcessingStatus = MeterSummary.LOADED;
		}

		// Line by line starting from line 4
		for (int quarter = 0; quarter < 4; quarter++) {
			for (int month = 0; month < 3; month++) {
				try {
					if ((lineFromFile = reader.readLine()) != null) {
						// Parse the next line, which should contain meter costs
						lineNumber++;
						st = parseLineFromFile(lineFromFile);
						
						/*
						//Make sure there are the right number of tokens in the line.
						if (st.length < correctNumberOfTokens) {
							cumulativeErrorString += "Missing data on line "+lineNumber;
						}
						if (st.length > correctNumberOfTokens) {
							throw new Exception("Extra data on line "+lineNumber+" of "+filename);
						}
						*/
						// If this is the fourth line of the file, the first cell should contain the
						// current year
						if (lineNumber == 4) {
							try {
								this.year = Integer.parseInt(st[0]);
								for (int i = 0; i < loadedMeterSummaryArray.length; i++) {
									loadedMeterSummaryArray[i].year = this.year;
								}
							}
							catch (Exception e) {
								cumulativeErrorString += "Invalid value for a year on line "+lineNumber+" column 1\n";
							}
						}

						/*
						// Check to see if there are the expected number of tokens, throw an error
						if (st.length < meterNames.size() + 4) {
							throw new Exception("Missing meter costs in Business as Usual bill, line " + lineNumber);
						}
						*/
						
						// Check to see if there are the expected number of tokens, throw an error
						if (st.length > meterNames.size() + 4) {
							cumulativeErrorString += "Badly formatted meter costs, line "+lineNumber+". Suspected comma in a meter cost\n";
						}

						for (int meter = 0; meter < meterNames.size(); meter++) {
							try {
								loadedMeterSummaryArray[meter].daysInMonth[3 * quarter + month] = Integer.parseInt(st[3]);
							}
							catch (Exception e) {
								cumulativeErrorString += "Line "+lineNumber+" column 4 is not a number - must be days in month\n";
							}
							try {
								loadedMeterSummaryArray[meter].monthly[3 * quarter + month] = Double.parseDouble(st[meter + 4]);
							}
							catch (Exception e) {
								cumulativeErrorString += "Line "+lineNumber+" column "+(meter+5)+" is not a number\n";
							}
						}
						if (month == 2) { // We have read three months worth of data, the next line should be quarter data
							// Read the next line
							lineFromFile = reader.readLine();
							lineNumber++;
							st = parseLineFromFile(lineFromFile);
							
							/*
							//Make sure there are the right number of tokens in the line.
							if (st.length < correctNumberOfTokens) {
								throw new Exception("Missing data on line "+lineNumber+" of "+filename);
							}
							*/
							if (st.length > correctNumberOfTokens) {
								cumulativeErrorString += "Badly formatted meter costs, line "+lineNumber+". Suspected comma in a meter cost\n";
							}
							
							// Update the quarterly information
							for (int meter = 0; meter < meterNames.size(); meter++) {
								try {
									loadedMeterSummaryArray[meter].quarterly[quarter] = Double.parseDouble(st[meter + 4]);
								}
								catch (Exception e) {
									cumulativeErrorString += "Line "+lineNumber+" column "+(meter+5)+" is not a number\n";
								}
							}
						}
						if (month == 2 && quarter == 3) { // We have now read all the month and quarter information
							// The next line must be the annual total
							lineFromFile = reader.readLine();
							lineNumber++;
							st = parseLineFromFile(lineFromFile);
							
							/*
							//Make sure there are the right number of tokens in the line.
							if (st.length < correctNumberOfTokens) {
								throw new Exception("Missing data on line "+lineNumber+" of "+filename);
							}
							*/
							
							if (st.length > correctNumberOfTokens) {
								cumulativeErrorString += "Badly formatted meter costs, line "+lineNumber+". Suspected comma in a meter cost\n";
							}
							
							// Update the Yearly information
							for (int meter = 0; meter < meterNames.size(); meter++) {
								try {
									loadedMeterSummaryArray[meter].yearly = Double.parseDouble(st[meter + 4]);
								}
								catch (Exception e) {
									cumulativeErrorString += "Line "+lineNumber+" column "+(meter+5)+" is not a number\n";
								}
							}
						}
					} else {
						throw new Exception("Mising lines in Business as Usual bill "+filename);
					}

				} catch (Exception e) {
					throw e;
				}
			}
		}
		if (cumulativeErrorString.compareTo("") != 0) {
			throw new Exception("Problems with Business As Usual file "+filename+"\n"+cumulativeErrorString);
		}

	}

	/**
	 * Tokenise the line from the .csv file, removing any double quotes (as they mess up converting the string values to numbers). 
	 * 
	 * @param lineFromFile Line from the "Business as Usual File" comma delimited input file.
	 * 
	 * @return An array containing all the tokens from the line.
	 */
	private String[] parseLineFromFile(String lineFromFile) {
		String string[] = lineFromFile.split(",");
		for (int i = 0; i < string.length; i++) {
			// discard all double quoutes (") from tokens.
			string[i] = string[i].replaceAll("\"", "");
		}
		return string;
	}

	/**
	 * Create the table model to display the cost savings in the "Potential Savings" pane of the Energy Calculator.
	 * 
	 * @param cost Costs All the cost information from meters and generators. 
	 * 
	 * @return Table model containing Potential savings
	 */
	public CEREITableModel createSavingCostTableModel(Cost cost) {
		List<MeterSummary> bauMeterSummaryList = new ArrayList<MeterSummary>();
		MeterSummary[] savings;
		if (cost == null) {
			savings = new MeterSummary[0];
		} else {
			savings = new MeterSummary[cost.meterNames.size()];

			// First up, create the list of MeterSummary that only contain Real Meters.
			// The order will be the same as the order in the Costs meter.
			for (int i = 0; i < cost.meterNames.size(); i++) {
				boolean found = false;
				for (int j = 0; j < this.meterNames.size(); j++) {
					if (cost.meterNames.get(i).compareTo(this.meterNames.get(j)) == 0) {
						bauMeterSummaryList.add(this.loadedMeterSummaryArray[j]);
						found = true;
						break;
					}
				}
				if (!found) {
					// Add an empty MeterList
					MeterSummary emptyMeterSummary = new MeterSummary();
					emptyMeterSummary.meterName = cost.meterNames.get(i);
					bauMeterSummaryList.add(emptyMeterSummary);
				}
			}

			// Now do the subtractions to end up with the savings in a new Array of
			// MeterSummaries
			for (int i = 0; i < bauMeterSummaryList.size(); i++) {
				MeterSummary saving = new MeterSummary();
				// save the name and year
				saving.meterName = bauMeterSummaryList.get(i).meterName;
				saving.year = bauMeterSummaryList.get(i).year;

				for (int j = 0; j < 12; j++) {
					saving.daysInMonth[j] = bauMeterSummaryList.get(i).daysInMonth[j];
					saving.monthly[j] = bauMeterSummaryList.get(i).monthly[j]
							- cost.realMeterCostSummaries.get(i).monthly[j];
				}
				for (int j = 0; j < 4; j++) {
					saving.quarterly[j] = bauMeterSummaryList.get(i).quarterly[j]
							- cost.realMeterCostSummaries.get(i).quarterly[j];
				}
				saving.yearly = bauMeterSummaryList.get(i).yearly - cost.realMeterCostSummaries.get(i).yearly;
				savings[i] = saving;
			}
		}
		// Convert savings into a list.
		List<MeterSummary> bauSavings = new ArrayList<MeterSummary>(Arrays.asList(savings));
		// Get the total savings
		if (cost == null) {
			totalsavingsMeter = new MeterSummary();
			totalsavingsMeter.meterName = "Grand Total";
			totalsavingsMeter.meterType = MeterSummary.TOTAL_COST;
			totalsavingsMeter.year = this.year;
		} else {
			totalsavingsMeter = cost.createTotalMeterCostSummay(bauSavings);
		}
		// Quick and dirty hack to get some output without column order or sub-totals.
		allbauSavings = bauSavings;

		// Add the total cost Meter to the end of the bauSavings.
		allbauSavings.add(totalsavingsMeter);

		return (createSummaryTableModel(allbauSavings));

	}

	/**
	 * Convert the information in MeterSummary form to a TableModel form.
	 * 
	 * @param allMeterSummaries Cost Savings in MeterSummary Form. 
	 * 
	 * @return Table model containing Potential savings.
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
		String[][] costData = createSummaryOutput(allMeterSummaries);

		CEREITableModel costTableModel = new CEREITableModel(costData, columnNames);

		return costTableModel;
	}

	/**
	 * Convert the information in MeterSummary form to a 2-dimensional array of Strings.
	 * 
	 * @param meterSummaries Cost Savings in MeterSummary Form.
	 * 
	 * @return Two dimensional array containing cost savings.
	 */
	protected String[][] createSummaryOutput(List<MeterSummary> meterSummaries) {
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
					outputData[i * 4 + j][k + 3] = df.format(meterSummaries.get(k).monthly[i * 3 + j]);
				}
			}
			outputData[i * 4 + j][0] = ""; // No year in this row
			outputData[i * 4 + j][1] = "Q" + Integer.toString(i + 1);
			outputData[i * 4 + j][2] = ""; // No month in this row
			for (int k = 0; k < meterSummaries.size(); k++) {
				outputData[i * 4 + j][k + 3] = df.format(meterSummaries.get(k).quarterly[i]);
			}
		}
		outputData[16][0] = Integer.toString(year); // Year in last row
		outputData[16][1] = ""; // No quarter in this row
		outputData[16][2] = ""; // No month in this row
		for (int k = 0; k < meterSummaries.size(); k++) {
			outputData[16][k + 3] = df.format(meterSummaries.get(k).yearly);
		}
		return outputData;
	}

}
