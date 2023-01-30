package app.pages;

import app.builders.AOFBuilder;
import app.builders.AOTBuilder;
import app.customtypes.aatypes.AOFRowData;
import app.customtypes.aatypes.AOTRowData;
import app.customtypes.customcollections.RowDataList;
import app.utils.StaticUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

/**
 * The page responsible for downloading the csv file (is re-directed here).
 * There was not enough time for an optimal solution, hence, a rushed but working solution was delivered (this class).
 *
 * @author Matthew Yamen
 */
public class CSVPage implements Handler
{
	private AOTBuilder aotBuilderInstance;
	private AOFBuilder aofBuilderInstance;
	private AdvancedOverTime aotInstance;
	private AdvancedOtherFactors aofInstance;
	private final String type;

	public CSVPage(String type, Object AAInstance)
	{
		//Depending on which type of object this is, only instantiate some fields and leave the rest to default to null.
		//This will allow us to determine which type of object this is, and what methods should be called for each.
		if (type.equalsIgnoreCase("AOT"))
		{
			this.aotInstance = (AdvancedOverTime) AAInstance;
			this.aotBuilderInstance = AOTBuilder.getAOTBuilder();
		}
		else
		{
			this.aofInstance = (AdvancedOtherFactors) AAInstance;
			this.aofBuilderInstance = AOFBuilder.getAOFBuilder();
		}
		this.type = type;
	}

	@Override
	public void handle (@NotNull Context context) throws Exception
	{
		if (aofInstance != null && aofBuilderInstance != null)
		{
			context.result(aofBuilderInstance.generateCSVFile(
					(RowDataList<AOFRowData>) aofInstance.model.get("tableData"),
					StaticUtils.comparisonFactorToCSVTitle((String) aofInstance.model.get("comparisonFactor"))))
					.contentType("text/csv").header("Content-Disposition","attachment; filename=homelessnessAuDownload.csv");
		}
		else if (aotInstance != null && aotBuilderInstance != null)
		{
			context.result(aotBuilderInstance.generateCSVFile((RowDataList<AOTRowData>) aotInstance.model.get("tableData")))
					.contentType("text/csv").header("Content-Disposition","attachment; filename=homelessnessAuDownload.csv");
		}
	}
}
