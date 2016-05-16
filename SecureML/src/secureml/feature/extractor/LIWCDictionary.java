package secureml.feature.extractor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Interface to the LIWC dictionary, implementing patterns for each LIWC category
 * based on the LIWC.CAT file.
 */
public class LIWCDictionary {

	/** Mapping associating LIWC features to regular expression patterns. */
	private Map<String,Pattern> map;
	
	public LIWCDictionary() {
		this.map = new LinkedHashMap<String,Pattern>();
	}
	
	
	/**
	 * Returns a map associating each LIWC categories to the number of
	 * their occurences in the input text. The counts are computed matching
	 * patterns loaded. It doesn't produce punctuation counts.
	 * 
	 * @param text input text.
	 * @param absoluteCounts includes counts that aren't relative to the total word 
	 * count (e.g. actual word count).
	 * @return hashtable associating each LIWC category with the percentage of
	 *         words in the text belonging to it.
	 */
	public Map<String,Double> getCounts(String text, boolean absoluteCounts) {

		Map<String,Double> counts = new LinkedHashMap<String, Double>(map.size());
		String[] words = tokenize(text);
		String[] sentences = splitSentences(text);
		/*System.err.println("Input text splitted into " + words.length
				+ " words and " + sentences.length + " sentences");
		*/
		// word count (NOT A PROPER FEATURE)
		if (absoluteCounts) { counts.put("WC", new Double(words.length)); }
		counts.put("WPS", new Double(1.0 * words.length / sentences.length));
		
		// type token ratio, words with more than 6 letters, abbreviations,
		// emoticons, numbers
		int sixletters = 0;
		int numbers = 0;
		for (int i = 0; i < words.length; i++) {
			String word = words[i].toLowerCase();
			if (word.length() > 6) {
				sixletters++;
			}

			if (word.matches("-?[,\\d+]*\\.?\\d+")) {
				numbers++;
			}
		}
		
		Set<String> types = new LinkedHashSet<String>(Arrays.asList(words));
		counts.put("UNIQUE", new Double(100.0 * types.size() / words.length));
		counts.put("SIXLTR", new Double(100.0 * sixletters / words.length));
		// abbreviations
		int abbrev = countMatches("\\w\\.(\\w\\.)+", text);
		counts.put("ABBREVIATIONS", new Double(100.0 * abbrev / words.length));
		// emoticons
		int emoticons = countMatches("[:;8%]-[\\)\\(\\@\\[\\]\\|]+", text);
		counts.put("EMOTICONS", new Double(100.0 * emoticons / words.length));
		// text ending with a question mark
		Double qmarks = 0.0;
		char[] chars = text.toCharArray();
 		for(int i = 1; i < chars.length; i++) {
			if(chars[i] == '?' && chars[i - 1] != '?') {
				qmarks++;
			}
		}
		counts.put("QMARKS", new Double(100.0 * qmarks / sentences.length));
//		int qmarks = countMatches("\\w\\s*\\?", text);
//		counts.put("QMARKS", new Double(100.0 * qmarks / sentences.length));		
		// punctuation
		int period = countMatches("\\.", text);
		counts.put("PERIOD", new Double(100.0 * period / words.length));
		int comma = countMatches(",", text);
		counts.put("COMMA", new Double(100.0 * comma / words.length));
		int colon = countMatches(":", text);
		counts.put("COLON", new Double(100.0 * colon / words.length));
		int semicolon = countMatches(";", text);
		counts.put("SEMIC", new Double(100.0 * semicolon / words.length));
		int qmark = countMatches("\\?", text);
		counts.put("QMARK", new Double(100.0 * qmark / words.length));
		int exclam = countMatches("!", text);
		counts.put("EXCLAM", new Double(100.0 * exclam / words.length));
		int dash = countMatches("-", text);
		counts.put("DASH", new Double(100.0 * dash / words.length));
		int quote = countMatches("\"", text);
		counts.put("QUOTE", new Double(100.0 * quote / words.length));
		int apostr = countMatches("'", text);
		counts.put("APOSTRO", new Double(100.0 * apostr / words.length));
		int parent = countMatches("[\\(\\[{]", text);
		counts.put("PARENTH", new Double(100.0 * parent / words.length));
		int otherp = countMatches("[^\\w\\d\\s\\.:;\\?!\"'\\(\\{\\[,-]",
				text);
		counts.put("OTHERP", new Double(100.0 * otherp / words.length));
		int allp = period + comma + colon + semicolon + qmark + exclam + dash
				+ quote + apostr + parent + otherp;
		counts.put("ALLPCT", new Double(100.0 * allp / words.length));
		return counts;
	}
	
	private int countMatches(String match, String text) {
		int count = text.length() - text.replaceAll(match, "").length();
		return count;
	}
	/**
	 * Splits a text into words separated by non-word characters.
	 * 
	 * @param text text to tokenize.
	 * @return an array of words.
	 */
	public static String[] tokenize(String text) {
		
		String words_only = text.replaceAll("\\W+\\s*", " ").replaceAll(
				"\\s+$", "").replaceAll("^\\s+", "");
		String[] words = words_only.split("\\s+");
		return words;
	}
		
	/**
	 * Splits a text into sentences separated by a dot, exclamation point or question mark.
	 * 
	 * @param text text to tokenize.
	 * @return an array of sentences.
	 */
	public static String[] splitSentences(String text) {
	
		return text.split("\\s*[\\.!\\?]+\\s+");
	}
}