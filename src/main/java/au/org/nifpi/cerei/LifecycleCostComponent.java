package au.org.nifpi.cerei;

import java.text.DecimalFormat;

/**
 * Individual components of a project for the purposes of conducting a financial analysis of a project.
 * 
 * @author Copyright (c) 2023 University of Technology Sydney and Federation University under MIT License.
 */
public class LifecycleCostComponent {

	/** Name of the project component. */
	protected String name;
	/** Optional cost code for this particular component. */
	protected String costCode = null;
	/** Quantity of this component required for the project. */
	protected String qtyString;
	/** Quantity of this component required for the project. */
	protected double qty;
	/** The capital cost for one of these components. */
	protected double captialCost = 0;
	/** The installation cost for one of these components. */
	protected double installationCost = 0;
	/** The Operational and Maintenance cost for one of these components. */
	protected double fixedOMCost = 0;
	/** The replacement cost of semi-consumable parts for one of these components. */
	protected double replacementCost = 0;
	/** The number of years that semi-consumable parts will last. */
	protected int replacementFrequency = 0;
	/** The years in which replacements of semi-consumable parts are required */
	protected String replacementPayments = null;
	/** The future costs associated with one of these components. */
	protected double futureCost = 0;
	/** The number of years between each expenditure of future costs for one of these components. */
	protected int futureFrequency = 0;
	/** The years in which expenditure of future costs for one of these components are required */
	protected String futurePayments = null;
	
	/** The total capital cost for all of these components (= capital cost * qty). */
	protected double totalCapitalCost;
	/** The total installation cost for all of these components (= installation cost * qty). */
	protected double totalInstallationCost;
	/** The total Operational and Maintenance cost for all of these components (= Operational and Maintenance * qty). */
	protected double totalFixedOMCost;
	/** The total cost for replacements of semi-consumable parts for all of these components (= replacement cost * qty). */
	protected double totalReplacementCost;
	/** The total future expenditures for all of these components (= future cost * qty). */
	protected double totalFutureCost;
	
	/** The discount rate for this particular component - equal to the project-wide discount rate if not explicitly supplied with the component. */
	protected double discountRate = -1;
	/** The inflation rate for this particular component - equal to the project-wide inflation rate if not explicitly supplied with the component. */
	protected double inflationRate = -1;
	/** The "j" factor - calcualted from the discount and inflation rates. */
	protected double j;

	/** Net discount rate - calculated but no longer used or displayed.*/
	protected double annualNdr;
	
	/** The life of this component from the Lifecycle Cost Parameters file. */
	protected String lifetimeString=null;
	/** The life of this component from the Lifecycle Cost Parameters file. */
	protected double lifetime = -1;
	
	/** Net Present Value of component capital costs. */
	protected double npvCapital=0;
	/** Net Present Value of component installation costs. */
	protected double npvInstallation=0;
	/** Net Present Value of component operational and maintenance costs. */
	protected double npvFixedOM = 0;
	/** Net Present Value of component replacement costs. */
	protected double npvReplacement = 0;
	/** Net Present Value of component future costs. */
	protected double npvFuture = 0;
	/** Total Net Present Value of component */  
	protected double totalNPV;
	
	/** ATLCC of component capital costs. */
	protected double atlccCapital=0;
	/** ATLCC of component installation costs. */
	protected double atlccInstallation=0;
	/** ATLCC of component operational and maintenance costs. */
	protected double atlccfixedOMCost=0;
	/** ATLCC of component replacement costs. */
	protected double atlccReplacement=0;
	/** ATLCC of component future costs. */
	protected double atlccFuture=0;
	
	/** Generates a String with two decimal places and commas. Minimum of one whole number and two decimal places. */
	private static final DecimalFormat df2 = new DecimalFormat("#,##0.00");  // Number of decimal places in summary tabs.
	/** Generates a String with two decimal places and commas. Minimum of one whole number and two optional decimal places. */
	private static final DecimalFormat df2optional = new DecimalFormat("#,##0.##");  // Number of decimal places in summary tabs.
	
	/**
	 * Default constructor - does nothing.
	 */
	public LifecycleCostComponent() {
		
	}
	/**
	 * Calculate the ATLCC and other financial measures for this particular component.
	 */
	public void calculateATLCC() {
		// All EQROI references pertain to Equations_ROI - 24.10.22.docx
		
		totalCapitalCost = captialCost * qty; //EQROI-2
		totalInstallationCost = installationCost * qty; //EQROI-2
		totalFixedOMCost = fixedOMCost * qty; //EQROI-2
		totalReplacementCost = replacementCost * qty; //EQROI-2
		totalFutureCost = futureCost * qty; //EQROI-2
		
		j = (discountRate/100 + inflationRate/100 + ((discountRate/100)*(inflationRate/100)))*100;  //EQROI-3
		
		npvCapital = totalCapitalCost * -1; //EQR01-4
		npvInstallation = totalInstallationCost * -1; //EQR01-4 
		
		annualNdr = ( ((1+(discountRate/100)) / (1+(inflationRate/100))) -1) * 100; // EQROI-4
		
		if (totalFixedOMCost > 0) {
			for (int n=1; n <= lifetime; n++) { //EQROI-5
				npvFixedOM += totalFixedOMCost * (-1) * (Math.pow(1+(inflationRate/100), n)/Math.pow(1 + j/100, n));
			}
		}
		
		if (totalReplacementCost > 0 ) {
			// Replacement starts at year "replacement frequency" and occurs every "replacement frequency" thereafter until we get to lifetime.  There is no replacement in the final year 
			// EQRO1-6
			for (int n=replacementFrequency; n < lifetime; n += replacementFrequency) {
				npvReplacement += totalReplacementCost * (-1) * (Math.pow(1.0+(inflationRate/100), n)/Math.pow(1+(j/100), n));
				if (replacementPayments == null) {
					replacementPayments = Integer.toString(n);
				}
				else {
					replacementPayments += "; "+Integer.toString(n);
				}
			}
		}
		
		if (totalFutureCost > 0 ) {
			// Future cost starts at year "future cost frequency" and occurs every "future cost frequency" thereafter until we get to lifetime.  There is no future cost in the final year 
			// EQRO1-6
			for (int n=futureFrequency; n < lifetime; n += futureFrequency) {
				npvFuture += totalFutureCost * (-1) * (Math.pow(1.0+(inflationRate/100), n)/Math.pow(1+(j/100), n));
				if (futurePayments == null) {
					futurePayments = Integer.toString(n);
				}
				else {
					futurePayments += "; "+Integer.toString(n);
				}
			}
		}		
		
		totalNPV = npvCapital + npvInstallation + npvFixedOM + npvReplacement + npvFuture; //EQROI-7
		
		atlccCapital = npvCapital *   //EQROI-8.  
				((j/100)*Math.pow(1+(j/100),lifetime))/(Math.pow(1+(j/100),lifetime)-1);
		
		atlccInstallation = npvInstallation * //EQROI-8 
				((j/100)*Math.pow(1+(j/100),lifetime))/(Math.pow(1+(j/100),lifetime)-1);

		atlccfixedOMCost = npvFixedOM * //EQROI-9
				((j/100)*Math.pow(1+(j/100),lifetime))/(Math.pow(1+(j/100),lifetime)-1);
		
		atlccReplacement = npvReplacement * //EQROI-10
				((j/100)*Math.pow(1+(j/100),lifetime))/(Math.pow(1+(j/100),lifetime)-1);
				
		atlccFuture = npvFuture * //EQROI-10
				((j/100)*Math.pow(1+(j/100),lifetime))/(Math.pow(1+(j/100),lifetime)-1);
	}


	/**
	 * Calculate the number of rows required for this component in the Lifecycle Cost Analysis Details file. 
	 * @return The number of rows required for this component in the Lifecycle Cost Analysis Details file.
	 * 
	 * @deprecated since version 1.0.1.0
	 */
	public int getNumOutputRows() {
		int numRows = 1; //There is always the name of the component
		if (captialCost != 0) {
			numRows++;
		}
		if (installationCost != 0) {
			numRows++;
		}
		if (fixedOMCost != 0) {
			numRows++;
		}

		if (replacementCost != 0) {
			numRows++;
		}

		if (futureCost != 0) {
			numRows++;
		}

		return numRows;
	}


	/**
	 * Add the financial analysis of this component into a 2-dimensional array of Strings.
	 * 
	 * @param economicData The component to add to a string array
	 * @param currentRow The row of the output array to start adding this information to. 
	 * 
	 * @return Updated 2-dimensional array of Strings.
	 * 
	 * @deprecated since version 1.0.1.0
	 */
	public int loadDataIntoArray(String[][] economicData, int currentRow) {
		economicData[currentRow][0] = costCode; 
		economicData[currentRow][1] = name;
		currentRow++;
		int costSubCode=1;
				
		if(captialCost != 0) {
			economicData[currentRow][0] = costCode + "." + String.valueOf(costSubCode); 
			economicData[currentRow][1] = "Captial Cost";
			economicData[currentRow][2] = df2.format(captialCost);
			economicData[currentRow][3] = "AUD/Unit";
			economicData[currentRow][4] = qtyString;
			economicData[currentRow][5] = "1";
			economicData[currentRow][6] = df2.format(totalCapitalCost);
			economicData[currentRow][7] = df2.format(discountRate);
			economicData[currentRow][8] = df2.format(inflationRate);
			economicData[currentRow][9] = df2.format(annualNdr);
			economicData[currentRow][10] = lifetimeString;
			economicData[currentRow][11] = df2.format(totalCapitalCost*-1);
			economicData[currentRow][12] = df2.format(atlccCapital);
			currentRow++;
			costSubCode++;
		}
		if(installationCost != 0) {
			economicData[currentRow][0] = costCode + "." + String.valueOf(costSubCode); 
			economicData[currentRow][1] = "Installation Cost";
			economicData[currentRow][2] = df2.format(installationCost);
			economicData[currentRow][3] = "AUD/Unit";
			economicData[currentRow][4] = qtyString;
			economicData[currentRow][5] = "1";
			economicData[currentRow][6] = df2.format(totalInstallationCost);
			economicData[currentRow][7] = df2.format(discountRate);
			economicData[currentRow][8] = df2.format(inflationRate);
			economicData[currentRow][9] = df2.format(annualNdr);
			economicData[currentRow][10] = lifetimeString;
			economicData[currentRow][11] = df2.format(totalInstallationCost*-1);
			economicData[currentRow][12] = df2.format(atlccInstallation);
			currentRow++;
			costSubCode++;
		}
		if(fixedOMCost != 0) {
			economicData[currentRow][0] = costCode + "." + String.valueOf(costSubCode); 
			economicData[currentRow][1] = "Fixed O&M Costs";
			economicData[currentRow][2] = df2.format(fixedOMCost);
			economicData[currentRow][3] = "AUD/Unit";
			economicData[currentRow][4] = qtyString;
			economicData[currentRow][5] = "1 ... " +lifetimeString;
			economicData[currentRow][6] = df2.format(totalFixedOMCost);
			economicData[currentRow][7] = df2.format(discountRate);
			economicData[currentRow][8] = df2.format(inflationRate);
			economicData[currentRow][9] = df2.format(annualNdr);
			economicData[currentRow][10] = df2optional.format(lifetime-1);
			economicData[currentRow][11] = df2.format(npvFixedOM);
			economicData[currentRow][12] = df2.format(atlccfixedOMCost);
			currentRow++;
			costSubCode++;
		}
		return currentRow;
	}

}
