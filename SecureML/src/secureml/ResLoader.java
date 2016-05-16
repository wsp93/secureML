/*
 * Mike Nickels
 * 
 * developed for
 * University of Washington, Tacoma
 * Privacy Preserving Maching Learning Group
 * secureml.insttech.washington.edu
 */

package secureml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * A singleton class used to load resources in a relative and safe way.
 * Access the instance with the {@link #getInstance()} method.
 * 
 * @author Mike Nickels | mnickels@uw.edu
 * @version 0.5
 */
public final class ResLoader {
	
	private static final ResLoader INSTANCE = new ResLoader();

	private ResLoader() { }
	
	public static ResLoader getInstance() {
		return INSTANCE;
	}

	/**
	 * Load an image from a file in the res folder.
	 * @param filename the name of the file to load, including extension.
	 * @return An image containing the information provided in the specified image file.
	 * 		   Returns null if there is an error loading the image, or if the input filename is null.
	 */
	public Image loadImage(String filename) {
		if (filename != null) {
			try {
				return SwingFXUtils.toFXImage(ImageIO.read(loadFile(filename)), null);
			} catch (IOException e) {
				System.out.printf("Error loading image: %s\n", filename);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**	
	 * Loads a file.
	 * @param filename the name of the file to be loaded, including extension.
	 * @return A File containing the data of the specified file.
	 * 		   Returns null if there is an error loading the file, or if the input filename is null.
	 */
	public File loadFile(String filename) {
		if (filename != null) {
			try {
				return new File(getClass().getClassLoader().getResource(filename).toURI());
			} catch (URISyntaxException e) {
				System.out.printf("Error loading file: %s\n", filename);
				e.printStackTrace();
			}
		}
		return null;
	}

}
