/*
 * Created on Sep 21, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package secureml.feature.extractor;
/**
 * Implements a field of the database.
 * 
 * @author Francois Mairesse, <a href=http://www.dcs.shef.ac.uk/~francois
 *         target=_top>http://www.dcs.shef.ac.uk/~francois</a>
 */
public class Field {

    
	
    private String id;
    
    public Field(String id) {
        this.id = id;
    }
    
    public String toString() {
        return id;
    }
    
}
