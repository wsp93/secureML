package secureml;

public class Const {
	/********************************************************************/
	/*							IP/PORT DEFAULTS   						*/
	/********************************************************************/
	public static final String SERVER_IP = "secureml.insttech.washington.edu";
	public static final String TI_IP = "secureml.insttech.washington.edu";
	
	public static final int SECURE_PORT = 1432;
	public static final int CLEAR_PORT = 6666;
	public static final int TI_PORT = 1235;
	
	/********************************************************************/
	/*							WINDOW CONSTANTS						*/
	/********************************************************************/
	
	public static final String APP_NAME = "Get To Know Your VIRTUAL Identity";
	public static final int MIN_WIDTH = 800;
	public static final int MIN_HEIGHT = 500;
	
	/********************************************************************/
	/*							   FILE PATHS   						*/
	/********************************************************************/
	
	public static final String UW_ICON_PATH = "uw-icon.png";
	public static final String CROPPED_IMG_PATH = "res/croppedImage.png";
	
	public static final String APPLICATION_LAYOUT_PATH = "application.css";
	public static final String INPUT_VIEW_PATH = "view/InputView.fxml";
	public static final String OPTIONS_VIEW_PATH = "view/OptionsView.fxml";
	public static final String PROCESSING_VIEW_PATH = "view/ProcessingView.fxml";
	public static final String RESULT_VIEW_PATH = "view/ResultView.fxml";
	
	public static final String MRC_PATH = "mrc2.dct";
	public static final String NRC_PATH = "NRCDic.txt";
	
	public static final String LANDMARK_COMMAND = "python res/face_landmark_detection.py";
	public static final String LANDMARK_DATA = "res/shape_predictor_68_face_landmarks.dat";
	public static final String FACE_DETECTION_PATH = "res/lbpcascade_frontalface.xml";
}
