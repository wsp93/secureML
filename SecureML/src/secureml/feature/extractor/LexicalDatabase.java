/*
 * Created on Sep 22, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package secureml.feature.extractor;

import java.util.*;
/**
 * Implementation fo a general lexical database, associating a word, 
 * part-of-speech and field to an integer value. Based on a LinkedHashMap.
 * 
 * @author Francois Mairesse, <a href=http://www.dcs.shef.ac.uk/~francois
 *         target=_top>http://www.dcs.shef.ac.uk/~francois</a>
 */
public class LexicalDatabase {
    
    
    protected List<Field> fields;
    protected Map<String,Field> idFieldsMap;
    protected Map<String, Map<PoS, Map<Field, Integer>>> map;
    
    /**
     * Construct a new lexical database.
     *
     */
    public LexicalDatabase() {
        map = new LinkedHashMap<String, Map<PoS, Map<Field, Integer>>>();
        fields = new ArrayList<Field>();
        idFieldsMap = new LinkedHashMap<String,Field>(fields.size());
    }
    
    /**
     * Construct a new lexical database, allocating the specified number of entries.
     *
     */    
    public LexicalDatabase(int size) {
    	map = new LinkedHashMap<String, Map<PoS, Map<Field, Integer>>>(size);
        fields = new ArrayList<Field>();
        idFieldsMap = new LinkedHashMap<String,Field>(fields.size());
       
    }
    
    /**
     * Returns true if database contains the field.
     * 
     * @param field String value of a database field.
     * @return
     */
    public boolean containsField(String field) {
        return idFieldsMap.containsKey(field);
    }
    
    /**
     * Returns true if database contains the field.
     * 
     * @param field database field.
     * @return
     */
    public boolean containsField(Field field) {
        return fields.contains(field);
    }
    
    
    /**
     * Returns true if the word (lower-case), part-of-speech and field are associated with a value in the database.
     * @param word word to look-up.
     * @param pos part-of-speech.
     * @param field field to look-up.
     * @return
     */
    public boolean containsEntry(String word, PoS pos, String field) {
        word = word.toLowerCase();
        if (map.containsKey(word) && map.get(word).containsKey(pos) 
                && map.get(word).get(pos).containsKey(idFieldsMap.get(field)))  {
            return true; 
        } else { return false; }
    }
    
    
    /**
     * Returns true if the word is associated with at least one entry in 
     * the database (case-insensitive).
     * @param word word to look-up.
     * @return
     */
    public boolean containsWord(String word) {
    	return map.containsKey(word.toLowerCase());
    }
    
    
    /**
     * Returns the set of all available part-of-speech tags for a specific word (homonyms).
     * @param word word to look-up.
     * @return
     */
    public Set<PoS> getAvailablePoS(String word) throws QueryException {
    	try {
    		return new LinkedHashSet<PoS>(map.get(word.toLowerCase()).keySet());
    	} catch (NullPointerException e) {
    		throw new EntryNotFoundException();
    	}
    }
    
    
    /**
     * Returns the set of all available fields for a specific word and part-of-speech.
     * @param word word to look-up.
     * @param pos part-of-speech to look up.
     * @return
     */
    public Set<Field> getAvailableFields(String word, PoS pos) throws QueryException {
    	try {
    		return new LinkedHashSet<Field>(map.get(word.toLowerCase()).get(pos).keySet());
    	} catch (NullPointerException e) {
			throw new EntryNotFoundException();
		}
    }
    
    /**
     * Returns a string comparator for based on the database values of a given field and for a given PoS tag.
     * All values must be defined or the ordering won't be total, therefore not garranteed.
     * 
     * @param field database field.
     * @param pos part-of-speech.
     * @return
     */
    public Comparator getLexiconComparator(final Field field, final PoS pos) {
    	
    	return new Comparator<String>() {
    			public int compare(String a , String b) {
    				try {
    					return (int) Math.signum(getValue(a, pos, field) - getValue(b, pos, field));
    				} catch (QueryException e) { return 0; }
    			}
    	};
    }
    
    
    /**
     * Returns all words in the database with values within the [min,max] 
     * interval specified (linear search).
     * @param field database field.
     * @param pos part-of-speech.
     * @param min minimum value (included).
     * @param max maximum value (included).
     * @return
     */
    public List<String> getWords(Field field, PoS pos, double min, double max) {
    	List<String> words = new ArrayList<String>();
    	for(String word : map.keySet()) {
    		try {
    			double value = getValue(word, pos, field);
    			if (value >= min && value <= max) { words.add((String) word); }
    		} catch (QueryException e) { System.err.println(e.getMessage()); }
    	}
    	return words;
    }

    /**
     * Returns all words in the database with the exact specified integer value 
     * (linear search).
     * @param field database field.
     * @param pos part-of-speech.
     * @param inputValue input value to match.
     * @return
     */
    public List<String> getWords(Field field, PoS pos, int inputValue) {
    	List<String> words = new ArrayList<String>();
    	System.err.println(map.size() + " word in lexical database");
    	for(String word : map.keySet()) {
    		try {
    			
    			int value = getValue(word, pos, field);
    			System.err.println("Looking at entry " + word + " " + pos + " value " + value);
    			if (inputValue == value) { words.add(word); }
    		} catch (QueryException e) { System.err.println(e.getMessage()); }
    	}
    	return words;
    }
    
    
    /**
     * Returns an integer feature value from the database.
     * 
     * @param word word to look-up.
     * @param pos part-of-speech to look up.
     * @param field field of the database.
     * @return
     */
    public int getValue(String word, PoS pos, String field)
            throws QueryException {
        try {
            return getValue(word, pos, idFieldsMap.get(field));
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }
    
    /**
     * Returns an integer feature value from the database for any random PoS of
     * the input word.
     * 
     * @param word
     * @param field
     * @return
     */
    public int getValue(String word, String field) throws QueryException {
        try {
            return getValue(word, idFieldsMap.get(field));
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }
    
    public List<Field> getFields() { return fields; }
    
 
    /**
     * Returns an integer feature value from the database.
     * 
     * @param word word to look-up.
     * @param pos part-of-speech to look up.
     * @param field field of the database.
     * @return
     */
    public int getValue(String word, PoS pos, Field field)
            throws QueryException {
        try {
            return map.get(word.toLowerCase()).get(pos).get(field).intValue();
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }
    
    
    /**
     * Returns an integer feature value from the database for any random PoS of the input word.
     * 
     * @param word
     * @param pos
     * @param field
     * @return
     */
    public int getValue(String word, Field field)
            throws QueryException {
        try {
            return map.get(word.toLowerCase()).values().iterator().next().get(field).intValue();
        } catch (NullPointerException e) {
            throw new EntryNotFoundException();
        }
    }
    
    /**
     * Insert a value in the lexical database.
     * 
     * @param word word to insert value for.
     * @param pos part-of-speech of the word.
     * @param field Type of value.
     * @param value Integer value to be added.
     */
    public void putValue(String word, PoS pos, Field field, int value) {
        
        Map<PoS, Map<Field, Integer>> posFieldMap = null;
        if (!map.containsKey(word)) { 
        	posFieldMap = new LinkedHashMap<PoS, Map<Field, Integer>>(1); 
        	map.put(word, posFieldMap); }
        else { posFieldMap = map.get(word); }
        
        Map<Field, Integer> fieldValueMap = null;
        if (!posFieldMap.containsKey(pos)) { 
        	fieldValueMap = new LinkedHashMap<Field, Integer>(1); 
        	posFieldMap.put(pos, fieldValueMap); 
        	}
        else { fieldValueMap = posFieldMap.get(pos); }
        
        fieldValueMap.put(field, new Integer(value));
        if (!fields.contains(field)) { 
            fields.add(field);
            idFieldsMap.put(field.toString(), field); 
  
        }
           
    }
    
    /**
     * Returns the underlying mapping of the database.
     * 
     * @return
     */
    public Map<String, Map<PoS, Map<Field, Integer>>> getMap() {
    	return map;
    }
    
    /**
     * Copies entries from the argument into the database, 
     * without overwriting existing entries.
     * 
     * @param db database to copy entries from.
     */
    public void addEntriesFrom(LexicalDatabase db) {
        for (String word: db.getMap().keySet()) {
           
            if (map.containsKey(word)) { 
                Map<PoS, Map<Field, Integer>> posMap = map.get(word); 
                Map<PoS, Map<Field, Integer>> dbPosMap =  db.getMap().get(word);
                for (PoS pos : dbPosMap.keySet()) {
                      
                    if (posMap.containsKey(pos)) { 
                    	Map<Field, Integer> fieldMap = posMap.get(pos); 
                    	Map<Field, Integer> dbFieldMap = dbPosMap.get(pos);
                        for (Field field: dbFieldMap.keySet()) {
                          
                            // don't overwrite
                            if (!fieldMap.containsKey(field)) {
                                fieldMap.put(field, dbFieldMap.get(field));
                            } 
                        }                                                 
                    } else {
                        // if PoS isnt here, add it
                        posMap.put(pos, dbPosMap.get(pos));
                    }
                }           
            }
            else { 
                // the word isnt here, add it
                map.put(word, db.getMap().get(word));
            }         		
        }
        
        for (Field f: db.getFields()) {
            if (!fields.contains(f)) {
                idFieldsMap.put(f.toString(), f); 
            }
        }
    }   
}
