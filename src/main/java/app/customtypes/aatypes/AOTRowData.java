package app.customtypes.aatypes;

/**
 * A class that represents a bundle of data for the AdvancedOtherFactors page. This is then passed to Thymeleaf, so that
 * it can be interpreted and fill the data table.
 *
 * @author Matthew Yamen
 */
public class AOTRowData
{
	private final String lgaName; //Immutable, so we don't need to worry about returning a defensive copy in the getter.
	private final short numAffected2016;
	private final short numAffected2018;
	private final short changeNum;
	private final float changePercent;

	public AOTRowData (String lgaName, short numAffected2016, short numAffected2018, short changeNum, float changePercent)
	{
		this.lgaName = lgaName;
		this.numAffected2016 = numAffected2016;
		this.numAffected2018 = numAffected2018;
		this.changeNum = changeNum;
		this.changePercent = changePercent;
	}

	public String getLGAName() { return this.lgaName; }
	public short getNumAffected2016() { return this.numAffected2016; }
	public short getNumAffected2018() { return this.numAffected2018; }
	public short getChangeNum() { return this.changeNum; }
	public float getChangePercent() { return this.changePercent; }
}
