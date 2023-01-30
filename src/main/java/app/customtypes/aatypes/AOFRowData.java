package app.customtypes.aatypes;

/**
 * A class that represents a bundle of data for the AdvancedOtherFactors page. This is then passed to Thymeleaf, so that
 * it can be interpreted and fill the data table.
 *
 * @author Matthew Yamen
 */
public class AOFRowData
{
	private final String lgaName; //Immutable, so we don't need to worry about returning a defensive copy in the getter.
	private final int lgaPopulation;
	private final short numAffected;
	private final short comparisonValue;
	private final float percentOfPop;

	public AOFRowData(String lgaName, int lgaPopulation, short numAffected, short comparisonValue, float percentOfPop)
	{ this.lgaName = lgaName; this.numAffected = numAffected; this.lgaPopulation = lgaPopulation;
		this.comparisonValue = comparisonValue; this.percentOfPop = percentOfPop; }

	public String getLgaName() { return this.lgaName; }
	public int getLgaPopulation() { return this.lgaPopulation; }
	public short getNumAffected() { return this.numAffected; }
	public short getComparisonValue() { return this.comparisonValue; }
	public float getPercentOfPop() { return this.percentOfPop; }
}
