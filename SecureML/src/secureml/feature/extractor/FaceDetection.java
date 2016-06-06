package secureml.feature.extractor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import secureml.Const;

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
	 * @throws FaceDetectionException If 0 or more than 1 face detected. 
	 */
	public static void cropFaces(String sourceImagePath) throws FaceDetectionException {
		System.out.println("Running FaceDetection");

		// Create a face detector from the cascade file in the resources
		// directory.
		CascadeClassifier faceDetector = new CascadeClassifier(Const.FACE_DETECTION_PATH);
		Mat image = Imgcodecs.imread(sourceImagePath);

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));
		boolean noFaceDetected = faceDetections.toArray().length == 0;
		boolean moreThanOneFace = faceDetections.toArray().length > 1;
		if(noFaceDetected) throw new FaceDetectionException("No Face Detected");
		if(moreThanOneFace) throw new FaceDetectionException("More Than One Face Detected");

		// Crop the face out and save as a separate image
		Rect rect = faceDetections.toArray()[0];
		Mat croppedImage = new Mat(image, generateWiderRectangle(rect, image.width(), image.height()));
	    System.out.println("Writing " + Const.CROPPED_IMG_PATH);
	    Imgcodecs.imwrite(Const.CROPPED_IMG_PATH, croppedImage);

		// Draw a bounding box around the face
		Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
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
	private static Rect generateWiderRectangle(Rect rect, int width, int height) {
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
}
