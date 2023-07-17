package au.org.nifpi.cerei;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Loads the various tariffs, per monthly and per meter per monthly from the "Network Tariff" input file
 * TO DO - Validation of all fields in the input file.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class NetworkParameter {

	/** Index for Monday */
	public static final int MONDAY = 0;
	/** Index  for Tuesday*/
	public static final int TUESDAY = 1;
	/** Index  for Wednesday */
	public static final int WEDNESDAY = 2;
	/** Index  for Thursday */
	public static final int THURSDAY = 3;
	/** Index  for Friday */
	public static final int FRIDAY = 4;
	/** Index  for Saturday */
	public static final int SATURDAY = 5;
	/** Index  for Sunday */
	public static final int SUNDAY = 6;
	/** Return value for an invalid day substring */
	public static final int INVALID_DAY = -1;
	
	/** Constant for PEAK */
	public static final int PEAK=1;
	/** Constant for Shoulder*/
	public static final int SHOULDER=2;
	/** Constant for Offpeak*/
	public static final int OFFPEAK=3;

	/** Name of the tariff */
	protected String tariffName;
	/** Holds per month parameters */
	protected Parameter[] parameters = new Parameter[12];
	/** Per meter per month parameters */
	protected List<Meter> meters = new ArrayList<Meter>();
	/** If true, distribute generation across multiple meters */

	/** If true, distribute generation across multiple meters */
	protected boolean distributeGeneration = false;
	/** Holds the names of meters we distribute generated power over */
	protected List<String> distributionMeters = new ArrayList<String>();
	/** True if there is a specified list of meters to distribute power over */
	private boolean specifiedDistributionList=false;
	
	//rate array is in 30 minute intervals to make it easy to index into given that usage data is
	//in 30 minute intervals.  Index is simply calculate by 2*(hour + (min / 60)).
	//	hour is 0 to 23, min is 0 or 30.  Indexes thus run from 0 to 47.
	//  Note that as usage is 30 minutes in arrears, we have to subtract 1 from the index to get the
	//  correct rate!
	/** Array containing 7 days * 48 (30 minute) periods per day that shows what rate (Peak, Shoulder or Offpeak) is applicable */
	protected int[][] rate = new int[7][48];
	
	/**
	 * Open up the General Parameter file (aka Tariff) ready for reading.  Line by line reading handled by method loadTariff
	 * 
	 * @param tariffFile InputFile containing name of file to read
	 * 
	 * @throws Exception If there is a problem opening or reading the file.
	 */
	public NetworkParameter(InputFile tariffFile) throws Exception {
		//initaliase the rate array to all OFFPEAK.
		for (int i=0; i<7;i++) {
			for (int j=0; j<48;j++) {
				rate[i][j]=NetworkParameter.OFFPEAK;
			}			
		}
    	try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tariffFile.inputFile)),16384)) {
    		loadTariff(reader, tariffFile.inputFile.getName());
    	}
	}

	/**
	 * Read and parse major blocks of the General Parameter file.
	 * 
	 * @param reader Access to the General Parameter file
	 * @param filename Name of the Business as Usual file.
	 * 
	 * @throws Exception if something goes wrong reading and parsing the general parameter file
	 */
	private void loadTariff(BufferedReader reader, String filename) throws Exception {
		String lineFromFile,key;
		String[] st;
		// A cumulative error string so we can find all of the problems with the Network Tariff file in one pass.
		String cumulativeErrorString = "";

		// Line by line
		try {
			while ((lineFromFile = reader.readLine()) != null) {
				st = parseLineFromFile(lineFromFile);
				// Keep processing if there is anything on this line at all, otherwise read next line
				if(st.length>0) {
					//Get rid of annoying white space.
					key = st[0].replaceAll("\\s", "").toLowerCase(Locale.ENGLISH);
					
					//Parse
					switch(key) {
					case "tariff":
						if (st.length < 2) {
							cumulativeErrorString += "Network Tariff file is missing Tariff Name\n";
						}
						else {
							tariffName = st[1];
						}
						break;
					case "peak":
						//Peak rate will be in next token, but loadRate will do the work
						cumulativeErrorString += loadRate(NetworkParameter.PEAK,st);
						break;
					case "shoulder":
						//Shoulder rate will be in next token, but loadRate will do the work
						cumulativeErrorString += loadRate(NetworkParameter.SHOULDER,st);
						break;
					case "offpeak":
						// Do nothing - rate preloaded with OFFPEAK
						break;
					case "generation":
						// If the word "generation is followed by the word "distributed" then we distribute generated power across existing real meters.  
						if (st.length > 1 && st[1].compareToIgnoreCase("distributed")==0) {
							distributeGeneration = true;
							// If "distributed" is followed by existing cells then assume these cells are the names of real meters.
							if (st.length > 2) {
								specifiedDistributionList=true;
								for (int i = 2; i <st.length; i++) {
									String meterName = st[i].replaceAll("\\s", "");
									//remove "(kWh)"
					    			int leftParen = meterName.indexOf("(");
					    			if (leftParen != -1) {
					    				meterName = meterName.substring(0,leftParen); 
					    			}
									distributionMeters.add(meterName);
								}
							}
						}
						break;
					case "general":
						// The next 13 lines contain the per month parameters, but loadGlobale will do the work 
						cumulativeErrorString += loadGlobalParameters(reader);
						break;
					case "meter":
						// Token next to "Meter" contains the meter name, which we need to cross reference to 
						// the usage files.  Add error message if it is missing.
						if(st.length < 2) {
							cumulativeErrorString += "Unamed meter in Tariff File\n";
						}
						else {
							//Get rid of annoying white space.
							String meterName = st[1].replaceAll("\\s", "");
							//remove "(kWh)"
			    			int leftParen = meterName.indexOf("(");
			    			if (leftParen != -1) {
			    				meterName = meterName.substring(0,leftParen); 
			    			}
							// The next 13 lines contain the per month parameters, but loadMeterParameters will do the work 
			    			cumulativeErrorString += loadMeterParameters(meterName,reader);
						}
						break;
					default:
						// Do nothing - rubbish line.
					}
				}
			}
			if (cumulativeErrorString.compareTo("") != 0) {
				throw new Exception("Problems with the Network Tariff file:\n"+cumulativeErrorString);
			}
		}
		catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Grab the next 13 lines (one header and one line per month) as they contain the per month parameters.
	 * 
	 * @param reader Access to the General Parameter file.
	 * 
	 * @throws Exception from called methods - most likely insufficient number of parameters in one of the rows.
	 * 
	 * @return String containing any validation error messages.
	 */
	private String loadGlobalParameters(BufferedReader reader) throws Exception {
		String lineFromFile;
		// A cumulative error string so we can find all of the problems with the General Parameters in the Network Tariff file in one pass.
		String cumulativeErrorString="";
		
		// Discard the table header. A future version might process this so that the
		// order of parameters isn't critical
		lineFromFile = reader.readLine();
		
		// Month by month for the next 12 lines
		for (int i = 0; i < 12; i++) {
			if ((lineFromFile = reader.readLine()) != null) {
				// Plonk the values into the parameters array
				try {
					parameters[i] = new Parameter(lineFromFile);
				}
				catch (Exception e) {
					cumulativeErrorString += e.getMessage();
				}
			} else {
				cumulativeErrorString += "Not all months present\n";
			}
		}
		return cumulativeErrorString;
	}

	/**
	 * Change the 7 day * 48 30minuteTimeslot array that shows the applicable rate (Peak, Shoulder or Offpeak) to a new
	 * rate type, based on the times of day specified in subsequent tokens
	 * Fragile.  Only considers hours!
	 * 
	 * @param rateType new rate type - PEAK, SHOULDER or OFFPEAK (see constants)
	 * @param st Rest of the line following Peak, Shoulder or Offpeak
	 * 
	 * @return String containing any validation error messages.
	 */
    private String loadRate(int rateType,String[] st) {
    	// Start and End times
    	int start=0, end=0;
    	// Start and end days if specified as a range
    	int firstday, lastday;
    	
		// A cumulative error string so we can find all of the problems with the Peak and Shoulder rates in the Network Tariff file in one pass.
    	String cumulativeErrorString="";
    	
    	// Wrap rate into next day if endTime is 24:00
    	boolean wrapRateIntoNextDay = false;
    	if (st.length < 2) {
    		cumulativeErrorString += st[0]+" has no associated times";
    		return cumulativeErrorString;
    	}
    	// Time period by time period
    	for (int i=1; i< st.length; i++) {
    		String daytime = st[i];
    		// Tokens within the next comma separated value in file are separated by spaces
    		String[] daysAndTimes = daytime.split(" ");
    		if (daysAndTimes.length < 2) {
    			cumulativeErrorString += st[i]+" missing start and end times for "+st[0]+" rates\n";
    		}
    		if (daysAndTimes.length < 3) {
    			cumulativeErrorString += st[i]+" missing end time for "+st[0]+" rates\n";
    		}
    		if (daysAndTimes.length > 3) {
    			cumulativeErrorString += st[i]+". Invalid format for "+st[0]+" rates.  Must be <days> <starttime> <endtime>.\n";
    		}
	    	if (daysAndTimes.length == 3) {
	    		// We can proceed
	    		
	    		// First part of the "cell is a string representing days
	    		String days = daysAndTimes[0];
	    		// Second part of the "cell is a string representing start time in H:mm.  However, only H is considered
	    		String startTime = daysAndTimes[1];
	    		// Last part of the "cell is a string representing start time in H:mm.  However, only H is considered
	    		String endTime = daysAndTimes[2];
	    		//start and end are used as indexes into the rate array.
	    		try {
	    			String[] startString = startTime.split(":");
	    			start = Integer.parseInt(startString[0])*2;
	    			if (start < 0) {
	    				cumulativeErrorString += st[i] + ". "+startTime+" is not a valid start time for "+st[0]+" rates\n";
	    			}
	    			if (start > 47) {
	    				cumulativeErrorString += st[i] + ". Start time must not be greater than 23:00 for "+st[0]+" rates\n";
	    			}
	    		}
	    		catch (Exception e) {
	    			cumulativeErrorString += st[i]+". "+startTime+ " is not a valid start time for "+st[0]+" rates\n";
	    		}
	    		try {
	    			String[] endString = endTime.split(":");
	    			end = Integer.parseInt(endString[0])*2; //might be minus 1 depending on input file format
	    			if (end < 0) {
	    				cumulativeErrorString += st[i] + ". "+endTime+" is not a valid end time for "+st[0]+" rates\n";
	    			}
	    			else if (end < 2) {
	    				cumulativeErrorString += st[i] + ". End time must not be less than 01:00 for "+st[0]+" rates\n";
	    			}
	    			if (end > 47) { // Need to wrap into the next day
	    				wrapRateIntoNextDay = true;
	    			}
	    		}
	        	catch (Exception e) {
	        		cumulativeErrorString += st[i]+". "+endTime+ " is not a valid end time for "+st[0]+" rates\n";
	        	}
	    		String[] day = days.split(";"); //get all day ranges and discrete days 
	    		for (int j=0; j < day.length; j++) {
	    			String dayRange = day[j];
	    			// Day range if there is a "-" in the days part of the "cell"
	    			String[] alldays = dayRange.split("-");
	    			// Get all the affected days.  If there is no '-' there will be only one day.  Convert to lower case!
	    			String startday = alldays[0].substring(0,3).toLowerCase(Locale.ENGLISH);
	    			String endday = alldays[alldays.length-1].substring(0,3).toLowerCase(Locale.ENGLISH);
	    			// Get the starting day index from startday
	    			if ((firstday = findDayFromString(startday)) == NetworkParameter.INVALID_DAY) {
	    				cumulativeErrorString += startday+" is not a day of the week in "+st[i]+ " for "+st[0]+" rates\n"; 
	    			}
	    			// Get the ending day index from endday
	    			if ((lastday = findDayFromString(endday)) == NetworkParameter.INVALID_DAY && lastday != firstday) {
	    				cumulativeErrorString += endday+" is not a day of the week in "+st[i]+ " for "+st[0]+" rates\n"; 
	    			}
	    			if (lastday < firstday) {
	    				cumulativeErrorString += st[i]+". "+dayRange+" is invalid.  Days must run from Mon-Sun for "+st[0]+" rates\n"; 
	    			}
	    			if (end < start) {
	    				cumulativeErrorString += st[i]+". "+daysAndTimes[1]+" "+daysAndTimes[2]+" is invalid. Start time must be before end time for "+st[0]+" rates\n"; 
	    			}
	    			//Adjust end if we have have to wrap because endtime was 24:00;
	    			if (wrapRateIntoNextDay) {
	    				// Don't overrun the rate array - it only goes from 0 to 47
	    				end = 47;
	    			}
	    			// Provided all days and times are valid we can update the rate array
	    			if (firstday != NetworkParameter.INVALID_DAY && lastday != NetworkParameter.INVALID_DAY
	    					&& firstday <= lastday
	    					&& start < end && start >= 0 && start < 48 && end >=0 && end <48) {
		    			// Set all the elements in the rate array for the affected days to the passed in rate type (PEAK, SHOULDER, OFFPEAK)
		   				for(int k=firstday; k<=lastday;k++) {
		   					for(int l=start; l<end; l++) {
		   						rate[k][l]=rateType;
		   					}
		   	    			if (wrapRateIntoNextDay) {
		   	    				//Set the first time period of the next day to the rateType
		   	    				rate[(k+1)%7][0]=rateType;
		   	    			}
		   				}
	    			}
	    		}
    		}
    	}
    	return cumulativeErrorString;
    }

    /**
	 * Grab the next 13 lines (one header and one line per month) as they contain the per meter per month parameters 
	 * 
     * @param meterName the name of the meter affected.
	 * @param reader Access to the General Parameter file
	 * 
 	 * @throws Exception from called methods - most likely insufficient number of parameters in one of the rows
 	 * 
	 * @return String containing any validation error messages.
	 */
    public String loadMeterParameters(String meterName, BufferedReader reader) throws Exception {
		// A cumulative error string so we can find all of the problems with the meter specific parameters in the Network Tariff file in one pass.
    	String cumulativeErrorString="";
    	
		try {
	    	//Discard the table header
	    	reader.readLine();
	    	// Add the monthly specific meter specific parameters to a new meter. 
			meters.add(new Meter(meterName,reader));
		}
		catch (Exception e) {
			cumulativeErrorString += e.getMessage();
		}
		return cumulativeErrorString;
    }
    
    /**
     * Convert a day string in the general parameter file to an index into the rate array
     * 
     * @param dayString Three letter day string - mon, tue, wed, thu, fri, sat or  sun. 
     * 
     * @return integer representing day of week, 0=mon, 1=tue, ... 6=sun.
     */
    public int findDayFromString(String dayString) {
    	int day;
		switch (dayString) {
		case "mon":
			day=NetworkParameter.MONDAY;
			break;
		case "tue":
			day=NetworkParameter.TUESDAY;
			break;
		case "wed":
			day=NetworkParameter.WEDNESDAY;
			break;
		case "thu":
			day=NetworkParameter.THURSDAY;
			break;
		case "fri":
			day=NetworkParameter.FRIDAY;
			break;
		case "sat":
			day=NetworkParameter.SATURDAY;
			break;
		case "sun":
			day=NetworkParameter.SUNDAY;
			break;
		default:
			day=NetworkParameter.INVALID_DAY;
		}
    	return day;
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

	/**
	 * Does the "Network Tariff" parameter .csv file contain a specific list of meters to distribute generated energy across?
	 * 
	 * @return Whether or not a specific list of meters to distribute generated energy across exists.
	 */
	public boolean isSpecifiedDistributionList() {
		return specifiedDistributionList;		
	}


}

