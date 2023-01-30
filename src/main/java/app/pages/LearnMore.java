package app.pages;

import app.builders.LMBuilder;
import app.customtypes.lmtypes.RowData;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The page responsible for presenting the requested data to users directly from the database via a simple interface.
 *
 * @author Matthew Yamen
 * @author Alexander Mitchell
 */
public class LearnMore implements Handler
{
	private static final String TEMPLATE = "learn-more.html";

	private final LMBuilder lmBuilderInstance = LMBuilder.getLMBuilder(); //Get singleton instance of JDBCConnection.
	private final HashMap<String, Object> model = new HashMap<>(9); // Initial capacity set to 9, shouldn't need more.

	public LearnMore ()
	{
		/* Retrieve the list of locations and populate the dropdown with it (pre-load before #handle() is called).
		 * Does not need to be replaced each time handle() is called - that is inefficient.*/
		model.put("locationNames", lmBuilderInstance.getLocationsList());
	}

	//TODO implement when the connection to the db should be closed. See if there are event handlers to see when user clicks on diff page.
	// Also check when another should be established.

	@Override
	public void handle (@NotNull Context context)
	{
		fillModel(context);
		context.render(TEMPLATE, model);
	}

	//Fill the model with the newly obtained input from the user.
	private void fillModel (Context context)
	{
		//Temporarily store for readability and to prevent multiple method invocations for the same result.
		String homelessnessType = StaticUtils.tableTypeToDBForm(context.queryParam("homelessnessType")); String sortBy = context.queryParam("sortBy");
		ArrayList<String> genderFilter = StaticUtils.genderToDBForm(StaticUtils.ensureArrayList(context.queryParams("genderFilter")));
		ArrayList<String> ageFilter = StaticUtils.ageToDBForm(StaticUtils.ensureArrayList(context.queryParams("ageFilter")));
		String locationName = context.queryParam("locationName");

		model.put("dataAvailable", false);

		if (sortBy != null && locationName != null)
		{
			RowDataList<RowData> rowList = lmBuilderInstance.getListOfRows(
					StaticUtils.getStateOrLGA(locationName), StaticUtils.getSortType(sortBy),
					homelessnessType, genderFilter,
					ageFilter);

			if (rowList.size() != 0) //If there is user input and it is not an invalid query, produce results.
			{
				model.put("tableData", rowList);
				model.put("dataTotal", rowList.getDynamicTotal()); //Total count for the data.
				model.put("dataAvailable", true);
			}
		}

		//Echo form data back to the user. This means they keep their options when they reload the page/submit the form.
		//This should always happen, even when there is no data to be output.
		model.put("sortBy", sortBy);
		model.put("locationName", locationName);
		model.put("homelessnessType", homelessnessType);
		model.put("genderFilter", genderFilter);
		model.put("ageFilter", ageFilter);
	}
}
