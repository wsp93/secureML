/*
 * Mike Nickels
 * 
 * developed for
 * University of Washington, Tacoma
 * Privacy Preserving Maching Learning Group
 * secureml.insttech.washington.edu
 */

package secureml.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import secureml.Const;
import secureml.ResLoader;
import secureml.feature.extractor.FaceDetection;
import secureml.feature.extractor.FaceDetectionException;
import secureml.feature.extractor.LIWCExtractor;
import secureml.feature.extractor.LandmarkExtractor;
import secureml.feature.extractor.MRCextractor;
import secureml.feature.extractor.NRCExtractor;
import secureml.feature.extractor.QueryException;
import secureml.gui.view.Controller;
import secureml.gui.view.ProcessingViewController;
import secureml.gui.view.ResultViewController;
import secureml.securesvm.PrivateSVMClient;
import secureml.svm.StringUtils;

/**
 * Starting driver and main controller for the program.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class Main extends Application {
	/** The primary stage for this application. */
	public Stage primaryStage;
	
	private String serverIP = Const.SERVER_IP;
	private String tiIP = Const.TI_IP;
	private int securePort = Const.SECURE_PORT;
	private int clearPort = Const.CLEAR_PORT;
	private int tiPort = Const.TI_PORT;
	
	/**
	 * Set IP and Ports for Server and TrustedInitializer.
	 */
	public void setNetworkInfo(String serverIP, String tiIP, int securePort, int clearPort, int tiPort) {
		this.serverIP = serverIP;
		this.tiIP = tiIP;
		this.securePort = securePort;
		this.clearPort = clearPort;
		this.tiPort = tiPort;
	}

	/**
	 * Displays the InputView.fxml Scene.
	 */
	public void inputView() {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource(Const.INPUT_VIEW_PATH));
			Scene inputScene;
			if (primaryStage.getScene() == null) {
				inputScene = new Scene((Pane) loader.load());
			} else {
				inputScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			}
			inputScene.getStylesheets().add(getClass().getResource(Const.APPLICATION_LAYOUT_PATH).toExternalForm());
			primaryStage.setScene(inputScene);
			((Controller) loader.getController()).linkMainController(this);
			primaryStage.show();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Displays the OptionsView.fxml Scene.
	 */
	public void optionsView() {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource(Const.OPTIONS_VIEW_PATH));
			Scene optionsScene;
			if (primaryStage.getScene() == null) {
				optionsScene = new Scene((Pane) loader.load());
			} else {
				optionsScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			}
			optionsScene.getStylesheets().add(getClass().getResource(Const.APPLICATION_LAYOUT_PATH).toExternalForm());
			primaryStage.setScene(optionsScene);
			((Controller) loader.getController()).linkMainController(this);
			primaryStage.show();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Displays the ProcessingView.fxml Scene.
	 * Passes input text and input Image to the Controller.
	 * @param inputText text input to analyze.
	 * @param secure indicates whether to perform a secure analysis or an in the clear analysis.
	 * @param inputImage input Image to analyze.
	 * @throws QueryException 
	 * @throws FaceDetectionException 
	 */
	public void processingView(String inputText, boolean secure, Image inputImage, String imagePath) throws QueryException {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource(Const.PROCESSING_VIEW_PATH));
			final Scene processingScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			processingScene.getStylesheets().add(getClass().getResource(Const.APPLICATION_LAYOUT_PATH).toExternalForm());
			primaryStage.setScene(processingScene);
			ProcessingViewController controller = (ProcessingViewController) loader.getController();
			controller.linkMainController(this);
			System.out.println(inputText);
			//feature extractors
			LIWCExtractor liwc = new LIWCExtractor();
			MRCextractor mrc = new MRCextractor();
			NRCExtractor nrc = new NRCExtractor();

			ArrayList<Double> textFeatures = nrc.nrcOnString(inputText); 
			textFeatures.addAll(mrc.mrcOnString(inputText));
			textFeatures.addAll(liwc.extract(inputText));
			
			System.out.println(imagePath);
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			
			try
			{
				FaceDetection.cropFaces(imagePath);
			}
			catch(FaceDetectionException e)
			{
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle(e.getMessage());
				alert.setHeaderText(e.getMessage());
				alert.setContentText("Please choose a different image.");
				alert.showAndWait();
				
				inputView();
			}
			
			//136 Image features
			List<Integer> picFeatures = LandmarkExtractor.extract(Const.CROPPED_IMG_PATH);
			System.out.println(picFeatures.size());
					
			if (!secure) {
				String outputText = "Analyzing In Clear...\n";
				outputText += "Text features:\n";
				
				Scanner inputScanner = new Scanner(this.getClass().getClassLoader().getResourceAsStream("textFeatureNames"));
				List<String> textFeaturesNames = StringUtils.parseStringList(inputScanner);
				
				for (int i = 0; i < textFeatures.size(); i++) {
					outputText += textFeaturesNames.get(i) + " : " +textFeatures.get(i) + "\n";
				}
				outputText += "Image features:\n";
				for (int i = 0; i < picFeatures.size(); i++) {
					outputText += picFeatures.get(i) + "\n";
				}
				controller.setInputs(outputText, secure, inputImage);
				
				//sending the extracted features to analyze
				controller.analyzeData(serverIP, clearPort, textFeatures, picFeatures);
			} else { //Secure is greenlit << Caleb
				System.out.println(textFeatures.size());
				double[] featureArray = new double[PrivateSVMClient.numTextFeatures];
				for (int p = 0; p < PrivateSVMClient.numTextFeatures; p++)
					featureArray[p] = textFeatures.get(p);
				controller.setInputs("<<< Protocol Log >>>\n\n", secure, inputImage);
				controller.secureAnalyzeData(serverIP, tiIP, securePort, tiPort, featureArray, picFeatures);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Displays the ResultView.fxml Scene.
	 */
	public void resultView(String outputText, String gender, String age, Image img) {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource(Const.RESULT_VIEW_PATH));
			final Scene resultScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			resultScene.getStylesheets().add(getClass().getResource(Const.APPLICATION_LAYOUT_PATH).toExternalForm());
			primaryStage.setScene(resultScene);
			ResultViewController controller = (ResultViewController) loader.getController();
			controller.linkMainController(this);
			controller.setImage(img);
			controller.setAge(age);
			controller.setGender(gender);
			controller.setResults(outputText);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the Stage (GUI frame).
	 * @param primaryStage the primary stage for this application, onto which the application scene can be 
	 */
	private void initStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setTitle(Const.APP_NAME);
		primaryStage.setMinWidth(Const.MIN_WIDTH);
		primaryStage.setMinHeight(Const.MIN_HEIGHT);
		primaryStage.getIcons().add(new Image(Const.UW_ICON_PATH));
	}

	@Override
	public void start(Stage primaryStage) {
		initStage(primaryStage);
		inputView();
	}

	/**
	 * Main method, the starting point for this program.
	 * @param args command line arguments (unused in this application).
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
