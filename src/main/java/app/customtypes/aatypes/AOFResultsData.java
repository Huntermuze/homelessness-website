package app.customtypes.aatypes;

/**
 * A class that represents a bundle of data for the AdvancedOtherFactors page. This is then passed to Thymeleaf, so that
 * it can be interpreted and fill the results table.
 *
 * @author Matthew Yamen
 */
public class AOFResultsData
{
	private final GenericValuePair total;
	private final GenericValuePair average;
	private final GenericValuePair highestValue;
	private final GenericValuePair highestPercent;
	private final GenericValuePair lowestValue;
	private final GenericValuePair lowestPercent;

	public AOFResultsData (GenericValuePair total, GenericValuePair average, GenericValuePair highestValue,
						   GenericValuePair highestPercent, GenericValuePair lowestValue, GenericValuePair lowestPercent)
	{
		this.total = total;
		this.average = average;
		this.highestValue = highestValue;
		this.highestPercent = highestPercent;
		this.lowestValue = lowestValue;
		this.lowestPercent = lowestPercent;
	}

	public GenericValuePair getTotal () { return total; }
	public GenericValuePair getAverage () { return average; }
	public GenericValuePair getHighestValue () { return highestValue; }
	public GenericValuePair getHighestPercent () { return highestPercent; }
	public GenericValuePair getLowestValue () { return lowestValue; }
	public GenericValuePair getLowestPercent () { return lowestPercent; }
}
