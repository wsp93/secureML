package secureml.feature.extractor;

import java.io.*;
import java.io.FileReader;
import java.util.*;

/**
 * Implementation of the MRC Psycholinguistic database, based on a LinkedHashMap.
 * 
 * @author Francois Mairesse, <a href=http://www.dcs.shef.ac.uk/~francois
 *         target=_top>http://www.dcs.shef.ac.uk/~francois</a>
 * @version 1.0
 */
public class MRCDatabase extends LexicalDatabase {
    
    /** Number of letters **/
    public static final Field FIELD_NLET = new Field("NLET");
    
    /** Number of phonemes **/
    public static final Field FIELD_NPHON = new Field("NPHON");
    
    /** Number of syllables **/
    public static final Field FIELD_NSYL = new Field("NSYL");
    
    /** K F frequency **/
    public static final Field FIELD_K_F_FREQ = new Field("K_F_FREQ");
    
    /** K F number of categories **/ 
    public static final Field FIELD_K_F_NCATS = new Field("K_F_NCATS");
    
    /** K F number of samples **/
    public static final Field FIELD_K_F_NSAMP = new Field("K_F_NSAMP");
    
    /** T L frequency **/
    public static final Field FIELD_T_L_FREQ = new Field("T_L_FREQ");
    
    /** Brown frequency **/
    public static final Field FIELD_BROWN_FREQ = new Field("BROWN_FREQ");
    
    /** Familiarity **/
    public static final Field FIELD_FAM = new Field("FAM");
    
    /** Concreteness **/
    public static final Field FIELD_CONC = new Field("CONC");
    
    /** Imagability **/
    public static final Field FIELD_IMAG = new Field("IMAG");
    
    /** Meaningfulness (Colorado norms)**/
    public static final Field FIELD_MEANC = new Field("MEANC");
    
    /** Meaningfulness (Paivo norms)**/
    public static final Field FIELD_MEANP = new Field("MEANP");
    
    /** Age of acquisition **/
    public static final Field FIELD_AOA = new Field("AOA");
    
    
    private Field[] fieldsArray = {FIELD_NLET, FIELD_NPHON, FIELD_NSYL, FIELD_K_F_FREQ, 
            FIELD_K_F_NCATS, FIELD_K_F_NSAMP, FIELD_T_L_FREQ, FIELD_BROWN_FREQ, FIELD_FAM,
            FIELD_CONC, FIELD_IMAG, FIELD_MEANC, FIELD_MEANP, FIELD_AOA};
    
    private Field[] fieldsWithNoNullValue = {FIELD_NLET, FIELD_NPHON, FIELD_NSYL, FIELD_FAM, FIELD_CONC, 
            FIELD_IMAG, FIELD_MEANC, FIELD_MEANP, FIELD_AOA};
            
         
    private Set<Field> fieldsWithNoNullValueSet;

    /**
     * Loads MRC Database with default number of entries.
     * 
     * @param dbFile file <code>mrc2.dct</code> from the MRC Psycholinguistic Database directory.
     * @throws IOException
     */
    public MRCDatabase(File dbFile) throws IOException {
        this(dbFile, 120038);
    }
    
    /**
     * Loads the MRC database into memory.
     * 
     * @param dbFile file <code>mrc2.dct</code> from the MRC Psycholinguistic Database directory.
     * @param size number of entries in the file.
     * @throws IOException
     */
    public MRCDatabase(File dbFile, int size) throws IOException {
        
        super(size);
        
        System.err.println("Loading MRC Psycholinguistic Database...");
        fields.addAll(Arrays.asList(fieldsArray));
        // create idFieldsMap
        for (Field f : fields) {
            idFieldsMap.put(f.toString(), f);
        }
        fieldsWithNoNullValueSet = new HashSet<Field>(Arrays.asList(fieldsWithNoNullValue));
        
        MRCPoS.init();
        
        BufferedReader reader = new BufferedReader(new FileReader(dbFile));
		String line;

		while ((line = reader.readLine()) != null) {

			// get stem efficiently
			String word = "";
			String wordSplit = line.substring(51);
			for (int i = 0; i < wordSplit.length(); i++) {
				if (wordSplit.charAt(i) == '|') {
					word = wordSplit.substring(0, i).toLowerCase();
					break;
				}
			}
			
			Map<PoS,Map<Field,Integer>> homonyms;
			if (this.getMap().containsKey(word)) {
				homonyms = this.getMap().get(word);
			} else {
				homonyms = new LinkedHashMap<PoS,Map<Field,Integer>>();
			}
			String pos = line.substring(44, 45);
			
			// homonyms.put(MRCPoS.getPoS(pos), getWordMRCCounts(line));
			if (MRCPoS.getMRCPoS(pos) != null) {
				homonyms.put(MRCPoS.getMRCPoS(pos), getWordMRCCounts(line));
			} else { System.err.println("Error: wrong PoS tag " + pos + ", skipping line " + line); }
	
			// add line to database
			this.getMap().put(word, homonyms);
		}		
		System.err.println(this.getMap().size() + " words loaded.");
    }
    
    /**
     * Parses a line from the database file, and returns Map of Integer values.
     * 
     * @param line
     * @return
     */
	private Map<Field,Integer> getWordMRCCounts(String line) {
					
				// extract features from entry
				String[] split = line.split("\\|");
				
					Map<Field, Integer> values = new HashMap<Field, Integer>(fieldsArray.length);
					values.put(FIELD_NLET, new Integer(split[0].substring(0, 2)));
					values.put(FIELD_NPHON, new Integer(split[0].substring(2, 4)));
					values.put(FIELD_NSYL, new Integer(split[0].substring(4, 5)));
					values.put(FIELD_K_F_FREQ, new Integer(split[0].substring(5, 10)));
					values.put(FIELD_K_F_NCATS, new Integer(split[0].substring(10, 12)));
					values.put(FIELD_K_F_NSAMP, new Integer(split[0].substring(12, 15)));
					values.put(FIELD_T_L_FREQ, new Integer(split[0].substring(15, 21)));
					values.put(FIELD_BROWN_FREQ, new Integer(split[0].substring(21, 25)));
					values.put(FIELD_FAM, new Integer(split[0].substring(25, 28)));
					values.put(FIELD_CONC, new Integer(split[0].substring(28, 31)));
					values.put(FIELD_IMAG, new Integer(split[0].substring(31, 34)));
					values.put(FIELD_MEANC, new Integer(split[0].substring(34, 37)));
					values.put(FIELD_MEANP, new Integer(split[0].substring(37, 40)));
					values.put(FIELD_AOA, new Integer(split[0].substring(40, 43)));
					
					
					return values;
			
		
	}
	
	
	/**
     * Returns an integer feature value from the database.
     * 
     * @param word lemma of the word to look for.
     * @param pos part-of-Speech of the word.
     * @param field field of the database providing the value.
     * @throws QueryException if the word/PoS/Field isn't found, 
     * or if the value is undefined (e.g. 0 value for some fields).
     * @return value.
     */
    public int getValue(String word, MRCPoS pos, Field field)
            throws QueryException {
        try {
            int value = this.getMap().get(word.toLowerCase()).get(pos).get(field).intValue();
            // take care of features in which zero isn't a correct value
            if (value == 0 && fieldsWithNoNullValueSet.contains(field)) {
                throw new UndefinedValueException();
            } 
            return value;
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }

    
	/**
     * Returns an integer feature value from the database.
     * 
     * @param word lemma of the word to look for.
     * @param pos part-of-speech of the word.
     * @param field String representing the field of the database providing the value.
     * @throws QueryException if the word/PoS/Field isn't found, 
     * or if the value is undefined (e.g. 0 value for some fields).
     * @return value.
     */
    public int getValue(String word, MRCPoS pos, String field)
            throws QueryException {
    	return getValue(word, pos, idFieldsMap.get(field));
    }
    
    
	/**
     * Returns an integer feature value from the database.
     * 
     * @param word Lemma of the word to look for.
     * @param pos String representation of the Part-of-Speech of the word.
     * @param field Field of the database providing the value.
     * @throws QueryException if the word/PoS/Field isn't found, 
     * or if the value is undefined (e.g. 0 value for some fields).
     * @return value.
     */
    public int getValue(String word, String pos, Field field)
            throws QueryException {
        try {
            int value = this.getMap().get(word.toLowerCase()).get(pos).get(field).intValue();
            // take care of features in which zero isn't a correct value
            if (value == 0 && fieldsWithNoNullValueSet.contains(field)) {
                throw new UndefinedValueException();
            } 
            return value;
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }
    
    
    /**
     * Returns all the fields of the MRC database.
     * 
     */
    public List<Field> getFields() {
        return fields;
    }

}
