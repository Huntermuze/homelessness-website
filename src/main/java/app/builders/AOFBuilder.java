package app.builders;

import app.customtypes.aatypes.*;
import app.customtypes.customcollections.LRUCache;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;

import java.lang.ref.SoftReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * An extension of the JDBCBuilder class that is responsible for providing the components to build the Advanced Other Factors
 * Analysis page. This is also a singleton.
 *
 * @author Matthew Yamen
 */
public final class AOFBuilder extends JDBCBuilder
{
	private static AOFBuilder singletonInstance;

	private SoftReference<LRUCache<String, RowDataList<AOFRowData>>> softCachedList =
			new SoftReference<>(new LRUCache<>(35)); //Soft Reference used to mitigate memory leaks.

	private AOFBuilder() {}

	//----UTILITY METHODS----
	//PUBLIC
	/**Get the single instance of the AOFBuilder class. Disallow concurrent or parallel access.
	 * @return An AOFBuilder singleton instance.
	 */
	public static synchronized AOFBuilder getAOFBuilder()
	{
		return (AOFBuilder.singletonInstance == null) ?
				AOFBuilder.singletonInstance = new AOFBuilder() : AOFBuilder.singletonInstance;
	}

	//PRIVATE
	//Builds the a dynamic database query to get the data based on the user's input.
	private String dataTableQueryBuilder(ArrayList<String> locations, ArrayList<String> gender, ArrayList<String> ageGroup,
										 String sortBy, String sortType, String dataType, String comparison,
										 RangePair pop, RangePair medianAge, RangePair medianHouseholdIncome,
										 RangePair medianMortgageRepay, RangePair medianWeeklyRent)
	{
		StringBuilder queryBuilder = (new StringBuilder())
				.append("SELECT lgas.name AS name, ").append(dataType).append(".value AS value, (CAST(").append(dataType)
				.append(".value AS REAL) / populationdata.population) AS ratio, ").append(comparison).append(" AS comparison,")
				.append(" populationdata.population AS lgapopulation").append(" FROM lgas JOIN (SELECT lgacode, SUM(amount) AS value FROM ")
				.append(dataType).append(" WHERE year = 2018").append(" AND agerange IN");

		iterateOptionsList(ageGroup, queryBuilder);
		queryBuilder.append("AND gender IN"); iterateOptionsList(gender, queryBuilder);

		queryBuilder.append("GROUP BY lgacode) ").append(dataType).append(" ON lgas.lgacode = ").append(dataType).append(".lgacode")
				.append(" JOIN (SELECT lgacode, population, medianage FROM populationdata WHERE year = 2018 AND population NOT NULL")
				.append(" AND medianage NOT NULL) populationdata ON lgas.lgacode = populationdata.lgacode JOIN (SELECT lgacode, weeklyhouseholdincome,")
				.append(" monthlymortgagerepayment, weeklyrent FROM incomedata WHERE year = 2018) incomedata ON lgas.lgacode = incomedata")
				.append(".lgacode WHERE lgas.name IN");

		boolean statesExist = iterateOptionsList(locations, queryBuilder);
		if (statesExist) iterateLocationsForState(locations, queryBuilder);

		queryBuilder.append(" AND populationdata.population >= ").append(pop.getValueOne()).append(" AND populationdata.population <= ").append(pop.getValueTwo())
				.append(" AND populationdata.medianage >= ").append(medianAge.getValueOne()).append(" AND populationdata.medianage <= ").append(medianAge.getValueTwo())
				.append(" AND incomedata.weeklyhouseholdincome >= ").append(medianHouseholdIncome.getValueOne())
				.append(" AND incomedata.weeklyhouseholdincome <= ").append(medianHouseholdIncome.getValueTwo())
				.append(" AND incomedata.monthlymortgagerepayment >= ").append(medianMortgageRepay.getValueOne())
				.append(" AND incomedata.monthlymortgagerepayment <= ").append(medianMortgageRepay.getValueTwo())
				.append(" AND incomedata.weeklyrent >= ").append(medianWeeklyRent.getValueOne())
				.append(" AND incomedata.weeklyrent <= ").append(medianWeeklyRent.getValueTwo());

		return queryBuilder.append(" ORDER BY ").append(sortBy).append(" ").append(sortType).toString();
	}

	//----DATA FETCHING METHODS----

	/**
	 * Method for obtaining a list of rows by various filters and options.
	 * @param locations the list of locations selected.
	 * @param gender the list of gender(s) that have been selected.
	 * @param ageGroup the list of age group(s) that have been selected.
	 * @param sortBy the filter by which to sort the data table upon.
	 * @param sortType the order of the data (descending or ascending).
	 * @param dataType the type of data requested (either At Risk or Homelessness).
	 * @param comparison the benchmark to compare either homelessness or at risk of homelessness upon.
	 * @param pop the minimum and maximum population selected.
	 * @param medianAge the minimum and maximum median age values selected.
	 * @param medianHouseholdIncome the minimum and maximum median household income values selected.
	 * @param medianMortgageRepay the minimum and maximum median mortgage repayment values selected.
	 * @param medianWeeklyRent the minimum and maximum median weekly rent repayment values selected.
	 * @return a list of rows to populate the data table with.
	 */
	public RowDataList<AOFRowData> getListOfRows(ArrayList<String> locations, ArrayList<String> gender, ArrayList<String> ageGroup,
												 String sortBy, String sortType, String dataType, String comparison,
												 RangePair pop, RangePair medianAge, RangePair medianHouseholdIncome,
												 RangePair medianMortgageRepay, RangePair medianWeeklyRent)
	{
		/* Unique identifier for this specific query.
		 * NOTE: String interning ensures two logically equivalent queries options will produce the same pointer to
		 * the existing string within the String Pool.*/
		String identifier = (locations.toString() + gender.toString() + ageGroup.toString() + sortBy + sortType + dataType
				+ comparison + pop + medianAge + medianHouseholdIncome + medianMortgageRepay + medianWeeklyRent);

		//If the soft reference has been cleaned, we must resurrect it and then generate a rows list (prevent NPE).
		if (softCachedList.get() == null) this.softCachedList = StaticUtils.resurrectSoftRefObject(softCachedList, 35);
		//Generate a new row list if the specific query has not already been searched before.
		RowDataList<AOFRowData> rowsList = Objects.requireNonNull(softCachedList.get()).generateNewList(softCachedList.get().get(identifier));

		if (rowsList.size() == 0) //If its empty, that means the query did not previously exist.
		{
			ResultSet results = generateResults(dataTableQueryBuilder(locations, gender, ageGroup, sortBy, sortType, dataType,
					comparison, pop, medianAge, medianHouseholdIncome, medianMortgageRepay, medianWeeklyRent));
			try
			{
				while (results.next())
				{
					rowsList.add(new AOFRowData(results.getString("name"), results.getInt("lgapopulation"),
							results.getShort("value"), results.getShort("comparison"), results.getFloat("ratio")));
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
	 * @see app.builders.AOFBuilder#getListOfRows(ArrayList, ArrayList, ArrayList, String, String, String, String, RangePair, RangePair, RangePair, RangePair, RangePair)
	 * @return the statistics in a bundled datatype to be interpreted by Thymeleaf.
	 */
	public AOFResultsData getResultsStatistics(RowDataList<AOFRowData> list)
	{
		AOFResultsData results = list.getAofResultsData();

		if (results == null && list.size() != 0)
		{
			final int FIRST_VALUE_NUM = list.get(0).getNumAffected();
			final float FIRST_VALUE_PERCENT = list.get(0).getPercentOfPop();
			int totalHomeless = 0;
			int totalPopulation = 0;
			int totalComparisonFactor = 0;
			float totalPercentHomeless = 0.0f;

			int highestValue = FIRST_VALUE_NUM; short hVIndex = 0;
			float highestPercent = FIRST_VALUE_PERCENT; short hPIndex = 0;

			int lowestValue = FIRST_VALUE_NUM; short lVIndex = 0;
			float lowestPercent = FIRST_VALUE_PERCENT; short lPIndex = 0;

			for (short i = 0; i < list.size(); ++i)
			{
				//Get the total values.
				totalHomeless += list.get(i).getNumAffected();
				totalPopulation += list.get(i).getLgaPopulation();
				totalComparisonFactor += list.get(i).getComparisonValue();
				totalPercentHomeless += list.get(i).getPercentOfPop();

				int currNumValue = list.get(i).getNumAffected();
				float currPercentValue = list.get(i).getPercentOfPop();

				//Get the minimum, maximum values for each category.
				if (currNumValue > highestValue)
				{ highestValue = currNumValue; hVIndex = i; }
				if (currNumValue < lowestValue)
				{ lowestValue = currNumValue; lVIndex = i; }

				if (currPercentValue > highestPercent)
				{ highestPercent = currPercentValue; hPIndex = i; }
				if (currPercentValue < lowestPercent)
				{ lowestPercent = currPercentValue; lPIndex = i; }
			}

			//Calculate the percentages.
			float percentOfPopulation = (float) totalHomeless / totalPopulation;
			float avgPercentOfPop = totalPercentHomeless / list.size();

			//Calculate the averages.
			int avgHomelessInLgas = totalHomeless / list.size();
			int avgComparisonFactor = totalComparisonFactor / list.size();

			//Createa a new AOFResultsData containing all of the statistics acquired.
			results = new AOFResultsData(new GenericValuePair(totalHomeless, percentOfPopulation), //total
					new GenericValuePair(avgHomelessInLgas, avgPercentOfPop, avgComparisonFactor), //average
					new GenericValuePair(highestValue, list.get(hVIndex).getPercentOfPop(), list.get(hVIndex).getComparisonValue(), list.get(hVIndex).getLgaName()), //highestVal
					new GenericValuePair(list.get(hPIndex).getNumAffected(), highestPercent, list.get(hPIndex).getComparisonValue(), list.get(hPIndex).getLgaName()), //highestPerc
					new GenericValuePair(lowestValue, list.get(lVIndex).getPercentOfPop(), list.get(lVIndex).getComparisonValue(), list.get(lVIndex).getLgaName()), //lowestVal
					new GenericValuePair(list.get(lPIndex).getNumAffected(), lowestPercent, list.get(lPIndex).getComparisonValue(), list.get(lPIndex).getLgaName())); //lowestPerc

			list.setAofResultsData(results); //Set this list's AOFResultsData field so that it can be cached alongside the list.
		}
		return results;
	}

	/**
	 * A method that will generate a csv file for the Advanced Over Time page's current data.
	 * @param list the list of row data returned via the getListOfRows() method.
	 * @param comparisonFactor the benchmark to compare either homelessness or at risk of homelessness upon, so that the title is appropriate.
	 * @see app.builders.AOFBuilder#getListOfRows(ArrayList, ArrayList, ArrayList, String, String, String, String, RangePair, RangePair, RangePair, RangePair, RangePair) 
	 * @return a string that will contain all of the csv's contents, which will be interpreted by Javalin.
	 */
	public String generateCSVFile(RowDataList<AOFRowData> list, String comparisonFactor)
	{
		if (list.getAofCSVContents() == null)
		{
			StringBuilder csvBuilder = new StringBuilder();
			csvBuilder.append("LGA NAME, NUMBER OF HOMELESS PEOPLE, PERCENTAGE OF POPULATION,")
					.append(comparisonFactor.toUpperCase()).append("\n");

			for (AOFRowData aofRowData : list)
			{
				csvBuilder.append(aofRowData.getLgaName()).append(",").append(aofRowData.getNumAffected()).append(",")
						.append(aofRowData.getPercentOfPop()).append(",").append(aofRowData.getComparisonValue()).append("\n");
			}
			return csvBuilder.toString();
		}

		return list.getAofCSVContents();
	}
}
