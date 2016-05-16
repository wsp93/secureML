/*
 * Mike Nickels
 * 
 * developed for
 * University of Washington, Tacoma
 * Privacy Preserving Maching Learning Group
 * secureml.insttech.washington.edu
 */

package secureml.gui.view;

import java.util.Scanner;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import secureml.gui.text.TextSummary;

/**
 * Controller for the ResultView.fxml Scene.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public class ResultViewController extends Controller {
	
	/** Display Label for the program's output text. */
	@FXML
	TextArea outputTextArea;
	@FXML
	ImageView outputImageView;
	@FXML
	Label ageLabel;
	@FXML
	Label genderLabel;
	
	public void setImage(Image img) {
		outputImageView.setImage(img);
	}
	
	public void setAge(String age) {
		ageLabel.setText(age);
	}
	
	public void setGender(String gender) {
		genderLabel.setText(gender);
	}
	
	/**
	 * Sets the text of {@link #outputTextArea}.
	 * @param results a string containing ones and zeros representing booleans for characteristics based on the big 5 model.
	 */
	public void setResults(String results) {
		System.out.println(results);
		boolean[] interpretedResults = new boolean[5];
		Scanner s = new Scanner(results);
		for (int i = 0; i < interpretedResults.length; i++) {
			try {
				interpretedResults[i] = extractBool(s.next());
			} catch (IllegalArgumentException e) {
				// no boolean in passed string
				i--;
			}
		}
		s.close();
		try {
			outputTextArea.setText(new TextSummary(interpretedResults).generateSummary());
		} catch (Exception e) {
			System.out.println("Something went wrong in TextSummary (or while interpreting the result string).");
			e.printStackTrace();
			outputTextArea.setText(results);
		}
	}
	
	private boolean extractBool(String input) throws IllegalArgumentException {
		if (input.contains("true")) {
			return true;
		}
		if (input.contains("false")) {
			return false;
		}
		throw new IllegalArgumentException("No booleans in input string.");
	}
	
	/**
	 * Repeats the program.
	 */
	@FXML
	private void analyzeAgain() {
		mainApp.inputView();
	}

}
