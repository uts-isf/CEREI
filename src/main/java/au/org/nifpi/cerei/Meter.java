package au.org.nifpi.cerei;

import java.io.BufferedReader;

/**
 * 12 months worth of parameters that pertain to a particular meter.
 * 
 * @author James Sargeant
 */
public class Meter {
	/** Meter Name */
	protected String meterName;
	/** Per month parameters for the particular meter */
	protected MonthlyParameter[] monthlyParameters = new MonthlyParameter[12];
	
	/**
	 * Create the set of monthly parameters that apply to all meters by reading the next 12 lines of the "General Parameters" (aka Tariff) file
	 * Fragile.  Currently meter parameters have to be in a specific sequence. 
	 * 
	 * @param meterName The name of the meter that these parameters apply to
	 * @param reader The Buffered reader reading the file that is the source of the information
	 * 
	 * @throws Exception Rethrows exception from called methods.
	 */
	public Meter(String meterName, BufferedReader reader) throws Exception {
		this.meterName = meterName;
		String cumulativeErrorString="";
		int i;
		// Month by month
		for (i = 0; i < 12; i++) {
			String lineFromFile = reader.readLine();
			String[] st = parseLineFromFile(lineFromFile);
			try {
				monthlyParameters[i] = new MonthlyParameter(meterName, st);
			}
			catch (Exception e) {
				cumulativeErrorString += e.getMessage();
			}
		}
		
		if (cumulativeErrorString.compareTo("") != 0) {
			throw new Exception(cumulativeErrorString);
		}

	}
	
	/**
	 * Tokenise the line from the "Network Tariff" parameter .csv file, removing any double quotes (as they mess up converting the string values to numbers). 
	 * 
	 * @param lineFromFile Line from the "Network Tariff" parameter comma delimited input file.
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

	
}
