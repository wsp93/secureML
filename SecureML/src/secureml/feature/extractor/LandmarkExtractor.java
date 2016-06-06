package secureml.feature.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import secureml.Const;

public class LandmarkExtractor {
	
	public static List<Integer> extract(String path)
	{
		List<Integer> list = new ArrayList<Integer>();
		String s = "";
		String result = "";
		
		try
		{
			Process p = Runtime.getRuntime().exec(Const.LANDMARK_COMMAND + " "
												+ Const.LANDMARK_DATA + " "
												+ path);
			BufferedReader stdInput = new BufferedReader(new
	                									InputStreamReader(p.getInputStream()));
			while ((s = stdInput.readLine()) != null) 
			{
	             result += s + "\n";
			}
			
			return parseResult(result);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return list;
	}
	
	private static List<Integer> parseResult(String resultStr)
	{
		List<Integer> list = new ArrayList<Integer>();
		
		Scanner lineScanner = new Scanner(resultStr);
		while(lineScanner.hasNextLine())
		{
			String line = lineScanner.nextLine(); // (x, y)
			String noParens = line.substring(1, line.length() - 1); // x, y
			Scanner pointScanner = new Scanner(noParens);
			pointScanner.useDelimiter(", ");
			
			while(pointScanner.hasNextInt())
			{
				list.add(pointScanner.nextInt());
			}
			
			pointScanner.close();
		}
		lineScanner.close();
		
		return list;
	}
}
