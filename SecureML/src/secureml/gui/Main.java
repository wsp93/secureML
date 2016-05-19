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
import secureml.Constants;
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

	/**
	 * Displays the InputView.fxml Scene.
	 */
	public void inputView() {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("view/InputView.fxml"));
			Scene inputScene;
			if (primaryStage.getScene() == null) {
				inputScene = new Scene((Pane) loader.load());
			} else {
				inputScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			}
			inputScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(inputScene);
			((Controller) loader.getController()).linkMainController(this);
			primaryStage.show();
			//Initialize Private Client so it can fetch random data  << Caleb
			PrivateSVMClient.globalClient = new PrivateSVMClient(new String[]{});
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
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ProcessingView.fxml"));
			final Scene processingScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			processingScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
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
			
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
			System.out.println(imagePath);
			try
			{
				FaceDetection.cropFaces(imagePath.substring(Constants.PATH_START_INDEX), "res/");
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
			List<Integer> picFeatures = LandmarkExtractor.extract("res/croppedImage.png");
			System.out.println(picFeatures.size());
					
			if (!secure) {
				String outputText = "Analyzing In Clear...\n";
				outputText += "Text features:\n";
				
				Scanner inputScanner = new Scanner(ResLoader.getInstance().loadFile("textFeatureNames"));
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
				controller.analyzeData(textFeatures, picFeatures);
			} else { //Secure is greenlit << Caleb
				System.out.println(textFeatures.size());
				double[] featureArray = new double[PrivateSVMClient.numTextFeatures];
				for (int p = 0; p < PrivateSVMClient.numTextFeatures; p++)
					featureArray[p] = textFeatures.get(p);
				controller.setInputs("<<< Protocol Log >>>\n\n", secure, inputImage);
				controller.secureAnalyzeData(featureArray, picFeatures);
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
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("view/ResultView.fxml"));
			final Scene resultScene = new Scene((Pane) loader.load(), primaryStage.getScene().getWidth(), primaryStage.getScene().getHeight());
			resultScene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
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
		primaryStage.setTitle(Constants.APP_NAME);
		primaryStage.setMinWidth(Constants.MIN_WIDTH);
		primaryStage.setMinHeight(Constants.MIN_HEIGHT);
		primaryStage.getIcons().add(new Image("uw-icon.png"));
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
