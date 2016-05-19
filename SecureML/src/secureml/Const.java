package secureml;

public class Const {
	
	/********************************************************************/
	/*							WINDOW CONSTANTS						*/
	/********************************************************************/
	
	public static final String APP_NAME = "Get To Know Your VIRTUAL Identity";
	public static final int MIN_WIDTH = 800;
	public static final int MIN_HEIGHT = 500;
	
	/********************************************************************/
	/*							   FILE PATHS   						*/
	/********************************************************************/
	
	// getURI() returns a string "file:ACTUAL_PATH_HERE"
	// To get the actual path, we don't need the "file:" part of the string.
	public static final int PATH_START_INDEX = 5;
	
	public static final String UW_ICON_PATH = "uw-icon.png";
	public static final String CROPPED_IMG_PATH = "res/croppedImage.png";
	
	public static final String APPLICATION_LAYOUT_PATH = "application.css";
	public static final String INPUT_VIEW_PATH = "view/InputView.fxml";
	public static final String PROCESSING_VIEW_PATH = "view/ProcessingView.fxml";
	public static final String RESULT_VIEW_PATH = "view/ResultView.fxml";
}
