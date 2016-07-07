/*
 * Mike Nickels
 * 
 * developed for
 * University of Washington, Tacoma
 * Privacy Preserving Maching Learning Group
 * secureml.insttech.washington.edu
 */

package secureml.gui.view;

import java.io.File;
import java.net.URI;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import secureml.ResLoader;
import secureml.feature.extractor.QueryException;
import secureml.gui.Main;
import secureml.Const;

/**
 * Controller for the InputView.fxml Scene.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class OptionsViewController extends Controller {
	@FXML
	private TextField serverIP;
	@FXML
	private TextField tiIP;
	@FXML
	private TextField securePort;
	@FXML
	private TextField clearPort;
	@FXML
	private TextField tiPort;
	
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	@Override
	public void linkMainController(Main mainApp) {
		setDefaults();
		super.linkMainController(mainApp);
	}
	
	/**
	 * This method is called when the user presses the "OK" button.
	 */
	@FXML
	private void okButtonPressed() {
		mainApp.setNetworkInfo(serverIP.getText(), tiIP.getText(), Integer.parseInt(securePort.getText()),
								Integer.parseInt(clearPort.getText()), Integer.parseInt(tiPort.getText()));
		mainApp.inputView();
	}
	
	/**
	 * This method is called when the user presses the "Cancel" button.
	 */
	@FXML
	private void cancelButtonPressed() {
		setDefaults();
		mainApp.inputView();
	}
	
	/**
	 * Set all options to the default settings.
	 */
	@FXML
	private void setDefaults() {
		serverIP.setText(Const.SERVER_IP);
		securePort.setText(Integer.toString(Const.SECURE_PORT));
		clearPort.setText(Integer.toString(Const.CLEAR_PORT));
		
		tiIP.setText(Const.TI_IP);
		tiPort.setText(Integer.toString(Const.TI_PORT));
	}
}
