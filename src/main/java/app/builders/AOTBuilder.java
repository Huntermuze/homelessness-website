package app.builders;

import app.customtypes.aatypes.AOTRowData;
import app.customtypes.aatypes.GenericValuePair;
import app.customtypes.aatypes.AOTResultsData;
import app.customtypes.customcollections.LRUCache;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;

import java.lang.ref.SoftReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * An extension of the JDBCBuilder class that is responsible for providing the components to build the Advanced Over Time
 * Analysis page. This is also a singleton.
 *
 * @author Matthew Yamen
 */
public final class AOTBuilder extends JDBCBuilder
{
	private static AOTBuilder singletonInstance;

	private SoftReference<LRUCache<String, RowDataList<AOTRowData>>> softCachedList =
			new SoftReference<>(new LRUCache<>(35)); //Soft Reference used to mitigate memory leaks.

	private AOTBuilder() {}

	//----UTILITY METHODS----
	//PUBLIC
	/**Get the single instance of the AOTBuilder class. Disallow concurrent or parallel access.
	 * @return An AOTBuilder singleton instance.
	 */
	public static synchronized AOTBuilder getAOTBuilder()
	{
		return (AOTBuilder.singletonInstance == null) ?
				AOTBuilder.singletonInstance = new AOTBuilder() : AOTBuilder.singletonInstance;
	}

	//PRIVATE
	//Builds the a dynamic database query to get the data based on the user's input.
	private String dataTableQueryBuilder(ArrayList<String> locations, ArrayList<String> popChange, ArrayList<String> gender,
										 ArrayList<String> ageGroup, String sortBy, String sortType, String dataType, int popMin, int popMax)
	{
		String popChangeDBForm = StaticUtils.popChangeToDBForm(popChange.get(0));
		StringBuilder queryBuilder = (new StringBuilder())
				.append("SELECT lgas.name, data2016.value AS value2016, data2018.value AS value2018, (data2018.value - data2016.value) AS change, ")
				.append("(CAST(Data2018.Value - Data2016.Value AS REAL) / data2016.value) AS Ratio FROM lgas JOIN (SELECT lgacode, ")
				.append("SUM(amount) AS value FROM ").append(dataType).append(" WHERE year = 2016 AND agerange IN");

		iterateOptionsList(ageGroup, queryBuilder);
		queryBuilder.append("AND gender IN"); iterateOptionsList(gender, queryBuilder);

		queryBuilder.append("GROUP BY lgacode) data2016 ON lgas.lgacode = data2016.lgacode JOIN (SELECT lgacode, ")
				.append("SUM(amount) AS value FROM ").append(dataType).append(" WHERE year = 2018 AND agerange IN");

		iterateOptionsList(ageGroup, queryBuilder);
		queryBuilder.append("AND gender IN"); iterateOptionsList(gender, queryBuilder);

		queryBuilder.append("GROUP BY lgacode) data2018 ON lgas.lgacode = data2018.lgacode JOIN (SELECT lgacode, ")
				.append("population FROM populationdata WHERE year = 2016) population2016 ON lgas.lgacode = ")
				.append("population2016.lgacode JOIN (SELECT lgacode, population FROM populationdata WHERE year = 2018) ")
				.append("population2018 ON lgas.lgacode = population2018.lgacode WHERE lgas.name IN ");

		boolean statesExist = iterateOptionsList(locations, queryBuilder);
		if (statesExist) iterateLocationsForState(locations, queryBuilder);

		queryBuilder.append("AND population2018.population >= ").append(popMin).append(" AND population2018.population <= ").append(popMax);
		if (popChange.size() != 2) queryBuilder.append(" AND change ").append(popChangeDBForm).append(" 0");

		return queryBuilder.append(" ORDER BY ").append(sortBy).append(" ").append(sortType).append(";").toString();
	}

	//----DATA FETCHING METHODS----
	/**
	 * Method for obtaining a list of rows by various filters and options.
	 * @param locations the list of locations selected.
	 * @param popChange the population change selected (increase or decrease).
	 * @param gender the list of gender(s) that have been selected.
	 * @param ageGroup the list of age group(s) that have been selected.
	 * @param sortBy the filter by which to sort the data table upon.
	 * @param sortType the order of the data (descending or ascending).
	 * @param dataType the type of data requested (either At Risk or Homelessness).
	 * @param popMin the minimum population filter selected.
	 * @param popMax the maximum population filter selected.
	 * @return a list of rows to populate the data table with.
	 */
	public RowDataList<AOTRowData> getListOfRows(ArrayList<String> locations, ArrayList<String> popChange, ArrayList<String> gender,
												 ArrayList<String> ageGroup, String sortBy, String sortType, String dataType, int popMin, int popMax)
	{
		/* Unique identifier for this specific query.
		 * NOTE: String interning ensures two logically equivalent queries options will produce the same pointer to
		 * the existing string within the String Pool.*/
		String identifier = (locations.toString() + gender.toString() + ageGroup.toString() + popChange.toString() +
							popMin + popMax + sortBy + sortType + dataType);

		//If the soft reference has been cleaned, we must resurrect it and then generate a rows list (prevent NPE).
		if (softCachedList.get() == null) this.softCachedList = StaticUtils.resurrectSoftRefObject(softCachedList, 35);
		//Generate a new row list if the specific query has not already been searched before.
		RowDataList<AOTRowData> rowsList = Objects.requireNonNull(softCachedList.get()).generateNewList(softCachedList.get().get(identifier));

		if (rowsList.size() == 0) //If its empty, that means the query did not previously exist.
		{
			ResultSet results = generateResults(dataTableQueryBuilder(locations, popChange, gender, ageGroup, sortBy,
					sortType, dataType, popMin, popMax));
			try
			{
				while (results.next())
				{
					rowsList.add(new AOTRowData(results.getString("name"),
							results.getShort("value2016"), results.getShort("value2018"),
							results.getShort("change"), results.getFloat("ratio")));
				}
				results.getStatement().close(); //Close the statement.
			}
			catch (SQLException e) { e.printStackTrace(); }

		}
		Objects.requireNonNull(softCachedList.get()).put(identifier, rowsList); //Once no exceptions and successful filling of array, add to cache list.

		return rowsList;
	}

	/**
	 * Method for obtaining the result table's statistics to be displayed to the user.
	 * @param list the list of row data returned via the getListOfRows() method.
	 * @see app.builders.AOTBuilder#getListOfRows(ArrayList, ArrayList, ArrayList, ArrayList, String, String, String, int, int)
	 * @return the statistics in a bundled datatype to be interpreted by Thymeleaf.
	 */
	public AOTResultsData getResultsStatistics(RowDataList<AOTRowData> list)
	{
		AOTResultsData results = list.getAotResultsData();

		if (results == null && list.size() != 0)
		{
			final int FIRST_VALUE_NUM = list.get(0).getChangeNum();
			final float FIRST_VALUE_PERCENT = list.get(0).getChangePercent();

			int total2018 = 0;
			int total2016 = 0;
			int totalChange = 0;
			int bestValue = FIRST_VALUE_NUM; short bVIndex = 0;
			int smallestValue = FIRST_VALUE_NUM; short sVIndex = 0;
			int worstValue = FIRST_VALUE_NUM; short wVIndex = 0;

			float sumOfPercentChanges = 0.0f;
			float bestPercent = FIRST_VALUE_PERCENT; short bPIndex = 0;
			float smallestPercent = FIRST_VALUE_PERCENT; short sPIndex = 0;
			float worstPercent = FIRST_VALUE_PERCENT; short wPIndex = 0;

			for (short i = 0; i < list.size(); ++i)
			{
				//Get the total values.
				totalChange += list.get(i).getChangeNum();
				total2018 += list.get(i).getNumAffected2018();
				total2016 += list.get(i).getNumAffected2016();
				sumOfPercentChanges += list.get(i).getChangePercent();

				int currNumValue = list.get(i).getChangeNum();
				float currPercentValue = list.get(i).getChangePercent();

				//Get the minimum, maximum and smallest values for each category.
				if (currNumValue < bestValue)
				{ bestValue = currNumValue; bVIndex = i; }
				if (Math.abs(currNumValue) < Math.abs(smallestValue))
				{ smallestValue = currNumValue; sVIndex = i; }
				if (currNumValue > worstValue)
				{ worstValue = currNumValue; wVIndex = i; }

				if (currPercentValue < bestPercent)
				{ bestPercent = currPercentValue; bPIndex = i; }
				if (Math.abs(currPercentValue) < Math.abs(smallestPercent))
				{ smallestPercent = currPercentValue; sPIndex = i; }
				if (currPercentValue > worstPercent)
				{ worstPercent = currPercentValue; wPIndex = i; }
			}


			//Calculate the averages and percentages.
			int average = totalChange / list.size();
			float avgPercentChange = sumOfPercentChanges / list.size();
			float totalPercentChange = (float) (total2018 - total2016) / total2016;


			//Createa a new AOTResultsData containing all of the statistics acquired.
			results = new AOTResultsData(new GenericValuePair(totalChange, totalPercentChange), //Total
					new GenericValuePair(average, avgPercentChange), //Average
					new GenericValuePair(bestValue, list.get(bVIndex).getChangePercent(), list.get(bVIndex).getLGAName()), //BestValue
					new GenericValuePair(list.get(bPIndex).getChangeNum(), bestPercent, list.get(bPIndex).getLGAName()), //BestPercent
					new GenericValuePair(smallestValue, list.get(sVIndex).getChangePercent(), list.get(sVIndex).getLGAName()), //SmallestValue
					new GenericValuePair(list.get(sPIndex).getChangeNum(), smallestPercent, list.get(sPIndex).getLGAName()), //SmallestPercent
					new GenericValuePair(worstValue, list.get(wVIndex).getChangePercent(), list.get(wVIndex).getLGAName()), //WorstValue
					new GenericValuePair(list.get(wPIndex).getChangeNum(), worstPercent, list.get(wPIndex).getLGAName())); //WorstPercent

			list.setAotResultsData(results); //Set this list's AOTResultsData field so that it can be cached alongside the list.
		}
		return results;
	}

	/**
	 * A method that will generate a csv file for the Advanced Over Time page's current data.
	 * @param list the list of row data returned via the getListOfRows() method.
	 * @see app.builders.AOTBuilder#getListOfRows(ArrayList, ArrayList, ArrayList, ArrayList, String, String, String, int, int)
	 * @return a string that will contain all of the csv's contents, which will be interpreted by Javalin.
	 */
	public String generateCSVFile(RowDataList<AOTRowData> list)
	{
		if (list.getAotCSVContents() == null)
		{
			StringBuilder csvBuilder = new StringBuilder();
			csvBuilder.append("LGA NAME, NUMBER OF HOMELESS PEOPLE IN 2016, NUMBER OF HOMELESS PEOPLE IN 2018, CHANGE FROM 2016-2018,")
					.append("PERCENTAGE CHANGE FROM 2016-2018\n");

			for (AOTRowData aotRowData : list)
			{
				csvBuilder.append(aotRowData.getLGAName()).append(",").append(aotRowData.getNumAffected2016()).append(",")
						.append(aotRowData.getNumAffected2018()).append(",").append(aotRowData.getChangeNum()).append(",")
						.append(aotRowData.getChangePercent()).append("\n");
			}
			return csvBuilder.toString();
		}

		return list.getAotCSVContents();
	}
}
