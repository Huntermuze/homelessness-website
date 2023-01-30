package app.builders;

import app.customtypes.aatypes.LocationGroup;
import app.utils.StaticUtils;

import java.sql.*;
import java.util.ArrayList;

/**An abstract class for managing the JDBC Connection to a SQLLite Database. This represents the base class for
 * the other builder classes. It is abstract because there should not be any instances of this class.
 *
 * @author Matthew Yamen
 */
public abstract class JDBCBuilder
{
	private static final String DATABASE = "jdbc:sqlite:database/homelessnessAus.db";
	private static Connection connection;

	private static ArrayList<String> locationsArray; //List of locations for the user to select from.
	private static ArrayList<LocationGroup> locationGroupArray; //List of locations within groupings for user to select from.

	protected JDBCBuilder()
	{
		/*Since subclasses call their super's constructor, this will run three times, since there are three subclasses.
		This conditional statement will prevent three connections from being established and over-computing the lists.*/
		if (connection == null)
		{
			try { connection = DriverManager.getConnection(DATABASE); }
			catch (SQLException throwable) { throwable.printStackTrace(); }
			locationsArray = createLocationsList();
			locationGroupArray = createLocationGroupsList();
		}
	}

	//----UTILITY METHODS----
	//PUBLIC
	/**Close the SQLite database connection stream.*/
	public void closeConnection()
	{
		try { if (connection != null) { connection.close(); } }
		catch (SQLException e) { e.printStackTrace(); }
	}

	//PRIVATE
	//Creates the list of locations to be displayed to the user.
	private ArrayList<String> createLocationsList()
	{
		ArrayList<String> locationList = StaticUtils.addStatesToLocList(new ArrayList<>());
		ResultSet results = generateResults("SELECT name FROM lgas;");
		try
		{
			while (results.next())
			{
				locationList.add(results.getString("name"));
			}
		}
		catch (SQLException e) { e.printStackTrace(); }
		locationList.add("NSW"); locationList.add("VIC"); locationList.add("QLD"); locationList.add("SA");
		locationList.add("WA"); locationList.add("TAS"); locationList.add("NT"); locationList.add("ACT");
		return locationList;
	}

	//Creates the list of locations, with grouping by state, to be displayed to the user.
	private ArrayList<LocationGroup> createLocationGroupsList()
	{
		ArrayList<LocationGroup> locationGroups = new ArrayList<>();
		for (int i = 1; i < 10; ++i)
		{
			locationGroups.add(new LocationGroup(StaticUtils.getStateFromInt(i)));
			ArrayList<String> tempGroupList = locationGroups.get(i - 1).getLocationsInGroup();
			try
			{
				ResultSet results = generateResults("SELECT name FROM lgas WHERE lgacode LIKE '" + i + "%';");
				while (results.next()) tempGroupList.add(results.getString("name"));
			}
			catch (SQLException e) { e.printStackTrace(); }
		}

		return locationGroups;
	}

	//Creates the ResultSet for a particular query.
	protected ResultSet generateResults(String query)
	{
		ResultSet results = null;
		try { results = connection.prepareStatement(query).executeQuery(); }
		catch (SQLException throwable) { throwable.printStackTrace(); }

		return results;
	}

	//Iterates a given option and further builds the query.
	protected boolean iterateOptionsList(ArrayList<String> optionsList, StringBuilder builder)
	{
		boolean statesExist = false;

		builder.append(" (");
		//All other options will be longer than length 2 except for the integers delineating a state.
		for(int i = 0; i < optionsList.size(); ++i)
		{
			if (optionsList.get(i).length() > 2)
				if (i == optionsList.size()- 1) builder.append("'").append(optionsList.get(i)).append("'");
				else builder.append("'").append(optionsList.get(i)).append("', ");
			else statesExist = true;
		}
		builder.append(") ");

		return statesExist;
	}

	//Iterates through the list of locations, grabs the state LGA code and appends in to the StringBuilder.
	protected void iterateLocationsForState(ArrayList<String> locations, StringBuilder builder)
	{
		for (String option : locations)
			if (option.length() <= 2) builder.append("OR lgas.lgacode LIKE '").append(option).append("%' ");
	}

	//----DATA FETCHING METHODS----
	/**Get the list of locations to display to the user as options.
	 * @return An ArrayList of location (States and LGAs) strings.*/
	public ArrayList<String> getLocationsList() { return this.locationsArray; }

	/**Get the list of locations (grouped by state) to display to the user as options.
	 * @return An ArrayList of location (States and LGAs) strings.*/
	public ArrayList<LocationGroup> getLocationGroupArray() { return this.locationGroupArray; }

}

