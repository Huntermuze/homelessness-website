# Form Parameters

homelessType: either "HomelessnessData" or "AtRiskData"  
Defines the homelessness data to be used

comparisonFactor: one of "medianAge", "medianMortgage", "medianRent", or "medianIncome"  
Defines the data the homelessness data will be compared against

location: a list of LGA name strings  
Defines the LGAs to be included in the final output

genderFilter: a list containing one or both of "Male", "Female"  
Defines the genders to be included when summing all entries for each LGA

ageFilter: a list containing at least one of "0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60+", "Unknown"  
Defines the ages to be included when summing all entries for each LGA

For the following entries, replace [x] with one of "population", "medianAge", "medianIncome", "medianMortgage", "
medianRent"  
[x]FilterMin: An integer  
LGAs with a value lower than this for the given statistic should be excluded from the final list.  
[x]FilterMax: An integer  
LGAs with a value higher than this for the given statistic should be excluded from the final list.

sortBy: one of "homeless", "population", "homelessPercentage", "medianAge", "medianMortgage", "medianRent", or "
medianIncome"  
Defines the statistic that the final list should be sorted by.  
Note that "homeless" and "homelessPercentage" should use data for at risk of homelessness when homelessType is "
AtRiskData"

sortAscending: a boolean, true or false (although likely given by Javalin as "checked" or null)  
if false, the output should be sorted descending, if true it should be sorted ascending.

# Model Parameters

homelessType: echoed back from form data  
comparisonFactor: echoed back from form data  
selectedLocations: a list of LGA names, echoed back from the location form parameter  
genderFilter: echoed back from form data  
ageFilter: echoed back from form data  
sortBy: echoed back from form data  
sortAscending: echoed back from form data

locationGroups: a list of objects with the following properties:  
Each object in the list should have two properties: name, which is the name of the group (likely the state name), and
list, which is a list of strings containing LGA names. This should be all the LGAs in the DB

For the following entry, replace [x] with one of "population", "medianAge", "medianIncome", "medianMortgage", "
medianRent"  
[x]Filter: an object with two properties, range and value  
Both range and value should have two properties: min and max  
range is the allowed range of the input, should be set to the minimum and maximum of the data in the DB  
value is the preset values for the min and max inputs. Should be echoed back from form data when present, and otherwise
should be set to the same values as range.

dataAvailable: a boolean, should be true whenever there is any data to be displayed.

requestType: either "GET" or "POST", this should be set based on the type of request. I've implemented this by just
passing a string to the constructor in HomelessAu.java, as I don't think javalin has a method to get this.

results: an object with the following properties: "total", "average", "highestValue", "highestPercentage",
"lowestValue", "lowestPercentage"  
Each object has the same properties:  
location: a string with the LGA name (should be null for total and average)  
value: the amount of homeless/at risk people  
ratio: a float/double between 0 and 1 which represents the percentage of homeless/at risk people vs total population  
comparison: the value for the statistic set by comparisonFactor (should be null for total)

tableData: a list of rows for the data table. each row should contain the data for one LGA.  
Each row should have the following properties:  
location: a String for the LGA Name  
value: the amount of homeless/at risk people for that LGA  
ratio: a float/double between 0 and 1 which is the percentage of homeless/at risk people vs total population  
comparison: the statistic given by comparisonFactor for that LGA

# CSV output

advanced-other-factors.csv is a page which takes the same form parameters, and it should have the same contents as
tableData, plus a heading.

```java
ctx.result(csvString)
		.contentType("text/csv")
		.header("Content-Disposition","attachment; filename=homelessnessAuDownload.csv");
```