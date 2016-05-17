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
	public void processingView(String inputText, boolean secure, Image inputImage) throws QueryException {
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
			LandmarkExtractor lm = new LandmarkExtractor();

			ArrayList<Double> textFeatures = nrc.nrcOnString(inputText); 
			textFeatures.addAll(mrc.mrcOnString(inputText));
			textFeatures.addAll(liwc.extract(inputText));
			
			//136 Image features
			ArrayList<Integer> picFeatures = new ArrayList<>(Arrays.asList(273,389,272,441,278,493,289,547,301,600,324,649,357,691,402,723,458,733,518,725,576,698,626,663,662,619,685,566,696,510,702,454,705,397,289,337,312,317,346,313,380,324,412,341,486,341,523,321,564,309,604,315,637,334,448,382,446,409,444,438,441,468,414,506,429,511,447,516,466,511,485,505,328,393,349,384,375,384,399,393,374,405,348,406,510,393,532,382,560,383,586,390,562,403,535,404,375,599,404,584,429,573,446,578,463,573,495,584,535,598,497,615,468,622,449,624,430,623,405,617,389,598,430,592,447,594,465,592,517,598,465,594,447,596,430,595));

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
