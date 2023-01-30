package app.builders;

import app.customtypes.customcollections.LRUCache;
import app.customtypes.lmtypes.RowData;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.constant.ConstantDesc;
import java.lang.ref.SoftReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;

/**A singleton class for managing the data built for the Learn More page.
 *
 * @author Matthew Yamen
 */
public final class LMBuilder extends JDBCBuilder
{
	private static LMBuilder singletonInstance;

	private int dynamicTotal;
	private SoftReference<LRUCache<String, RowDataList<RowData>>> softCachedList =
			new SoftReference<>(new LRUCache<>(35)); //Soft Reference used to mitigate memory leaks.

	private LMBuilder() {}

	//----UTILITY METHODS----
	//PUBLIC
	/**Get the single instance of the LMBuilder class. Disallow concurrent or parallel access.
	 * @return An LMBuilder singleton instance.
	 */
	public static synchronized LMBuilder getLMBuilder()
	{
		return (LMBuilder.singletonInstance == null) ?
				LMBuilder.singletonInstance = new LMBuilder() : LMBuilder.singletonInstance;
	}

	//PRIVATE
	//Sets the dynamic total within this class - to be displayed to user.
	private void setAmountTotal(StringBuilder queryToModify)
	{
		if (!queryToModify.toString().contains("SUM(amount)")) queryToModify.insert(6, " SUM(amount),").append(";");
		ResultSet results = generateResults(queryToModify.toString());

		try { this.dynamicTotal = results.getInt("SUM(amount)"); }
		catch (SQLException e ) { e.printStackTrace(); }
	}

	//Builds a query based on the user's input - to be passed to generateResults().
	private String queryBuilder(ConstantDesc requestType, String sortType, String dataType, ArrayList<String> gender, ArrayList<String> ageGroup)
	{
		StringBuilder queryBuilder = (new StringBuilder()).append("SELECT agerange, gender,");

		boolean integer = (requestType instanceof Integer);

		queryBuilder.append(integer ? " SUM(amount) FROM " + dataType : " amount FROM (" + dataType + " d1 JOIN lgas l1 ON d1.lgacode = l1.lgacode)");
		queryBuilder.append(integer ? " WHERE lgacode LIKE '" + requestType + "%'" : " WHERE LOWER(name) = '" + requestType + "'");
		settingsBuilder(gender, ageGroup, queryBuilder); //Will apply the gender and agegroup settings as requested.

		setAmountTotal(new StringBuilder(queryBuilder)); //Set the total amount of rows.

		if (integer) queryBuilder.append(" GROUP BY agerange, gender ORDER BY SUM(amount) "); //Add a grouping clause if it is by state.
		else queryBuilder.append(" ORDER BY amount ");

		return queryBuilder.append(sortType).append(";").toString();
	}

	/*Generates the selected user settings (for age and gender)
	NOTE: This functionality is shared between both at risk/homelessness and state/lga - regardless of what is chosen.*/
	private void settingsBuilder(ArrayList<String> gender, ArrayList<String> ageGroup, StringBuilder builder)
	{
		if (ageGroup.size() == 8 && gender.size() != 2 && gender.size() != 0) builder.append(" AND gender = " + "'").append(gender.get(0)).append("'");
		else if (ageGroup.size() != 8 && (gender.size() == 2 || gender.size() == 0)) iterateOptionLists(builder, ageGroup);
		else if (ageGroup.size() != 8)
		{
			iterateOptionLists(builder, ageGroup);
			builder.append(" AND gender = '").append(gender.get(0)).append("'");
		}
		builder.append(" AND year = 2018");
	}

	//Belongs to the settingsBuilder() method entirely. Utilised to append the options and prevent code repetition.
	private void iterateOptionLists (StringBuilder builder, ArrayList<String> optionList)
	{
		int i = 1;
		builder.append(" AND (");
		//Used instead of traditional for loop because it is slightly more performant due to not having to traverse the list each iteration to find the element at position (i).
		//This enhanced for-loop is really just an iterator internally - verified by checking the compiled bytecode.
		for (String option : optionList)
		{
			builder.append((i == optionList.size()) ? "agerange" + " = " + "'" + option + "'" : "agerange" + " = " + "'" + option + "' OR ");
			++i;
		}
		if (optionList.size() != 8) builder.append(")");
	}

	//----DATA FETCHING METHODS----
	/**Method for obtaining a list of rows by state or LGA.
	 * @param requestType the type of data requested (state: Integer or LGA: String).
	 * @param sortType can either be best (default) or worst.
	 * @param dataType "at_risk" represents at risk and "homeless" represents homelessness.
	 * @param genderSelected the list of gender(s) that have been selected.
	 * @param ageGroup the list of age group(s) that have been selected.
	 * @return a list of rows to populate table with.*/
	public RowDataList<RowData> getListOfRows(ConstantDesc requestType, @NotNull String sortType, @NotNull final String dataType,
											  @NotNull final ArrayList<String> genderSelected, @NotNull final ArrayList<String> ageGroup)
	{
		/* Unique identifier for this specific query.
		 * NOTE: String interning ensures two logically equivalent queries options will produce the same pointer to
		 * the existing string within the String Pool.*/
		String identifier = (requestType.toString() + sortType + dataType + genderSelected + ageGroup); //requestType object will call its overridden toString() method for Integer and String classes (upcasting).

		//If the soft reference has been cleaned, we must resurrect it and then generate a rows list (prevent NPE).
		if (softCachedList.get() == null) this.softCachedList = StaticUtils.resurrectSoftRefObject(softCachedList, 35);
		//Generate a new row list if the specific query has not already been searched before.
		RowDataList<RowData> rowsList = Objects.requireNonNull(softCachedList.get()).generateNewList(softCachedList.get().get(identifier));

		//If by state get SUM(amount) column, else get the standard amount column.
		String tempDBColumn = (requestType instanceof Integer ? "SUM(amount)" : "amount");

		if (rowsList.size() == 0) //If its empty, that means the query did not previously exist.
		{
			ResultSet results = generateResults(queryBuilder(requestType, sortType, dataType, genderSelected, ageGroup));
			try
			{
				while (results.next())
				{
					rowsList.add(new RowData(results.getString("agerange"),
							results.getString("gender"), results.getInt(tempDBColumn)));
				}
				results.getStatement().close(); //Close the statement.
				rowsList.setDynamicTotal(this.dynamicTotal);
			}
			catch (SQLException e) { e.printStackTrace(); }

		}
		Objects.requireNonNull(softCachedList.get()).put(identifier, rowsList); //Once no exceptions and successful filling of array, add to cache list.

		return rowsList;
	}


}
