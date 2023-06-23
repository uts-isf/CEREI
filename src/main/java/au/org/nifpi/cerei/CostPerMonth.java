package au.org.nifpi.cerei;

import java.time.LocalDateTime;
import java.time.Year;

/**
 * Low level working class that provides most of the detail functionality used to calculate costs and PEIs
 * Could do with refactoring to separate out Cost and PEI with some commonality in a yet to be designed parent class
 * 
 * @author James Sargeant
 */
public class CostPerMonth {
	// Monthly rates determined from Meter Montly Paramters which are loaded from Tariff file
	/** Spot Price Loss ratio per meter per month.  Loaded from General Parameter file */
	protected double spotPriceLossRatio = 0;
	/** Feed in tariff Loss ratio per meter per month.  Loaded from General Parameter file */
	protected double feedInLossRatio = 0;
	/** Demand Capacity Usage per meter per month.  Loaded from General Parameter file */
	protected double demandCapacityUsage=0;
	/** Demand Critical Peak Usage per meter per month.  Loaded from General Parameter file */
	protected double demandCriticalPeakUsage=0;
	
	// Usage 
	// /** Accumulate power use for the month in kWh */ //No longer used
	// protected double monthlyUsage=0; // No longer used
	/** Accumulate 30 min power generated for the month in kWh */ 
	protected double monthlyGenerated=0;
	/** Accumulate net power used for the month in kWh (monthlyUsage - monthlyGenerated)*/
	// protected double monthlyNett=0; // No longer used
	/** Accumulate 30 min actual net energy used from the grid for the month in kWh */
	protected double monthlyNettGridUsed=0;
	/** Accumulate 30 min actual net energy exported to the grid for the month in kWh - will be negative!*/
	protected double monthlyNettExported=0;
	//Energy charges
	/** Pool Pass Through Charge depends on applicable spot price. */
	protected double poolPassThroughCharge=0;
	/** Feed In Charge depends on applicable feed in tariff - typically negative. */
	protected double feedInCharge=0;
	/** Service and Admin charge calculated from rate loaded from General Parameter multiplied by days in month */ 
	protected double serviceAdminCharge=0;
	/** Energy charge is sum of pool pass through charge and service and Admin charge */
	protected double energyCharge=0;
	// Network charges
	/** Standing charge is a fixed amount per year that is converted into an amount per month */ 
	protected double standingCharge=0;
	/** peak energy charge depends on applicable PEAK rate. */
	protected double peakEnergyCharge=0;
	/** shoulder energy charge depends on applicable SHOULDER rate. */
	protected double shoulderEnergyCharge=0;
	/** off peak energy charge depends on applicable OFFPEAK rate. */
	protected double offpeakEnergyCharge=0;
	/** Demand Capacity Charge depends on the relevant per month rate and the per meter per month usage - both in General Parameter file */
	protected double demandCapacityCharge=0;
	/** Demand Critical Peak Charge depends on the relevant per month rate and the per meter per month usage - both in General Parameter file */
	protected double demandCriticalPeakCharge=0;
	/** Network charge is the sum of the 6 Network charges: standing charge, peak Energy charge, shoulder energy charge, offpeak energy charge, demand capacity charge and demand critical peak charge */  
	protected double networkCharge=0;
	// Market charges
	/** Victorian Energy Efficiency Target charge */
	protected double veetCharge=0;
	/** Small Scale Renewable Energy Scheme charge */
	protected double sresCharge=0;
	/** Large Scale Renewable Energy Target charge */
	protected double lretCharge=0;
	/** AEMO pool charge plus Reliability and Emergency Reserve Trader charge */
	protected double aemoPoolRertCharge=0;
	/** Ancilliary Service charge */ 
	protected double ancilliaryServicesCharge=0;
	/** Market charge is the sum of the 5 Market charges: VEET, SRES, LRET, AMEO Pool + RERT. and Ancillary Service */  
	protected double marketCharge=0;
	// Other charges
	/** Meter charge is a fixed amount per year that is converted into an amount per month */
	protected double meterCharge=0;
	/** CT Compliance Testing Levy is a fixed amount per year that is converted into an amount per month */
	protected double ctComplianceTestingLevy=0;
	/** Other charges is the sum of the meter charge and the CT Compliance testing levy */  
	protected double otherCharge=0;
	// Total charges
	/** Sum of the energy charge, network charge, market charge and other charges. */
	protected double totalChargeExGST=0;
	/** 
	 * GST calculated in line as 10%.  NOTE: Documentation states AEMO Pool charge is gst exempt.  
	 * However AEMO is lumped in with RERT so not possible to separate out for purposes of gst
	 * calculations.  Therefore, GST includes AEMO Pool Charge so will be slightly too high
	 */
	protected double gst=0;
	/** Total payable. */
	protected double totalChargeIncGST=0;
	
	// Aggregate Usages
	/** Peak power used over the month */ 
	protected double peakUsage=0;
	/** Shoulder power used over the month */ 
	protected double shoulderUsage=0;
	/** OffPeak power used over the month */ 
	protected double offpeakUsage=0;
	
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
	public CostPerMonth(String meterName, int month, int year) {
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
	 * Load the loss ratio, Demand Capacity Usage and Demand Critical Peak Usage information from the relevant 
	 * parameters for that particular month
	 * 
	 * @param monthlyParameter The parameters associated with the month under consideration
	 */
	// Add in the monthly meter-specific parameters.
	protected void addMonthlyParameters(MonthlyParameter monthlyParameter) {
		this.spotPriceLossRatio = monthlyParameter.spotPriceLossRatio;
		this.feedInLossRatio = monthlyParameter.feedInLossRatio;
		this.demandCapacityUsage = monthlyParameter.demandCapacityUsage;
		this.demandCriticalPeakUsage = monthlyParameter.demandCriticalPeakUsage;
	}
	
	/**
	 * Inner loop that processes each 30 minute spot price and usage to accumulate overall usage.  Used to add up the 
	 * total usage, aggregate usages and spot price charges.
	 * The PEI calculations can be optimised by removing them from this inner loop.  However, that would make understanding the code more difficult.  
	 * For now, inner loop left as inefficient
	 *  
	 * @param networkParameters Complete Energy charge, Network charge, Spot Energy, Market charge and Other Charge Loss ratios and pricing.
	 * @param recordDateTime Timestamp for this particular reading
	 * @param usage energy used from grid
	 * @param spotPrice Spot price that applies to this particular reading
	 * @param generated energy generated
	 * @param feedInTariff Feed in tarrif that applies to this particular reading
	 */
	protected void addUnitOfCharges(NetworkParameter networkParameters, LocalDateTime recordDateTime, double usage, double spotPrice, double generated, double feedInTariff) {
		double spotPriceIncLoss, feedInTariffIncLoss;
		double nett;
		double gridUsed = 0;
		int usageType;
		
		// Get the month and hour and minute from the usage date stamp.
		int hour = recordDateTime.getHour(); //Already in the range 0 - 23
		int minute = recordDateTime.getMinute(); // In the range 0 - 59
		int dayOfWeek = recordDateTime.getDayOfWeek().getValue()-1; //Minus 1 to turn day into our array index

		// Start processing this 30 minutes' data
		// Calculate net usage for the month
		nett = usage - generated;
		
		//Accumulate monthly usages
		// monthlyUsage += usage;  //No longer used in calculations
		monthlyGenerated += generated;
		//monthlyNett += nett; // No longer used in calculations
		
		if (nett > 0) {
			gridUsed = nett;
			monthlyNettGridUsed += nett;
		}
		else {
			monthlyNettExported += nett;
		}

		spotPriceIncLoss = spotPrice + (spotPrice * this.spotPriceLossRatio); // EQ1
		feedInTariffIncLoss = feedInTariff + (feedInTariff * this.feedInLossRatio); // EQ1

		// TO DO - move this up to the earlier if statement - have to change comments to suit.
		if (nett > 0) {
			poolPassThroughCharge +=  spotPriceIncLoss*nett; // EQ2
		}
		else {
			feedInCharge += feedInTariffIncLoss*nett; //EQ3
		}
			
		// Find out whether the applicable rate is PEAK, SHOULDER or OFFPEAK
		int tariffRateIndex = 2*hour + 2*minute/60;
		usageType = networkParameters.rate[dayOfWeek][tariffRateIndex];
		
		// Accumulate the PEAK, SHOULDER or OFFPEAK as appropriate
		// Look out - more divide by two's here!
		switch (usageType) {
		case NetworkParameter.PEAK:
			peakUsage += gridUsed;  // In preparation for EQ5
			break;
		case NetworkParameter.SHOULDER:
			shoulderUsage += gridUsed; // In preparation for EQ5
			break;
		default:
			offpeakUsage += gridUsed; // In preparation for EQ5
		}
	}
	
	/**
	 * Calculate all charges once usage and price files for that meter and month processed.
	 * 
	 * @param networkParamters Complete Energy charge, Network charge, Spot Energy, Market charge and Other Charge Loss ratios and pricing.
	 * 
	 * @throws Exception Rethrows exceptions from called methods - most likely divide by zero exception when calculating PEIs	 
	 */
	protected void calcuateCharges(NetworkParameter networkParamters) throws Exception {
		
		//Firstly, calculate all the charges.
		double vertRateIncLoss = networkParamters.parameters[month].veetRate + networkParamters.parameters[month].veetRate*networkParamters.parameters[this.month].veetLossRatio;
		double sresRateIncLoss = networkParamters.parameters[month].sresRate + networkParamters.parameters[month].sresRate*networkParamters.parameters[this.month].sresLossRatio;
		double lretRateIncLoss = networkParamters.parameters[month].lretRate + networkParamters.parameters[month].lretRate*networkParamters.parameters[this.month].lretLossRatio;
		double aemoPoolRertRateIncLoss = networkParamters.parameters[month].aemoPoolRertRate + networkParamters.parameters[month].aemoPoolRertRate*networkParamters.parameters[this.month].aemoPoolRertLossRatio;
		double ancilliaryServicesRateIncLoss = networkParamters.parameters[month].ancilliaryServicesRate + networkParamters.parameters[month].ancilliaryServicesRate*networkParamters.parameters[this.month].ancilliaryServicesLossRatio;
		//Energy charge components
		//Pool pass through charge calculated incrementally in addUnitOfCharges()
		
		this.serviceAdminCharge = networkParamters.parameters[month].serviceAdminRate*daysInMonth; // EQ3
		
		//Network charge components.  Peak, Shoulder and Offpeak charges divided by 100 as tariffs are in cents
		this.standingCharge = networkParamters.parameters[month].standingRate/daysInYear * daysInMonth; //EQ4
		this.peakEnergyCharge = this.peakUsage * networkParamters.parameters[month].peakRate/100; //EQ5
		this.shoulderEnergyCharge = this.shoulderUsage * networkParamters.parameters[month].shoulderRate/100; //EQ7
		this.offpeakEnergyCharge = this.offpeakUsage * networkParamters.parameters[month].offpeakRate/100; //EQ6
		this.demandCapacityCharge = this.demandCapacityUsage * networkParamters.parameters[month].demandCapacityRate; //EQ8
		this.demandCriticalPeakCharge = this.demandCriticalPeakUsage * networkParamters.parameters[month].demandCriticalPeakRate; //EQ9
		
		//Market charge components. Divided by 100 as tariffs are in cents/kWh
		this.veetCharge = this.monthlyNettGridUsed * vertRateIncLoss/100; //EQ10
		this.sresCharge = this.monthlyNettGridUsed * sresRateIncLoss/100; //EQ11
		this.lretCharge = this.monthlyNettGridUsed * lretRateIncLoss/100; //EQ12
		this.aemoPoolRertCharge = this.monthlyNettGridUsed * aemoPoolRertRateIncLoss/100; //EQ13
		this.ancilliaryServicesCharge = this.monthlyNettGridUsed * ancilliaryServicesRateIncLoss/100; //EQ14
		
		//Other charge components
		this.meterCharge = (networkParamters.parameters[month].meterRate/this.daysInYear)*this.daysInMonth; //EQ15
		this.ctComplianceTestingLevy = (networkParamters.parameters[month].ctComplianceTestingRate/this.daysInYear)*this.daysInMonth; //EQ16
		
		//Sub-totals
		this.energyCharge = this.poolPassThroughCharge + this.feedInCharge + this.serviceAdminCharge; //EQ17
		this.networkCharge = this.standingCharge + this.peakEnergyCharge + this.shoulderEnergyCharge
				+ this.offpeakEnergyCharge +this.demandCapacityCharge+this.demandCriticalPeakCharge; //EQ18
		this.marketCharge = this.veetCharge + this.sresCharge + this.lretCharge + this.aemoPoolRertCharge 
				+this.ancilliaryServicesCharge; //EQ19
		this.otherCharge = this.meterCharge+this.ctComplianceTestingLevy; //EQ20
		
		//Total charges
		this.totalChargeExGST = this.energyCharge + this.networkCharge + this.marketCharge + this.otherCharge; //EQ21
		
		//GST only applicable for positive total charges.
		if (this.totalChargeExGST > 0 ) {
			this.gst = this.totalChargeExGST*0.1;
		}
		else {
			this.gst = 0;
		}
		
		this.totalChargeIncGST = this.totalChargeExGST + this.gst; //EQ22
	}

}
