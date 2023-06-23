package au.org.nifpi.cerei;

/**
 * Collect the per month parameters that apply to all meters
 * 
 * @author James Sargeant
 */
public class Parameter {
	/** Month for which parameters apply */
	public String month;
	/** The Service and Administration rate in $/month */
	public double serviceAdminRate=0;
	/** The standing Rate in $/year */
	public double standingRate=0;
	/** The demand capacity rate in $/kVA/month */
	public double demandCapacityRate=0;
	/** The demand critical peak rate in $/kVA/month */
	public double demandCriticalPeakRate=0;
	/** Victorian Energy Efficiency Target (VEET) in c/kWh */
	public double veetRate=0;
	/** Loss ratio applicable to the VEET rate */
	public double veetLossRatio=0;
	/** Small Scale Renewable Energy Scheme (SRES) in c/kWh */ 
	public double sresRate=0;
	/** Loss ratio applicable to the SRES rate */
	public double sresLossRatio=0;
	/** Large Scale Renewable Energy Target (LRET) rate in c/kWh */
	public double lretRate=0;
	/** Loss ratio applicable to the LRET rate */
	public double lretLossRatio=0;
	/** AEMO pool charge plus Reliability and Emergency Reserve Trader (RERT) rate in c/kWh */
	public double aemoPoolRertRate=0;
	/** Loss ratio applicable to the AMEOPool+LRET rate */
	public double aemoPoolRertLossRatio=0;
	/** Ancillary Services rate in c/kWh */
	public double ancilliaryServicesRate=0;
	/** Loss ratio applicable to the Ancillary Service rate */
	public double ancilliaryServicesLossRatio=0;
	/** Meter rate in $/year */
	public double meterRate=0;
	/** CT Compliance testing levy in $/year */
	public double ctComplianceTestingRate=0;
	/** Peak energy rate in c/kWh */
	public double peakRate=0;
	/** Shoulder energy rate in c/kWh */
	public double shoulderRate=0;
	/** Offpeak energy rate in c/kWh */
	public double offpeakRate=0;
	
	
	/**
	 * Load the per month parameters from the appropriate line in the input file.
	 * Fragile.  Parameters must be in the correct order.
	 * 
	 * @param lineFromFile Array containing parameters IN THE CORRECT ORDER
	 * 
	 * @throws Exception If any of the required parameters are missing or not numbers.
	 */
	public Parameter(String lineFromFile) throws Exception {
		String cumulativeErrorString="";
		String[] st = parseLineFromFile(lineFromFile);
		
		if (st.length < 1) {
			cumulativeErrorString += "General Parameters: Unexpected empty line\n";
			throw new Exception(cumulativeErrorString);
		}
		month = st[0];

		try {
			serviceAdminRate = Double.parseDouble(st[1]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Service and Admin Charge for "+month+" is not a number\n";
		}
		try {
			standingRate = Double.parseDouble(st[2]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Standing Charge for "+month+" is not a number\n";
		}
		try {
			demandCapacityRate = Double.parseDouble(st[3]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Demand Capacity for "+month+" is not a number\n";
		}
		try {
			demandCriticalPeakRate = Double.parseDouble(st[4]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Demand Critical Peak  for "+month+" is not a number\n";
		}
		try {
			veetRate = Double.parseDouble(st[5]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: VEET Charge for "+month+" is not a number\n";
		}
		try {
			veetLossRatio = Double.parseDouble(st[6]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: VEET Loss Ratio for "+month+" is not a number\n";
		}
		try {
			sresRate = Double.parseDouble(st[7]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: SRES Charge for "+month+" is not a number\n";
		}
		try {
			sresLossRatio = Double.parseDouble(st[8]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: SRES Loss Ratio for "+month+" is not a number\n";
		}
		try {
			lretRate = Double.parseDouble(st[9]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: LRET Charge for "+month+" is not a number\n";
		}
		try {
			lretLossRatio = Double.parseDouble(st[10]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: LRET Loss Ratio for "+month+" is not a number\n";
		}
		try {
			aemoPoolRertRate = Double.parseDouble(st[11]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: AEMO Pool RERT Charge for "+month+" is not a number\n";
		}
		try {
			aemoPoolRertLossRatio = Double.parseDouble(st[12]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: AEMO + RERT Loss Ratio for "+month+" is not a number\n";
		}
		try {
			ancilliaryServicesRate = Double.parseDouble(st[13]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Ancillary Services for "+month+" is not a number\n";
		}
		try {
			ancilliaryServicesLossRatio = Double.parseDouble(st[14]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Ancillary Services Loss Ratio for "+month+" is not a number\n";
		}
		try {
			meterRate = Double.parseDouble(st[15]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Meter Charge for "+month+" is not a number\n";
		}
		try {
			ctComplianceTestingRate = Double.parseDouble(st[16]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: CT Compliance Testing Levy for "+month+" is not a number\n";
		}
		try {
			peakRate = Double.parseDouble(st[17]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Peak Rate for "+month+" is not a number\n";
		}
		try {
			shoulderRate = Double.parseDouble(st[18]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Shoulder Rate for "+month+" is not a number\n";
		}
		try {
			offpeakRate = Double.parseDouble(st[19]);
		}
		catch (Exception e) {
			cumulativeErrorString += "General Parameters: Off-peak Rate for "+month+" is not a number\n";
		}
		
		//If there was a problem with any of the general parameters on this line throw an exception.
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
