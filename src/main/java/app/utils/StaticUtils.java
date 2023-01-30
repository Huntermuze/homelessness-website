package app.utils;

import app.customtypes.customcollections.LRUCache;

import java.lang.constant.ConstantDesc;
import java.lang.ref.SoftReference;
import java.util.*;

/**A "static" helper class that will never have an instance. An inner static class could have been used, however
 * that requires we instantiate the inner or outer class. In this utility, instantiation is prohibited.
 * Only the static methods will be utilised within this class and nothing else.
 *
 * These methods exist as a form of validating input to prevent SQL injections. Irrespective of whether the user
 * attempts to manipulate the form input, it will always return a pre-defined result that is safe.
 *
 * @author Matthew Yamen
 */
public class StaticUtils
{
	private static final String regexPattern = "(?i)[^a-zA-Z \\\\/.()-]|select|from|join|where|like|order|insert|drop|delete";

	//Disallow instantiation. This is a "static" class with no instances.
	private StaticUtils() {}

	//----SHARED----

	//Convert the search type to a valid SQL, ORDER BY clause option.
	public static String getSortType(String sortType)
	{ return (sortType.equalsIgnoreCase("best") ? "ASC" : "DESC"); }

	//Convert the data type form input to a valid db table.
	public static String tableTypeToDBForm(String table)
	{
		if (table == null) return "HomelessnessData";
		else if (table.equalsIgnoreCase("AtRiskData")) return "AtRiskData";
		else if (table.equals("HomelessnessData")) return "HomelessnessData";
		else return "HomelessnessData";
	}

	/* The context.queryParams() returns either a SingletonList or an ArrayList which was problematic.
	 * Hence this helper method has been crafted to enforce type ArrayList.*/
	public static <T> ArrayList<T> ensureArrayList(List<T> t)
	{ return (t.getClass().equals(ArrayList.class) ? (ArrayList<T>) t : (new ArrayList<>(t))); }

	/* The Soft Reference may be garbage collected when the (most likely, as it will survive for a while) tenured space
	 * is about to be full (before an OutOfMemory exception is thrown) via a major (or full) gc. Hence, we must be
	 * able to resurrect the object (or rather, create a new one) in this event.*/
	public static <T, K> SoftReference<LRUCache<T, K>> resurrectSoftRefObject(SoftReference<LRUCache<T, K>> obj, int capacity)
	{ return (obj.get() == null) ? new SoftReference<>(new LRUCache<>(capacity)) : obj; }

	//Validate the input from the Age options so as to prevent any malicious injection
	public static ArrayList<String> ageToDBForm(ArrayList<String> ageList)
	{
		ageList.removeIf(age -> !validAge(age));
		return (ageList.size() != 0 ? ageList :
				(new ArrayList<>(Arrays.asList("0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60+", "Unknown"))));
	}

	//Another form of validation to prevent SQL injection.d
	public static ArrayList<String> genderToDBForm(ArrayList<String> genderList)
	{
		boolean validList = true;
		for (String gender : genderList)
		{
			if (!(gender.equals("Male") || gender.equals("Female")))
			{
				validList = false;
				break;
			}
		}
		return (validList ? genderList : (new ArrayList<>(Arrays.asList("Male", "Female"))) );
	}

	/* Adds in the additional search options to search by state.
	 * NOTE: This is tedious, messy and partially inefficient. A better solution would have been to have added a "State"
	 * field in the LGAs table in the db. However, that would require a large re-do of this functionality.*/
	public static ArrayList<String> addStatesToLocList(ArrayList<String> list)
	{
		list.add("New South Wales"); list.add("Australian Capital Territory"); list.add("Victoria");
		list.add("Queensland"); list.add("South Australia"); list.add("Western Australia");
		list.add("Northern Territory"); list.add("Tasmania"); list.add("Offshore Island Territories");
		list.add("Jervis Bay Territory"); list.add("Australian Antarctic Territory");
		return list;
	}

	//Converts the provided integer (LGA state code) into its relevant state as a String literal.
	public static String getStateFromInt(int input)
	{
		return switch (input)
				{
					case 1 -> "New South Wales";
					case 2 -> "Victoria";
					case 3 -> "Queensland";
					case 4 -> "South Australia";
					case 5 -> "Western Australia";
					case 6 -> "Tasmania";
					case 7 -> "Northern Territory";
					case 8 -> "Australian Capital Territory";
					default -> "Other Territories";
				};
	}

	//Obtains the sort type for both Advanced Analysis pages.
	public static String getAASortType (String input)
	{ return (input == null) ? "DESC" : "ASC"; }

	//Converts the data returned by the user into format that is readable by the database. Also prevents SQL injection.
	public static String orderToDBFormAA (String input)
	{
		return switch(input)
				{
					case ("homeless") -> "value";
					case ("population") -> "populationdata.population";
					case ("homelessPercentage") -> "ratio";
					case ("medianMortgage") -> "incomedata.monthlymortgagerepayment";
					case ("medianRent") -> "incomedata.weeklyrent";
					case ("medianIncome") -> "incomedata.weeklyhouseholdincome";
					default -> "populationdata.medianage"; //Safety measure as method is used twice for different areas.
				};
	}

	//Replace everything that is not a-z (ignoring casing) or a space (the pattern).
	private static String lgaValidation(String lga)
	{ return lga.replaceAll(regexPattern, "").toLowerCase(); }

	//A check to validate whether the age(s) entered are possible ages. Used only within this class.
	private static boolean validAge(String input)
	{
		return switch(input)
				{
					case ("0-9"), ("10-19"), ("20-29"), ("30-39"), ("40-49"), ("50-59"), ("60+"), ("Unknown") -> true;
					default -> false;
				};
	}

	//Return the LGA number respective to each state. Used only within this class.
	private static Integer getState(String input)
	{
		return switch (input.toLowerCase())
				{
					case ("nsw"), ("new south wales") -> 1;
					case ("vic"), ("victoria") -> 2;
					case ("qld"), ("queensland") -> 3;
					case ("sa"), ("south australia") -> 4;
					case ("wa"), ("western australia") -> 5;
					case ("tas"), ("tasmania") -> 6;
					case ("nt"), ("northern territory") -> 7;
					case ("act"), ("australian capital territory") -> 8; //other territories.
					case ("offshore island territories"), ("jervis bay territory"), ("australian antarctic territory") -> 9;
					default -> -1;
				};
	}


	//----LEARN MORE PAGE UTILS----

	/* A method to get the state code or LGA name based on the user input.
	 * NOTE: The ConstantDesc interface has been used here as opposed to the Object class, as to limit the amount of
	 * potential objects passed to this method. All static constants that are expressible natively in the constant pool
	 * implement this interface (String, Integer, Long, Float, Double etc). Hence, mitigating the room for error and
	 * misuse, as initially it was of type Object, which allowed for *any* object - regardless of type.*/
	public static ConstantDesc getStateOrLGA(String userInput)
	{
		String validatedInput = lgaValidation(userInput);
		Integer tempState = getState(validatedInput); //Autobox primitive int to Integer wrapper class.
		return (tempState == -1 ? validatedInput: tempState);
	}


	//----ADVANCED OTHER FACTORS UTILS----

	//Converts the comparison factor returned by the user into a neat heading for a particular column in the CSV file.
	public static String comparisonFactorToCSVTitle(String comparisonFactor)
	{
		return switch(comparisonFactor)
				{
					case ("medianAge") -> "Median Age";
					case ("medianMortgage") -> "Median Monthly Mortgage Repayment";
					case ("medianRent") -> "Median Weekly Rent";
					default -> "Median Weekly Income";
				};
	}

	//----ADVANCED OVER TIME UTILS----

	//Converts the user input for the ordering value into database readable input. Also prevents SQL injection.
	public static String orderByToDBForm(String input)
	{
		return switch(input)
		{
			case ("2016") -> "value2016";
			case ("2018") -> "value2018";
			case ("percentageChange") -> "ratio";
			default -> "change";
		};
	}

	//Converts the population change filter generated via the user into database readable input. Also prevents SQL injection.
	public static String popChangeToDBForm(String input)
	{
		return switch(input.toLowerCase())
				{
					case ("increase") -> ">";
					case ("decrease") -> "<";
					default -> "all";
				};
	}

	//Converts all of the states within the locations list into their respective LGA code state numbers, so that
	//the whole state, containing all of its respective LGAs can be obtained.
	public static ArrayList<String> getStateOrLGAS(ArrayList<String> locations)
	{
		for(int i = 0; i < locations.size(); ++i)
		{
			if (getState(locations.get(i)) != -1) locations.set(i, String.valueOf(getState(locations.get(i))));
		}
		return locations;
	}


}
