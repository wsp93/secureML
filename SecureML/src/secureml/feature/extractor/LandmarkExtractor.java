package secureml.feature.extractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class LandmarkExtractor {
	public LandmarkExtractor() {
		String s = "";
		String result = "";
		
		try
		{
			Process p = Runtime.getRuntime().exec("python src/secureml/feature/extractor/face_landmark_detection.py "
					+ "src/secureml/feature/extractor/shape_predictor_68_face_landmarks.dat "
					+ "src/secureml/feature/extractor/me.jpg ");
			BufferedReader stdInput = new BufferedReader(new
	                									InputStreamReader(p.getInputStream()));
			while ((s = stdInput.readLine()) != null) 
			{
	             result += s + "\n";
			}
			
			System.out.println(result);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
	}
}
