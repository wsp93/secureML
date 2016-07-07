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
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import secureml.ResLoader;
import secureml.securesvm.PrivateSVMClient;
import secureml.svm.SVMClient;

/**
 * Controller for the ResultView.fxml Scene.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class ProcessingViewController extends Controller {
	
	/** The gif animation to indicate that the program is processing data securely. */
	private static final Image SECURE_GIF = ResLoader.getInstance().loadImage("secure-animation.gif");
	/** The gif animation to indicate that the program is processing data in the clear. */
	private static final Image IN_THE_CLEAR_GIF = ResLoader.getInstance().loadImage("in-the-clear-animation.gif");

	private String serverIP;
	private String tiIP;
	private int securePort;
	private int clearPort;
	private int tiPort;

	private String inputText;
	private Image inputImage;
	private boolean secure;
	private String outputText;
	private String gender;
	private String age;

	@FXML
	private TextArea bytesTextArea;
	@FXML
	private TextArea charsTextArea;
	@FXML
	Button continueButton;
	@FXML
	ImageView gifImageView;

	public void setInputs(String inputText, boolean secure, Image inputImage) {
		this.inputText = inputText;
		setbytesText(inputText);
		this.secure = secure;
		this.inputImage = inputImage;
		
		if (secure) {
			gifImageView.setImage(SECURE_GIF);
		} else {
			gifImageView.setImage(IN_THE_CLEAR_GIF);
		}
	}

	public void analyzeData(String ip, int port, ArrayList<Double> features, List<Integer> picFeatures) {
		// TODO incorporate the methods of analysis on inputText and inputImage here!
		SVMClient svmClient = new SVMClient(features, picFeatures);
		String result = svmClient.predict(ip, port);
		System.out.println(result);
		this.outputText = result.split(",gender")[0];
		System.out.println(outputText);
		this.gender = result.split("gender: ")[1].split(",")[0];
		System.out.println(gender);
		this.age = result.split("category: ")[1].replaceAll("]", "");
		System.out.println(age);
		continueButton.setDisable(false);
	}

	public void secureAnalyzeData(String serverIP, String tiIP, int securePort, int tiPort, double[] features, List<Integer> picFeatures) {
		// TODO incorporate the methods of analysis on inputText and inputImage here!
		outputText = "";
		//Initialize Private Client so it can fetch random data  << Caleb
		PrivateSVMClient.globalClient = new PrivateSVMClient(serverIP, tiIP, securePort, tiPort);
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean[] results = PrivateSVMClient.globalClient.runTextPrediction(features, bytesTextArea);
				outputText = results[0] + " " + results[1] + " " + results[2] + " " + 
						results[3] + " " + results[4];
				boolean[] resultsImg = PrivateSVMClient.globalClient.runImagePrediction(picFeatures, bytesTextArea);
				
				bytesTextArea.appendText("\n<<< Classification Complete >>>\n");
				age = "" + (resultsImg[1] ? (resultsImg[2] ? 2 : 1 ) : (resultsImg[3] ? 4 : 3));
				gender = (resultsImg[0] ? "male" : "female");
				continueButton.setDisable(false);
			}
		}.start();
	}

	/**
	 * Sets the text of {@link #outputTextArea}.
	 * @param text the results of the program to output to the user.
	 */
	public void setbytesText(String text) {
		bytesTextArea.setText(text);
	}

	@FXML
	private void seeTheResults() {
		// TODO put the output text in as a parameter of resultView() (or change the parameters for the method if you want to pass it something other than a string)
		mainApp.resultView(this.outputText, this.gender, this.age, inputImage);
	}

}
