package app.customtypes.customcollections;

import app.customtypes.aatypes.AOFResultsData;
import app.customtypes.aatypes.AOTResultsData;

import java.util.ArrayList;
/**
 * A wrapper class for the ArrayList class, which contains the total homeless/at risk for each particular query.
 * Fixes the problem where the dynamic total is not updated for cached queries and provides the opportunity for
 * various other datatypes to be bundled with their respective queries and thus, cached.
 *
 * @author Matthew Yamen
 */
public final class RowDataList<T> extends ArrayList<T>
{
	private int rowDataQueryTotal; //Dynamic total
	private AOTResultsData aotResultsData;
	private AOFResultsData aofResultsData;
	private String aotCSVContents;
	private String aofCSVContents;

	public void setDynamicTotal(int total) { this.rowDataQueryTotal = total; }
	public void setAotResultsData(AOTResultsData aotResultsData) { this.aotResultsData = aotResultsData; }
	public void setAofResultsData(AOFResultsData aofResultsData) { this.aofResultsData = aofResultsData; }
	public void setAotCSVContents(String contents) { aotCSVContents = contents; }
	public void setAofCSVContents(String contents) { aofCSVContents = contents; }

	public int getDynamicTotal() { return rowDataQueryTotal; }
	public AOTResultsData getAotResultsData() { return aotResultsData; }
	public AOFResultsData getAofResultsData() { return aofResultsData; }
	public String getAotCSVContents() { return aotCSVContents; }
	public String getAofCSVContents() { return aofCSVContents; }
}
