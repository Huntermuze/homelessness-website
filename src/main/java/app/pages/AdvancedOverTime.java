package app.pages;

import app.builders.AOTBuilder;
import app.customtypes.aatypes.AOTRowData;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The page responsible for presenting the relevant data for the AdvancedOverTime page to the user upon request.
 *
 * @author Matthew Yamen
 * @author Alexander Mitchell
 */
public class AdvancedOverTime implements Handler
{
	private static final String TEMPLATE = "advanced-over-time.html";

	private final AOTBuilder aotBuilderInstance = AOTBuilder.getAOTBuilder(); //Get singleton instance of AOTBuilder.
	protected final HashMap<String, Object> model = new HashMap<>(15); // Initial capacity set to 15, shouldn't need more.

	public AdvancedOverTime(String requestType)
	{
		/* Retrieve the list of locations (and other items) and populate the dropdown with it (pre-load before #handle() is called).
		 * Does not need to be replaced each time handle() is called - that is inefficient.*/
		model.put("locationGroups", aotBuilderInstance.getLocationGroupArray());
		model.put("populationFilterMin", 0);
		model.put("populationFilterMax", 1300000);

		if (requestType.equals("POST"))
		{
			model.put("requestType", "POST");
			model.put("dataAvailable", true);
		}
		else
		{
			model.put("requestType", "GET");
			model.put("dataAvailable", false);
		}
	}

	@Override
	public void handle(@NotNull Context context)
	{
		fillModel(context);
		context.render(TEMPLATE, model);
	}

	//Fill the model with the newly obtained input from the user.
	private void fillModel(Context context)
	{
		ArrayList<String> locations = StaticUtils.ensureArrayList(context.formParams("location"));
		ArrayList<String> genderFilter = StaticUtils.ensureArrayList(context.formParams("genderFilter"));
		ArrayList<String> ageFilter = StaticUtils.ensureArrayList(context.formParams("ageFilter"));
		ArrayList<String> popChange = StaticUtils.ensureArrayList(context.formParams("populationChangeFilter"));
		String homelessnessType = context.formParam("homelessType"); String sortBy = context.formParam("sortBy");
		String sortByAscending = context.formParam("sort-ascending"); String popMin = context.formParam("populationFilterMin");
		String popMax = context.formParam("populationFilterMax");

		if (popMin != null && popMax != null && sortBy != null)
		{
			model.put("populationFilterMin", popMin);
			model.put("populationFilterMax", popMax);

			if (homelessnessType != null)
			{
				RowDataList<AOTRowData> rowsList =  aotBuilderInstance.getListOfRows(StaticUtils.getStateOrLGAS(locations),
						popChange, StaticUtils.genderToDBForm(genderFilter), StaticUtils.ageToDBForm(ageFilter),
						StaticUtils.orderByToDBForm(sortBy), StaticUtils.getAASortType(sortByAscending),
						StaticUtils.tableTypeToDBForm(homelessnessType), Integer.parseInt(popMin), Integer.parseInt(popMax));

				if (rowsList.size() != 0) //If the data is invalid or does not exist, display an error.
				{
					model.put("results", aotBuilderInstance.getResultsStatistics(rowsList));
					model.put("dataAvailable", true);
					model.put("tableData", rowsList);
				}
				else model.put("dataAvailable", false);
			}
		}

		//Echo form data
		model.put("homelessType", homelessnessType);
		model.put("selectedLocations", locations);
		model.put("genderFilter", genderFilter);
		model.put("ageFilter", ageFilter);
		model.put("populationChangeFilter", popChange);
		model.put("sortBy", sortBy);
		model.put("sortByAscending", sortByAscending);
	}
}
