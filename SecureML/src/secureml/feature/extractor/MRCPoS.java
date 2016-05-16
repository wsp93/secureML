/*
 * Created on Sep 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package secureml.feature.extractor;

import java.util.*;
/**
 * MRC part-of-speech tags.
 * 
 * @author Francois Mairesse, <a href=http://www.dcs.shef.ac.uk/~francois
 *         target=_top>http://www.dcs.shef.ac.uk/~francois</a>
 */
public enum MRCPoS implements PoS {
    
	NOUN, ADJECTIVE, ADVERB, VERB, PAST_PARTICIPLE, PREPOSITION, CONJUNCTION, PRONOUN, INTERJECTION, OTHER;
	
 
	private static Map<String,MRCPoS> map;
	
	/**
	 * Initialization procedure required using this class.
	 */
	public static void init() {
	    map = new HashMap<String,MRCPoS>(10);
	    map.put(NOUN.toString(), NOUN);
	    map.put(ADJECTIVE.toString(), ADJECTIVE);
	    map.put(ADVERB.toString(), ADVERB);
	    map.put(VERB.toString(), VERB);
	    map.put(PAST_PARTICIPLE.toString(), PAST_PARTICIPLE);
	    map.put(PREPOSITION.toString(), PREPOSITION);
	    map.put(CONJUNCTION.toString(), CONJUNCTION);
	    map.put(PRONOUN.toString(), PRONOUN);
	    map.put(INTERJECTION.toString(), INTERJECTION);
	    map.put(OTHER.toString(), OTHER);
	    map.put(" ", OTHER);
	}
	
	//public static String getPoSToREM(String pos) {
	//    return (String) map.get(pos);
	//}
	
	/**
	 * Returns the number of part-of-speech tags in the database.
	 */
	public static int posCount() {
	    return map.size();
	}
	
	public static Set<MRCPoS> getAll() {
		return new LinkedHashSet<MRCPoS>(map.values());
	}
	
	/**
	 * Returns the MRCPoS associated with the string, if it exists, else returns null.
	 * 
	 * @param pos part-of-speech string.
	 * @return
	 */
	public static MRCPoS getMRCPoS(String pos) {
		if (map.containsKey(pos)) { 
			return map.get(pos);
		} else { return null; }
	}
	
	/**
	 * Returns the string symbol of the part-of-speech.
	 * @return
	 */
	public String toString() {
		switch (this) {
		case NOUN:
			return "N";
		case ADJECTIVE:
			return "J";
		case ADVERB:
			return "A";
		case VERB:
			return "V";
		case PAST_PARTICIPLE:
			return "P";
		case PREPOSITION:
			return "R";
		case CONJUNCTION:
			return "C";
		case PRONOUN:
			return "U";
		case INTERJECTION:
			return "I";
		case OTHER:
			return "O";	
		default:
			return null;
		}
	}
	


}
