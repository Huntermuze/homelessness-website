package app.pages;

import app.builders.AOFBuilder;
import app.customtypes.aatypes.AOFRowData;
import app.customtypes.aatypes.RangePair;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The page responsible for presenting the relevant data for the AdvancedOtherFactors page to the user upon request.
 *
 * @author Matthew Yamen
 * @author Alexander Mitchell
 */
public class AdvancedOtherFactors implements Handler
{
	private static final String TEMPLATE = "advanced-other-factors.html";

	private final AOFBuilder aofBuilderInstance = AOFBuilder.getAOFBuilder(); //Get singleton instance of AOTBuilder.
	protected final HashMap<String, Object> model = new HashMap<>(17); // Initial capacity set to 17, shouldn't need more.

	public AdvancedOtherFactors(String requestType)
	{
		/* Retrieve the list of locations (and other items) and populate the dropdown with it (pre-load before #handle() is called).
		 * Does not need to be replaced each time handle() is called - that is inefficient.*/
		model.put("locationGroups", aofBuilderInstance.getLocationGroupArray());
		model.put("populationFilter", new RangePair(0, 1300000));
		model.put("medianAgeFilter", new RangePair(0, 70));
		model.put("medianIncomeFilter", new RangePair(0, 3500));
		model.put("medianMortgageFilter", new RangePair(0, 3500));
		model.put("medianRentFilter", new RangePair(0, 700));

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
	public void handle (@NotNull Context context)
	{
		fillModel(context);
		context.render(TEMPLATE, model);
	}

	//Fill the model with the newly obtained input from the user.
	private void fillModel (Context context)
	{
		String homelessnessType = context.formParam("homelessType"); String comparisonFactor = context.formParam("comparisonFactor");
		String sortBy = context.formParam("sortBy"); String sortByAscending = context.formParam("sort-ascending");
		ArrayList<String> locations = StaticUtils.ensureArrayList(context.formParams("location"));
		ArrayList<String> genderFilter = StaticUtils.ensureArrayList(context.formParams("genderFilter"));
		ArrayList<String> ageFilter = StaticUtils.ensureArrayList(context.formParams("ageFilter"));

		String popMax = context.formParam("populationFilterMax");String popMin = context.formParam("populationFilterMin");
		String ageMax = context.formParam("medianAgeFilterMax");String ageMin = context.formParam("medianAgeFilterMin");
		String incomeMax = context.formParam("medianIncomeFilterMax");String incomeMin = context.formParam("medianIncomeFilterMin");
		String mortgageMax = context.formParam("medianMortgageFilterMax");String mortgageMin = context.formParam("medianMortgageFilterMin");
		String rentMax = context.formParam("medianRentFilterMax");String rentMin = context.formParam("medianRentFilterMin");


		if (popMin != null &&  ageMin != null && incomeMin != null &&  mortgageMin != null && rentMin != null)
		{
			RangePair populationFilter = new RangePair(Integer.parseInt(popMin), Integer.parseInt(popMax));
			RangePair medianAgeFilter = new RangePair(Integer.parseInt(ageMin), Integer.parseInt(ageMax));
			RangePair medianIncomeFilter = new RangePair(Integer.parseInt(incomeMin), Integer.parseInt(incomeMax));
			RangePair medianMortgageFilter = new RangePair(Integer.parseInt(mortgageMin), Integer.parseInt(mortgageMax));
			RangePair medianRentFilter = new RangePair(Integer.parseInt(rentMin), Integer.parseInt(rentMax));

			if (homelessnessType != null)
			{
				RowDataList<AOFRowData> rowsList = aofBuilderInstance.getListOfRows(StaticUtils.getStateOrLGAS(locations),
						StaticUtils.genderToDBForm(genderFilter), StaticUtils.ageToDBForm(ageFilter),
						StaticUtils.orderToDBFormAA(sortBy), StaticUtils.getAASortType(sortByAscending),
						StaticUtils.tableTypeToDBForm(homelessnessType), StaticUtils.orderToDBFormAA(comparisonFactor),
						populationFilter, medianAgeFilter, medianIncomeFilter, medianMortgageFilter, medianRentFilter);

				if (rowsList.size() != 0) //If the data is invalid or does not exist, display an error.
				{
					model.put("results", aofBuilderInstance.getResultsStatistics(rowsList));
					model.put("dataAvailable", true);
					model.put("tableData", rowsList);
				}
				else model.put("dataAvailable", false);
			}

			model.put("populationFilter", populationFilter);
			model.put("medianAgeFilter", medianAgeFilter);
			model.put("medianIncomeFilter", medianIncomeFilter);
			model.put("medianMortgageFilter", medianMortgageFilter);
			model.put("medianRentFilter", medianRentFilter);
		}

		//Echo form data
		model.put("homelessType", homelessnessType);
		model.put("comparisonFactor", comparisonFactor);
		model.put("selectedLocations", locations);
		model.put("genderFilter", genderFilter);
		model.put("ageFilter", ageFilter);
		model.put("sortBy", sortBy);
		model.put("sortByAscending", sortByAscending);
	}
}
