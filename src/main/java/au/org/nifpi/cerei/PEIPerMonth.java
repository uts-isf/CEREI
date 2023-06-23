package au.org.nifpi.cerei;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * Low level working class that provides most of the detail functionality used to calculate PEIs
 * Could do with refactoring to separate out Cost and PEI with some commonality in a yet to be designed parent class
 * 
 * @author James Sargeant
 */
public class PEIPerMonth {
	// Monthly rates determined from Meter Montly Paramters which are loaded from Tariff file
	/** Spot Price Loss ratio per meter per month.  Loaded from General Parameter file */
	protected double spotPriceLossRatio = 0;
	/** Feed In Tariff Loss ratio per meter per month.  Loaded from General Parameter file */
	protected double feedInLossRatio = 0;
	
	// Usage 
	/** Accumulate power use for the month in kWh */
	protected double monthlyUsage=0;
	/** Accumulate power generated for the month in kWh */
	protected double monthlyGenerated=0;
	/** Accumulate net power used for the month in kWh (monthlyUsage - monthlyGenerated)*/
	protected double monthlyNett=0;

	
	// Aggregate Usages
	/** Peak power used over the month */ 
	protected double peakUsage=0;
	/** Shoulder power used over the month */ 
	protected double shoulderUsage=0;
	/** OffPeak power used over the month */ 
	protected double offpeakUsage=0;
	
	// EEI accumulators
	/** Rolling 30 minute accumulation of the (total 30 minute price in c/kWh) for the month */ 
	protected double sumMonthlyTotalCharge = 0;
	/** Rolling 30 minute accumulation of the (total 30 minute price in c/kWh multiple by the 30 minute usage) for the month */  
	protected double sumMonthlyTotalChargeUsage = 0;
	/** Number of lines of data in usage and price files for the month */
	protected int numberOfMeasurements = 0;
	
	//Price Efficiency Index for the month - calculated at the end of processing input
	/** Price Efficiency Index for the month */
	protected double peiMonthly;
	
	// Miscellaneous
	/** Year for which results are being generated */
	protected int year;
	/** Month for which results are being generated */
	protected int month;
	/** Name of the meter */
	protected String meterName;
	/** Number of days in this particular month: 28, 29, 30 or 31 as appropriate */
	protected int daysInMonth;
	/** Number of days in this year: 365 or 366 as appropriate */
	protected int daysInYear;

	/**
	 * Create a data structure to hold the cost and pei data for a single meter for a single month.
	 *   
	 * @param meterName The name of the meter that this information pertains to.  
	 * @param month The month of the year that this information pertains to.
	 * @param year The calendar year that this information pertains to.
	 */
	public PEIPerMonth(String meterName, int month, int year) {
		// Save incoming information in object variables
		this.meterName = meterName;
		this.year = year;
		this.month=month;
		
		// Leap year processing
		daysInYear = Year.of(year).length();
		if (month==1) {
			if (daysInYear==366) {
				daysInMonth=29;
			}
			else {
				daysInMonth=28;
			}
		}
		// days in this particular month
		else if (month==0 || month==2 || month==4 || month==6 || month==7 || month==9 || month==11) {
			daysInMonth=31;
		}
		else {
			daysInMonth=30;
		}
	}
	
	/**
	 * Load the Spot Price Loss Ratio and the Feed-in Loss Ratio from the relevant parameters for that particular month
	 * 
	 * @param monthlyParameter The parameters associated with the month under consideration
	 */
	protected void addMonthlyParameters(MonthlyParameter monthlyParameter) {
		this.spotPriceLossRatio = monthlyParameter.spotPriceLossRatio;
		this.feedInLossRatio = monthlyParameter.feedInLossRatio;
	}
	
	/**
	 * Inner loop that processes each 30 minute spot price and usage to accumulate overall usage and 30 minute PEI accumulators
	 * The PEI calculations can be optimised by removing them from this inner loop.  However, that would make understanding the code more difficult.  For now, inner loop left as inefficient
	 *  
	 * @param networkParameters Complete Energy charge, Network charge, Spot Energy, Market charge and Other Charge Loss ratios and pricing.
	 * @param recordDateTime Timestamp for this particular reading
	 * @param usage energy used from grid
	 * @param spotPrice Spot price that applies to this particular reading
	 * @param generated energy generated
	 * @param feedInTariff Feed in tarrif that applies to this particular reading
	 */
	// Used to add up the total usage, aggregate usages and spot price charges.
	protected void addUnitOfCharges(NetworkParameter networkParameters, LocalDateTime recordDateTime, double usage, double spotPrice, double generated, double feedInTariff) {
		double veetCharge30min, sresCharge30min, lretCharge30min, aemoPoolRertCharge30min,ancillaryServicesCharge30min;
		double SpotPriceIncLossCharge30min;
		double totalCharge30min=0;
		double nett;
		int usageType;
		
		// Get the month and hour and minute from the usage date stamp.
		int hour = recordDateTime.getHour(); //Already in the range 0 - 23
		int minute = recordDateTime.getMinute(); // In the range 0 - 59
		int dayOfWeek = recordDateTime.getDayOfWeek().getValue()-1; //Minus 1 to turn day into our array index

		// Get the rates from the tariff structure
		double veetRate = networkParameters.parameters[this.month].veetRate;
		double sresRate = networkParameters.parameters[this.month].sresRate;
		double lretRate = networkParameters.parameters[this.month].lretRate;
		double aemoPoolRertRate = networkParameters.parameters[this.month].aemoPoolRertRate;
		double ancilliaryServicesRate = networkParameters.parameters[this.month].ancilliaryServicesRate;
		double peakRate = networkParameters.parameters[this.month].peakRate;
		double shoulderRate = networkParameters.parameters[this.month].shoulderRate;
		double offpeakRate = networkParameters.parameters[this.month].offpeakRate;
		
		// Start processing this 30 minutes' data
		 
		// Calculate net usage for the month
		nett = usage - generated;
		if ( nett <  0) {
			nett = 0;
		}
		monthlyUsage += usage;
		monthlyGenerated += generated;
		monthlyNett += nett;

		//determine the 30min charges for EEI calculations
		// Client insists on the "divide by 2".  Implemented as per client requirements but
		// developer not convinced that this is correct. 
		// Developer's concern:
		//		All the "rates" are in c/kWh.  kWh is a measure of "energy", so multiplying the 
		//		rate by the usage ends up with the charge (in c) FOR THAT 30 MINUTES.  
		//		Dividing by 2 to get a 30min value is thus not required (i.e. is incorrect).
		// Fortunately, the numerator and the denominator of the PEI calculation both have the
		// divide by two for most of their components, so the divide by 2 will mostly cancel out.
		// Not performing the "divide by 2" typically results in a reduction of the PEI in the 
		// order 0.1 - 0.3.
		
		// A lot of this could be moved out of this inner loop, but left in here to be consistent with documentation 
		// and easer to understand.
		veetCharge30min = (veetRate + veetRate*networkParameters.parameters[this.month].veetLossRatio)/2;
		sresCharge30min = (sresRate + sresRate*networkParameters.parameters[this.month].sresLossRatio)/2;
		lretCharge30min = (lretRate + lretRate*networkParameters.parameters[this.month].lretLossRatio)/2;
		aemoPoolRertCharge30min =(aemoPoolRertRate + aemoPoolRertRate*networkParameters.parameters[this.month].aemoPoolRertLossRatio)/2;
		ancillaryServicesCharge30min = (ancilliaryServicesRate + ancilliaryServicesRate*networkParameters.parameters[this.month].ancilliaryServicesLossRatio)/2;
		SpotPriceIncLossCharge30min = spotPrice + (spotPrice * this.spotPriceLossRatio/2);

		// Add up all the 30 minute charges (except peak, shoulder and offpeak charges) for EEI
		totalCharge30min = 
				veetCharge30min + 
				sresCharge30min + 
				lretCharge30min + 
				aemoPoolRertCharge30min +
				ancillaryServicesCharge30min +
				SpotPriceIncLossCharge30min;  //EQ 23
		
		// Find out whether the applicable rate is PEAK, SHOULDER or OFFPEAK
		int tariffRateIndex = 2*hour + 2*minute/60;
		usageType = networkParameters.rate[dayOfWeek][tariffRateIndex];
		
		// Accumulate the PEAK, SHOULDER or OFFPEAK as appropriate and also add charge to EEI 
		// Look out - more divide by two's here!
		switch (usageType) {
		case NetworkParameter.PEAK:
			peakUsage += nett;
			totalCharge30min += peakRate/2;
			//totalCharge30min += peakRate;
			break;
		case NetworkParameter.SHOULDER:
			shoulderUsage += nett;
			totalCharge30min += shoulderRate/2;
			//totalCharge30min += shoulderRate;
			break;
		default:
			offpeakUsage += nett;
			totalCharge30min += offpeakRate/2;
			//totalCharge30min += offpeakRate;
		}
		
		//Accumulate (totalcharge30min and (totalcharge30min x usage30min) in preparation for EQ24, EQ25 and EQ26.
		sumMonthlyTotalCharge += totalCharge30min;
		// Divide by 2 as required by client.
		sumMonthlyTotalChargeUsage += totalCharge30min*nett;
		
		// Count up the number of measurements taken in a month
		numberOfMeasurements++;
		
	}
	
	/**
	 * Calculate all charges once usage and price files for that meter and month processed.
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs	 
	 * 
	 */
	protected void calcuateCharges() throws Exception {
		peiMonthly = PEI.calculatePEI(sumMonthlyTotalCharge, sumMonthlyTotalChargeUsage, monthlyUsage, numberOfMeasurements);
	}

}
