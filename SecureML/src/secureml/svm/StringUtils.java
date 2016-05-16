package secureml.svm;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class StringUtils
{
	public static final String[] COMPARISONS = {"<=", ">=", "<", "=", ">"};
	
	/**
	 * Converts a String representation of a comparison type (<, <=, =, >=, >) into
	 * a boolean evaluation of "inputValue" compared against "comparand."
	 */
	public static boolean compare(BigDecimal inputValue, String comparisonType, BigDecimal comparand)
	{
		switch(comparisonType)
		{
			case "<":
				return inputValue.compareTo(comparand) < 0;
			case "=":
				return inputValue.compareTo(comparand) == 0;
			case ">":
				return inputValue.compareTo(comparand) > 0;
			case "<=":
				return inputValue.compareTo(comparand) <= 0;
			case ">=":
				return inputValue.compareTo(comparand) >= 0;
			default:
				throw new IllegalArgumentException("Invalid comparison type: " + comparisonType);
		}
	}
	
	/**
	 * Converts the string representation of a Double[] back into a Double[].
	 */
	public static List<BigDecimal> stringToDoubleArray(String arrayRep)
	{
		List<BigDecimal> list = new ArrayList<BigDecimal>();
		String data = arrayRep.substring(1, arrayRep.length() - 1);	// only look at info between the [] brackets.
		Scanner s = new Scanner(data);
		
		s.useDelimiter(", ");
		while(s.hasNext())
		{
			list.add(new BigDecimal(s.next()));
		}
		
		return list;
	}
	//~ public static List<List<BigDecimal>> stringToListOfDoubleArrays(String listArrayRep)
	//~ {
		//~ List<List<BigDecimal>> list = new ArrayList<List<BigDecimal>>();
		//~ String listData = listArrayRep.substring(1, listArrayRep.length() - 1);	// only look at info between the [] brackets.
		//~ Scanner listScanner = new Scanner(listData);
		//~ listScanner.useDelimiter("], ");
		//~ 
		//~ while(listScanner.hasNext())
		//~ {
			//~ List<BigDecimal> arrayAsList = new ArrayList<BigDecimal>();
			//~ String arrayRep = listScanner.next();
			//~ String data = arrayRep.substring(1, arrayRep.length() - 1);	// only look at info between the [] brackets.
			//~ Scanner s = new Scanner(data);
			//~ 
			//~ s.useDelimiter(", ");
			//~ while(s.hasNext())
			//~ {
				//~ arrayAsList.add(new BigDecimal(s.next()));
			//~ }
			//~ 
			//~ list.add(arrayAsList);
		//~ }
		//~ 
		//~ return list;
	//~ }
	
	/**
	 * Converts the string representation of a Map back into a Map.
	 */
	//~ public static Map<String, Double> stringToMap(String mapRep)
	//~ {
		//~ Map<String, Double> map = new HashMap<String, Double>();
		//~ String data = mapRep.substring(1, mapRep.length() - 1);	// only look at info between the {} brackets.
		//~ Scanner s = new Scanner(data);
		//~ 
		//~ s.useDelimiter(", ");
		//~ while(s.hasNext())
		//~ {
			//~ String[] keyValPair = s.next().trim().split("=");
			//~ String key = keyValPair[0];
			//~ Double value = new Double(keyValPair[1]);
			//~ map.put(key, value);
		//~ }
		//~ 
		//~ return map;
	//~ }
	public static List<Map<String, BigDecimal>> stringToListofMaps(String listMapRep)
	{
		List<Map<String, BigDecimal>> list = new ArrayList<Map<String, BigDecimal>>();
		String listData = listMapRep.substring(1, listMapRep.length() - 2);	// only look at info between the [] brackets.
		Scanner listScanner = new Scanner(listData);
		listScanner.useDelimiter("}, ");
		
		while(listScanner.hasNext())
		{
			Map<String, BigDecimal> map = new HashMap<String, BigDecimal>();
			String mapRep = listScanner.next();
			String mapData = mapRep.substring(1, mapRep.length());	// only look at info after the { bracket.
			Scanner s = new Scanner(mapData);
			
			s.useDelimiter(", ");
			while(s.hasNext())
			{
				String[] keyValPair = s.next().trim().split("=");
				String key = keyValPair[0];
				BigDecimal value = new BigDecimal(keyValPair[1]);
				map.put(key, value);
			}
			
			list.add(map);
		}
		
		return list;
	}
	
	/**
	 * Returns a List of doubles from a String representation of the list.
	 * Assumes file format of double,double,double,...,double. In other words, between every comma,
	 * there is a double (no spaces).
	 * Assumes inputScanner has next line.
	 */
	public static List<BigDecimal> parseDoubleList(Scanner inputScanner)
	{
		List<BigDecimal> doubleList = new ArrayList<BigDecimal>();
		Scanner lineScanner = new Scanner(inputScanner.nextLine());
		
		lineScanner.useDelimiter(",");
		while(lineScanner.hasNext())
		{
			doubleList.add(new BigDecimal(lineScanner.next()));
		}
		
		lineScanner.close();
		
		return doubleList;
	}
	
	/**
	 * Returns a List of strings from a String representation of the list.
	 * Assumes file format of "string","string","string",...,"string". 
	 * In other words, between every comma, there is a string surrounded by "" marks (no spaces).
	 * Assumes inputScanner has next line.
	 */
	public static List<String> parseStringList(Scanner inputScanner)
	{
		List<String> list = new ArrayList<String>();
		Scanner lineScanner = new Scanner(inputScanner.nextLine());
		
		lineScanner.useDelimiter(",");
		while(lineScanner.hasNext())
		{
			String element = lineScanner.next();
			list.add(element.substring(1, element.length() - 1));
		}
		
		lineScanner.close();
		
		return list;
	}
	
	/**
	 * Skips lines that the scanner won't use.
	 */
	public static void skipLines(Scanner s, int linesToSkip)
	{
		for(int i = 0; i < linesToSkip && s.hasNextLine(); i++)
		{
			s.nextLine();
		}
	}
	
	public static List<BigDecimal> extractDecimals(String toScan)
	{
		Scanner scanner = new Scanner(toScan);
		List<BigDecimal> list = new ArrayList<BigDecimal>();
			
		scanner.useDelimiter(",");
		while(scanner.hasNext())
		{
			String data = scanner.next().trim();
			try
			{
				list.add(new BigDecimal(data));
			}
			catch(NumberFormatException e)
			{
				continue;
			}
		}
		
		return list;
	}
	
	/**
	 * Stores the contents of a text file into a String.
	 */
	public static String readFile(String filename)
	{
		StringBuilder result = new StringBuilder();
		try(Scanner fileScanner = new Scanner(new File(filename)))
		{
			while(fileScanner.hasNextLine())
			{
				result.append(fileScanner.nextLine() + "\n");
			}
		}
		catch(FileNotFoundException e)
		{
			System.out.println(e.getMessage());
		}
		
		return result.toString();
	}
}
