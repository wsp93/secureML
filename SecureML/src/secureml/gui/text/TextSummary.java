package secureml.gui.text;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author SISI
 * @May 3,2016
 */
public class TextSummary {
	
	int[] big5; // an array of integers where 1 means true and 0 means false in the order of ext, neu, agr, con, ope.
	String[] names; // names of big 5 personality in the same order
	Map<String, String> dict;
	Map<String, String> description;

	public TextSummary(boolean[] input) throws Exception {
		// if the input boolean array does not have exactly 5 arguments, throw exception and quit.
		if (input.length != 5) {
			throw new IllegalArgumentException("Wrong number of arguments.");
		}
		big5 = new int[5];
		getBig5Array(input);
		names = new String[]{"ext", "neu", "agr", "con", "ope"};
		dict = new HashMap<String, String>();
		loadDictionary(dict);
		description = new HashMap<String, String>();
		loadDescription(description);
	}
	
	/**
	 * @param input - an input array of booleans of big 5 personality traits.
	 */
	public void getBig5Array(boolean[] input) {
		for (int i = 0; i < big5.length; i++) {
			big5[i] = input[i] ? 1 : 0; 
		}
	}
	
	/**
	 * @param dict - a mapping of two personality traits as the key and a phrase as the value.
	 * @throws Exception - file IO exception
	 */
	/**
	 * @param dict
	 * @throws Exception
	 */
	public void loadDictionary(Map<String, String> dict) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("res/dictionary"));
		String line = br.readLine();
		while (line != null) {
			line = line.replace("\"", "");
			String[] keyValuePair = line.split(",");
			dict.put(keyValuePair[0], keyValuePair[1]);
			line = br.readLine();
		}	
	}
	
	/**
	 * @param desc - a mapping of a personality trait as the key and a description string as the value;
	 * @throws Exception - file IO exception
	 */
	public void loadDescription(Map<String, String> desc) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("res/description"));
		String line = br.readLine();
		int counter = 0;
		while (line != null) {
			line = line.replace("\"", "");
			desc.put(names[counter++], line);
			line = br.readLine();
		}
				
	}
	
	/**
	 * @return - a text summary of that person bases on his or her big 5 personality traits
	 */
	public String generateSummary() {
		String summary = "";
		summary += "You are:\n\t";
		for (int i = 0; i < big5.length; i++) {
			for (int j = i + 1; j < big5.length; j++) {
				String key = buildKey(i, j);
				summary += getPhrase(key) + "\n\t";
			}
		}
		
		for (int i = 0; i < big5.length; i++) {
			if (big5[i] == 1) {
				summary += "\n" + description.get(names[i]);
			}
		}
		
		return summary;
	}
	
	/**
	 * @param primary - index of primary personality trait
	 * @param secondary - index of secondary personality trait
	 * @return a string as the key to dictionary
	 */
	public String buildKey(int primary, int secondary) {
		String key = "";
		key += names[primary];
		key += big5[primary];
		key += names[secondary];
		key += big5[secondary];
		
		return key;
	}
	
	/**
	 * @param key - a string as the key to dictionary
	 * @return a string as the corresponding personality trait
	 */
	public String getPhrase(String key) {
		String phrase = "";
		if (dict.containsKey(key)) {
			phrase = dict.get(key);
		}
		
		return phrase;
	}

}


