package app.customtypes.aatypes;

/**
 * A class that represents a bundle of data for the AdvancedOverTime page. This is then passed to Thymeleaf, so that
 * it can be interpreted and fill the results table.
 *
 * @author Matthew Yamen
 */
public class AOTResultsData
{
	private final GenericValuePair total;
	private final GenericValuePair average;
	private final GenericValuePair bestValue;
	private final GenericValuePair bestPercent;
	private final GenericValuePair smallestValue;
	private final GenericValuePair smallestPercent;
	private final GenericValuePair worstValue;
	private final GenericValuePair worstPercent;

	public AOTResultsData (GenericValuePair total, GenericValuePair average, GenericValuePair bestValue, GenericValuePair bestPercent,
						   GenericValuePair smallestValue, GenericValuePair smallestPercent, GenericValuePair worstValue, GenericValuePair worstPercent)
	{
		this.total = total;
		this.average = average;
		this.bestValue = bestValue;
		this.bestPercent = bestPercent;
		this.smallestValue = smallestValue;
		this.smallestPercent = smallestPercent;
		this.worstValue = worstValue;
		this.worstPercent = worstPercent;
	}

	public GenericValuePair getTotal () { return total; }
	public GenericValuePair getAverage () { return average; }
	public GenericValuePair getBestValue () { return bestValue; }
	public GenericValuePair getBestPercent () { return bestPercent; }
	public GenericValuePair getSmallestValue () { return smallestValue; }
	public GenericValuePair getSmallestPercent () { return smallestPercent; }
	public GenericValuePair getWorstValue () { return worstValue; }
	public GenericValuePair getWorstPercent () { return worstPercent; }
}
