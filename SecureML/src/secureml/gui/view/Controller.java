/*
 * Mike Nickels
 * 
 * developed for
 * University of Washington, Tacoma
 * Privacy Preserving Maching Learning Group
 * secureml.insttech.washington.edu
 */

package secureml.gui.view;

import secureml.gui.Main;

/**
 * Controllers are able to be linked to the Main object of the application.
 * They are subclasses of Controller, and are used to give functionality to
 * and interact with the program's Scenes that are generated from the fxml views.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public abstract class Controller {
	
	/** Reference to the Main of this application for Controller implementations. */
	protected Main mainApp;
	
	/**
	 * Link the Main object to this Controller so Scenes can be switched, along with other functionality.
	 * Should always be called upon loading of a new scene and its controller using an FXMLLoader.
	 * @param m the Main object that references the Main controller of the application.
	 */
	public void linkMainController(Main m) {
		mainApp = m;
	}

}
