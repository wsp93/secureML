package secureml.feature.extractor;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author SISI
 * @date May 15,2016
 * 
 * developed for
 * University of Washington, Tacoma
 * Secure Maching Learning Team
 * secureml.insttech.washington.edu
 */

public class LIWCExtractor {
	
	/**
	 * Extract LIWC19 features of input String
	 * @param input - String
	 * @return results - Arraylist<Double>
	 */
	public ArrayList<Double> extract(String input) {
		ArrayList<Double> results = new ArrayList<Double>();
		LIWCDictionary liwc = new LIWCDictionary();
		Map<String,Double> objectMap = liwc.getCounts(input, true);
		for (String key : objectMap.keySet()) {
    	    results.add(objectMap.get(key));
    	}
		return results;
	}
	/*
    public static void main(String[] args) {
    	String text = "A spokeswoman for Mr. Trump, 12 Hope Hicks, insisted that Mr. Lewandowski was 'not arrested' over the allegations that he grabbed the reporter, Michelle Fields, after a news conference at Trump-National ;-) Golf, Club: in; Jupiter ? Instead, she said he �was issued a notice to appear and was given a court date";
    	//File file = new File("./res/LIWC2001WordStat.txt"); 
    	LIWCDictionary liwc = new LIWCDictionary();
    	Map<String,Double> objectMap = liwc.getCounts(text, true);
    	for (String key : objectMap.keySet()) {
    	    System.out.println(key + " " + objectMap.get(key));
    	}
    	System.err.println("Number of features: "+objectMap.keySet().size());
    } 
      */  
}



