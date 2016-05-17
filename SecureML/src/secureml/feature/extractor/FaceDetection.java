package secureml.feature.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class FaceDetection {
	/**
	 * Percentage that dictates how big of a margin there should be around the faces.
	 */
	public static final double CROP_MARGIN = 0.4;

	/**
	 * Takes in a source image and an output directory. It will scan the source image for faces and
	 * save each face as a separate image into the output directory, as well as the original image with
	 * a green rectangle around each detected face.
	 *
	 * @param sourceImagePath Path to the image to scan for faces.
	 * @param outputImagePath Path to the directory where cropped faces will be saved.
	 */
	public void cropFaces(String sourceImagePath, String outputImagePath) {
		System.out.println("Running FaceDetection");

		// Create a face detector from the cascade file in the resources
		// directory.
		CascadeClassifier faceDetector = new CascadeClassifier("src/secureml/feature/extractor/lbpcascade_frontalface.xml");
		Mat image = Imgcodecs.imread(sourceImagePath);

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

		// Crop each face out and save as a separate image
//		int iterator = 1;
//		for (Rect rect : faceDetections.toArray()) {
			Rect rect = faceDetections.toArray()[0];
			Mat croppedImage = new Mat(image, generateWiderRectangle(rect, image.width(), image.height()));
		    String croppedImageFilePath = outputImagePath + "croppedImage.png";
		    System.out.println("Writing " + croppedImageFilePath);
		    Imgcodecs.imwrite(croppedImageFilePath, croppedImage);
//		    iterator++;
//		}

		// Draw a bounding box around each face
//		for (Rect rect: faceDetections.toArray()) {
			Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
//		}

		// Save the visualized detection.
//		String filename = outputImagePath + "faceDetection.png";
//		System.out.println(String.format("Writing %s", filename));
//		Imgcodecs.imwrite(filename, image);
	}


	/**
	 * Uses CROP_MARGIN to dictate how much to widen the rectangle. Includes bounds checking to make sure
	 * rectangle doesn't go out of the bounds of the image.
	 *
	 * @param rect The original rectangle that will be scaled.
	 * @param width Width of the image. This restricts the bounds of the scaled rectangle.
	 * @param height Height of the image. This restricts the boudns of the scaled rectangle.
	 * @return
	 */
	private Rect generateWiderRectangle(Rect rect, int width, int height) {
		Rect widerRect = rect.clone();

		double newX = (int) (rect.x - (rect.width * CROP_MARGIN));
		double newY = (int) (rect.y - (rect.width * CROP_MARGIN));
		// Check for possible out of bounds of image
		if (newX < 0) {
			newX = 0.0;
		}
		if (newY < 0) {
			newY = 0.0;
		}

		double newWidth = rect.width * (1 + (CROP_MARGIN * 2)); // CROP_MARGIN * 2 to account for newX and newY
		double newHeight = rect.height * (1 + (CROP_MARGIN * 2));
		// Check for possible out of bounds of image
		if(newX + newWidth >= width) {
			newWidth = width - newX;
		}
		if(newY + newHeight >= height) {
			newHeight = height - newY;
		}

		widerRect.set(new double[]{newX, newY, newWidth, newHeight});
		return widerRect;
	}

//	public static void main(String[] args) throws FileNotFoundException {
//	    // Load the native library.
//	    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//	    new FaceDetection().cropFaces("src/secureml/feature/extractor/me.jpg", "res/");
//	}


}
