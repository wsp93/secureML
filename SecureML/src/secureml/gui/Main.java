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
import java.util.Arrays;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import secureml.feature.extractor.LIWCExtractor;
import secureml.feature.extractor.LandmarkExtractor;
import secureml.feature.extractor.MRCextractor;
import secureml.feature.extractor.NRCExtractor;
import secureml.feature.extractor.QueryException;
import secureml.gui.view.Controller;
import secureml.gui.view.ProcessingViewController;
import secureml.gui.view.ResultViewController;
import secureml.securesvm.PrivateSVMClient;

/**
 * Starting driver and main controller for the program.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class Main extends Application {

	/** The name of the application, displayed in the top bar of the GUI frame. */
	private static final String APP_NAME = "Get To Know Your VIRTUAL Identity";
	private static final int MIN_WIDTH = 800;
	private static final int MIN_HEIGHT = 500;

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
			
			//136 Image features
			List<Integer> picFeatures = LandmarkExtractor.extract(imagePath);
					
			if (!secure) {
				String outputText = "Analyzing In Clear...\n";
				outputText += "Message sending:\n";
				for (int i = 0; i < textFeatures.size(); i++) {
					outputText += textFeatures.get(i) + "\n";
				}
				controller.setInputs(outputText, secure, inputImage);
				
				//sending the extracted features to analyze
				controller.analyzeData(textFeatures, picFeatures);
			} else { //Secure is greenlit << Caleb
				System.out.println(textFeatures.size());
				double[] featureArray = new double[PrivateSVMClient.numFeatures];
				for (int p = 0; p < PrivateSVMClient.numFeatures; p++)
					featureArray[p] = textFeatures.get(p);
				controller.setInputs("<<< Protocol Log >>>\n\n", secure, inputImage);
				controller.secureAnalyzeData(featureArray);
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
		primaryStage.setTitle(APP_NAME);
		primaryStage.setMinWidth(MIN_WIDTH);
		primaryStage.setMinHeight(MIN_HEIGHT);
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
