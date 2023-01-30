package app.customtypes.aatypes;

/**
 * A class that represents a range (min and max). This is to be used mostly in the AdvancedOtherFactors page so as to
 * represent the five options that contain a range each. THis is later interpreted by Thymeleaf and echoed back to the user,
 * whilst also being fed to the database query.
 *
 * @author Matthew Yamen
 */
public class RangePair
{
	//The actual values that they store (to be echoed back to user).
	private int valueOne; //Min
	private int valueTwo; //Max

	public RangePair (int valueOne, int valueTwo)
	{
		this.valueOne = valueOne;
		this.valueTwo = valueTwo;
	}

	public int getValueOne() { return this.valueOne; }
	public int getValueTwo() { return this.valueTwo; }

	public void setValueOne(int valueOne) { this.valueOne = valueOne; }
	public void setValueTwo(int valueTwo) { this.valueTwo = valueTwo; }

	/*(Overridden, otherwise the unique identifier will return a toString() value for each of the objects, regardless of
	//their contents. This is due to the hashcode method being different for *almost* all objects - if they are stored in
	different memory locations. Hence, the hashcode method has been removed from the general purpose (original) definition.*/
	@Override
	public String toString()
	{ return (this.getClass().toString() + "@" + valueOne + valueTwo); }
}
