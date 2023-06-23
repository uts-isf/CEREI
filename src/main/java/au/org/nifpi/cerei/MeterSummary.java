package au.org.nifpi.cerei;

/**
 * Holds summary cost OR PEI results for each meter.  
 * Could do with refactoring so that there is a base class with child classes for each of cost and PEI
 * 
 * @author James Sargeant
 */
public class MeterSummary {

	// State of the Meter for future state checking
	/** Meter created but contains no data */
	public static final int CREATED = 0;
	/** Meter contains data but has not been used as part of a subtotal */
	public static final int LOADED = 1;
	/** Meter contains data and has been used as part of one or more subtotals */
	public static final int INCLUDED_IN_SUBTOTAL = 2;
	
	//The type of meter for future processing and state checking
	/** Meter contains cost data for a real meter */
	public static final int REAL_COST = 0;
	/** Meter contains cost data for a subtotal meter */
	public static final int SUBTOTAL_COST = 1;
	/** Meter contains cost data for a Grand total meter */
	public static final int TOTAL_COST = 2;
	/** Meter contains PEI data for a real meter */
	public static final int REAL_PEI = 10;
	/** Meter contains PEI data for a subtotal meter */
	public static final int SUBTOTAL_PEI = 11;
	/** Meter contains PEI data for a Grand total meter */
	public static final int TOTAL_PEI = 12;
	
	// Meter Data
	/** Name of the meter */
	protected String meterName;
	/** Data year */
	protected int year;
	/** Meter type, REAL, SUBTOTAL, TOTAL into COST, PEI */
	protected int meterType;
	/** Meter processing status */
	protected int meterProcessingStatus;
	/** Holds one value per month */
	protected double[] monthly = new double[12];
	/** Holds one value per quarter */
	protected double[] quarterly = new double[4];
	/** Holds one value for the entire year */
	protected double yearly=0;

	// For PEI Calculations.
	/** PEI sumTotalCharge */
	protected double[] sumTotalCharge = new double[12];
	/** PEI sumTotalChargeUsage */
	protected double[] sumTotalChargeUsage = new double[12];
	/** PEI monthlyUsage */
	protected double[] monthlyUsage = new double[12];
	/** PEI number of measurements for this month.  Used to calculate the mean */
	protected int[] numberOfMeasurements = new int[12];
	/** PEI number of days in each month */
	int[] daysInMonth = new int[12];
	
	/**
	 * Create an empty MeterSummary object containing zero totals.
	 */
	public MeterSummary() {
		this.meterProcessingStatus = CREATED;
		for (int i=0; i<12; i++) {
			monthly[i]=0;
			sumTotalCharge[i] = 0;
			sumTotalChargeUsage[i]=0;
			monthlyUsage[i] = 0;
			daysInMonth[i] = 0;
			numberOfMeasurements[i] = 0;
		}
		for(int i=0; i<4 ; i++) {
			quarterly[i]=0;
		}
	}
	
	/**
	 * Process the relevant detailed cost structure and calculate the monthly, quarterly and yearly costs 
	 * 
	 * @param costsPerMonth THe detailed cost information associated with this meter
	 */
	public void addCost(CostPerMonth[] costsPerMonth) {
		this.year = costsPerMonth[0].year;
		this.meterType  = REAL_COST;
		this.meterName = costsPerMonth[0].meterName;
		int i;
		int j;
		//Quarter by quarter
		for(i=0; i<4; i++) {
			// Month by month
			for(j=0;j<3;j++) {
				double cost; 
				cost = costsPerMonth[i*3+j].totalChargeIncGST;
				monthly[i*3+j] = cost;
				quarterly[i] += cost;
				yearly += cost;
			}
		}
		this.meterProcessingStatus = LOADED;	
	}
	
	/**
	 * Process the relevant detailed cost structure and calculate the monthly, quarterly and yearly PEIs
	 * 
	 * @param peisPerMonth The detailed pei information associated with this meter
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs
	 */
	public void addEei(PEIPerMonth[] peisPerMonth) throws Exception {
		this.year = peisPerMonth[0].year;
		this.meterType = REAL_PEI;
		this.meterName = peisPerMonth[0].meterName;
		int i;
		int j;
		double yearlyTotalCharge30min = 0;
		double yearlyTotalChargeUsage30min = 0;
		double yearlyUsage = 0;
		int numberOfMeasurementsInYear = 0;

		// Quarter by Quarter
		for (i = 0; i < 4; i++) {
			double quarterlyTotalChargeUsage30min = 0;
			double quarterlyTotalCharge30min = 0;
			double quarterlyUsage = 0;
			int numberOfMeasurementsInQuarter = 0;
			// Month by Month
			for (j = 0; j < 3; j++) {
				// Keep a number of accumulators for the Total PEI calculation and any
				// subTotals.
				sumTotalCharge[3 * i + j] = peisPerMonth[3 * i + j].sumMonthlyTotalCharge;
				sumTotalChargeUsage[3 * i + j] = peisPerMonth[3 * i + j].sumMonthlyTotalChargeUsage;
				monthlyUsage[3 * i + j] = peisPerMonth[3 * i + j].monthlyUsage;
				daysInMonth[3 * i + j] = peisPerMonth[3 * i + j].daysInMonth;
				numberOfMeasurements[3 * i + j] = peisPerMonth[3 * i + j].numberOfMeasurements;

				// Calculate the monthly PEI. Equation 24
				monthly[3 * i + j] = PEI.calculatePEI(peisPerMonth[3 * i + j].sumMonthlyTotalCharge,
						peisPerMonth[3 * i + j].sumMonthlyTotalChargeUsage, peisPerMonth[3 * i + j].monthlyUsage,
						peisPerMonth[3 * i + j].numberOfMeasurements);

				// Accumulators for monthly and yearly PEIs
				quarterlyTotalCharge30min += peisPerMonth[3 * i + j].sumMonthlyTotalCharge;
				quarterlyTotalChargeUsage30min += peisPerMonth[3 * i + j].sumMonthlyTotalChargeUsage;
				quarterlyUsage += peisPerMonth[3 * i + j].monthlyUsage;
				numberOfMeasurementsInQuarter += peisPerMonth[3 * i + j].numberOfMeasurements;

				yearlyTotalCharge30min += peisPerMonth[3 * i + j].sumMonthlyTotalCharge;
				yearlyTotalChargeUsage30min += peisPerMonth[3 * i + j].sumMonthlyTotalChargeUsage;
				yearlyUsage += peisPerMonth[3 * i + j].monthlyUsage;
				numberOfMeasurementsInYear += peisPerMonth[3 * i + j].numberOfMeasurements;
			}
			// EQ 25
			quarterly[i] = PEI.calculatePEI(quarterlyTotalCharge30min, quarterlyTotalChargeUsage30min, quarterlyUsage,
					numberOfMeasurementsInQuarter);
		}
		// EQ 26
		yearly = PEI.calculatePEI(yearlyTotalCharge30min, yearlyTotalChargeUsage30min, yearlyUsage,
				numberOfMeasurementsInYear);
		this.meterProcessingStatus = LOADED;
	}

}
