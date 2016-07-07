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
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import secureml.ResLoader;
import secureml.feature.extractor.QueryException;
import secureml.gui.Main;

/**
 * Controller for the InputView.fxml Scene.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class InputViewController extends Controller {
	
	/** The Image to indicate to the user to choose an image for the program input. */
	private static final Image PROMPT_IMAGE = ResLoader.getInstance().loadImage("prompt-icon.png");
	
	/** 
	 * Flag to indicate whether the user has entered an Image or not.
	 * If false, {@link #getImageInput()} will return null.
	 */
	private boolean imageChosen = false;
	
	/** The TextArea that the user enters their text input into. */
	@FXML
	private TextArea inputTextArea;
	/** The ImageView that the user's image input will be displayed in. */
	@FXML
	private ImageView inputImageView;
	private String imageFilePath;
	
	@FXML
	private Button clearButton;
	@FXML
	private Button secureButton;
	
	@Override
	public void linkMainController(Main mainApp) {
		super.linkMainController(mainApp);
//		clearButton.disableProperty().bind(
//				Bindings.isEmpty(inputTextArea.textProperty()));
//		secureButton.disableProperty().bind(
//				Bindings.isEmpty(inputTextArea.textProperty()));
		inputImageView.setImage(PROMPT_IMAGE);
	}
	
	/**
	 * This method is called when the user presses the "Analyze in the Clear" button.
	 * @throws QueryException 
	 */
	@FXML
	private void analyzeInTheClearButtonPressed() throws QueryException {
		mainApp.processingView(getTextInput(), false, getImageInput(), imageFilePath);
	}
	
	/**
	 * This method is called when the user presses the "Analyze Privately" button.
	 * @throws QueryException 
	 */
	@FXML
	private void analyzePrivatelyButtonPressed() throws QueryException {
		//should be two different from the analyze in the clear 
		mainApp.processingView(getTextInput(), true, getImageInput(), imageFilePath);
	}
	
	/**
	 * This method is called when the user clicks on {@link #inputImageView}.
	 * Asks the user to choose an input file using a FileChooser dialog.
	 * Will not change the selected image if the dialog returns null.
	 */
	@FXML
	private void chooseImage() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Choose a picture!");
		chooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg"));
		File selection = chooser.showOpenDialog(mainApp.primaryStage);
		if (selection != null) {
			URI imgUri = selection.toURI();
			
			inputImageView.setImage(new Image(imgUri.toString()));
			imageFilePath = imgUri.getPath();
			imageChosen = true;
		}
	}
	
	/**
	 * Clears all input fields in the Scene.
	 * (Calls clear() on {@link #inputTextArea},
	 * calls setImage({@link #PROMPT_IMAGE}) on {@link #inputImageView},
	 * and sets {@link #imageChosen} to false.)
	 */
	@FXML
	private void clearEntries() {
		inputTextArea.clear();
		inputImageView.setImage(PROMPT_IMAGE);
		imageChosen = false;
	}
	
	/**
	 * This method is called when the user presses the "Options" button.
	 */
	@FXML
	private void optionsButtonPressed() {
		mainApp.optionsView();
	}
	
	/**
	 * Gets the text input entered by the user.
	 * @return the return String of {@link #inputTextArea}.getText().
	 * 		   Will be an empty String if the user has not input any text.
	 */
	public String getTextInput() {
		return inputTextArea.getText();
	}
	
	/**
	 * Gets the Image selected by the user.
	 * @return the return Image of {@link #inputImageView}.getImage(),
	 * or null if {@link #imageChosen} is false.
	 */
	public Image getImageInput() {
		if (!imageChosen) return null;
		return inputImageView.getImage();
	}

}
