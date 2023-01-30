package app.customtypes.lmtypes;

/**The custom unit of data that will represent a unique row within the results collection for the learn more page.//Since subclasses call their super's constructor, this will run three times, since there are three subclasses.
 *
 * @author Matthew Yamen
 */

/* NOTE: This class should be an immutable type to preserve the validity of the caching system, since if a RowData
 * object's state were to be modified, then the cache would not longer preserve its true, initial state. However, if this
 * class were to be immutable, then new Strings for age and gender would need to be returned, and a defensive copy of
 * any objects passed through the constructor (copy constructor) would need to be created. This would result in an
 * excessive amount of garbage for the Garbage Collector, and thus promoting more minor gcs (since there are over 5k+
 * unique RowData objects that could be created, where each contains two mutable fields; age, gender). Thus, the
 * potential inefficiency outweigh the con of lesser performance, and since no other thread will be mutating these
 * fields within a particular object, then thread-safety is not a plausible concern in this context.*/
public class RowData
{
	private final String age;
	private final String gender;
	private final int count;

	public RowData(String age, String gender, int count)
	{
		this.age = age;
		this.gender = gender;
		this.count = count;
	}

	public String getAge() { return age; }
	public String getGender() { return gender; }
	public int getCount() { return count; }

}
