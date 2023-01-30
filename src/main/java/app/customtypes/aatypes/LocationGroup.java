package app.customtypes.aatypes;

import java.util.ArrayList;

/**
 * A class that represents the location grouping data bundle. Each instance of this class represents a state with
 * a multitude of LGAs within them.
 *
 * @author Matthew Yamen
 */
public class LocationGroup
{
	private final String locationName; //State name.
	private final ArrayList<String> locationsInGroup; //List of LGAs within this state.

	public LocationGroup(String locationName)
	{
		this.locationName = locationName;
		this.locationsInGroup = new ArrayList<>();
	}

	public String getName() { return this.locationName; }
	public ArrayList<String> getLocationsInGroup() { return this.locationsInGroup; }
}
