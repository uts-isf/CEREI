package au.org.nifpi.cerei;

/**
 * Collect the per meter per month parameters of Loss Ratio Demand Capacity Usage and Demand Critical Peak Usage
 * 
 * @author James Sargeant
 */
public class MonthlyParameter {
	/** month not currently used - here to be used to match months */
	protected String month;
	/** Per meter per month spot price loss Ratio */
	protected double spotPriceLossRatio;
	/** Per meter per month feed-in tariff loss Ratio */
	protected double feedInLossRatio;
	/** Per meter per month Demand Capacity Charge */
	protected double demandCapacityUsage;
	/** Per meter per month Demand Critical Peak Usage */
	protected double demandCriticalPeakUsage;
	
	/**
	 * Load the per meter per month parameters from the appropriate line in the input file.
	 * Fragile.  Parameters must be in the correct order.
	 * 
	 * @param meterName name of the meter that this line pertains to
	 * @param st Array containing month, loss Ratio, Demand Capacity Usage and Demand Critical Peak Usage IN THAT ORDER
	 * 
	 * @throws Exception If any of the required parameters are missing
	 */
	public MonthlyParameter (String meterName, String[] st) throws Exception {
		String cumulativeErrorString = "";
		
		if (st.length < 1) {
			throw new Exception("Missing monthly specific parameters for meter "+meterName);
		}
		// Load them up!
		month = st[0];
		try {
			spotPriceLossRatio = Double.parseDouble(st[1]);
		}
		catch (Exception e) {
			cumulativeErrorString += "\tLoss Ratio (Spot Price) for meter "+meterName+" for "+month+" is not a number\n";
		}
		try {
			feedInLossRatio = Double.parseDouble(st[2]);
		}
		catch (Exception e) {
			cumulativeErrorString += "\tLoss Ratio (Feed-in) for meter "+meterName+" for "+month+" is not a number\n";
		}
		try {
			demandCapacityUsage = Double.parseDouble(st[3]);
		}
		catch (Exception e) {
			cumulativeErrorString += "\tDemand Capacity for meter "+meterName+" for "+month+" is not a number\n";
		}
		try {
			demandCriticalPeakUsage = Double.parseDouble(st[4]);
		}
		catch (Exception e) {
			cumulativeErrorString += "\tDemand Critical Peak for meter "+meterName+" for "+month+" is not a number\n";
		}
		
		//If there was a problem with any of the monthly parameters for this meter throw an exception.
		if (cumulativeErrorString.compareTo("") != 0) {
			throw new Exception(cumulativeErrorString);
		}

	}
}
