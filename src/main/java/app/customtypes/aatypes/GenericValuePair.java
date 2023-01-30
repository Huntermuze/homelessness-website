package app.customtypes.aatypes;

/**
 * A class that represents a generic grouping of data. This is used within the ResultsTable classes, so as to prevent
 * an excessive amount of fields, and to maintain conciseness within each class. This is because each category in the results table
 * represents an entire row of data, typically comprising three-four components (columns).
 *
 * @author Matthew Yamen
 */
public class GenericValuePair
{
	private final int valueOne;
	private final float valueTwo;
	private int valueThree;
	private String valueFour;

	//A multitude of constructors to be applicable for both pages.
	public GenericValuePair(int valueOne, float valueTwo)
	{
		this.valueOne = valueOne;
		this.valueTwo = valueTwo;
	}

	public GenericValuePair(int valueOne, float valueTwo, String valueFour)
	{
		this(valueOne, valueTwo);
		this.valueFour = valueFour;
	}

	public GenericValuePair(int valueOne, float valueTwo, int valueThree)
	{
		this(valueOne, valueTwo);
		this.valueThree = valueThree;
	}

	public GenericValuePair(int valueOne, float valueTwo, int valueThree, String valueFour)
	{
		this(valueOne, valueTwo, valueFour);
		this.valueThree = valueThree;
	}

	public int getValueOne() { return valueOne; }
	public float getValueTwo() { return valueTwo; }
	public int getValueThree() { return valueThree; }
	public String getValueFour () { return valueFour; }
}
