package secureml.svm;
import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SVMClient
{
	public static final String hostName = "140.142.71.66";
	public static final int portNumber = 1234;
	
	public String predict(ArrayList<Double> features) {
		try
		{
			SVMClient client = new SVMClient();
			Socket socket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String result = client.runProtocol(in, out, features);
			
			in.close();
			out.close();
			socket.close();
			return result;
		}
		catch(Exception e)
		{
			return(e.getMessage());
		}
		
	}
	public static void main(String[] args)
	{
		ArrayList<Double> features = new ArrayList<Double>(
				Arrays.asList(0.8148148148148148,
						0.18518518518518517,
						0.05263157894736842,
						0.3157894736842105,
						0.0,
						0.08771929824561403,
						0.17543859649122806,
						0.07017543859649122,
						0.08771929824561403,
						0.21052631578947367,
						3.5319767441860463,
						1.183139534883721,
						1.072674418604651,
						6897.332848837209,
						13.244186046511627,
						296.79651162790697,
						29082.658430232557,
						1653.4186046511627,
						479.0218023255814,
						240.9171511627907,
						258.40843023255815,
						270.7369186046512,
						25.563953488372093,
						18.611918604651162,
						688.0,
						18.594594594594593,
						41.71511627906977,
						8.430232558139535,
						0.0,
						0.0,
						10.81081081081081,
						4.796511627906977,
						4.941860465116279,
						0.0,
						0.14534883720930233,
						0.5813953488372093,
						0.0,
						0.14534883720930233,
						0.29069767441860467,
						3.488372093023256,
						0.0,
						0.5813953488372093,
						14.970930232558139));
		SVMClient client = new SVMClient();
		String result = client.predict(features);
		System.out.println(result);

	}

	public String runProtocol(BufferedReader in, PrintWriter out, List<Double> features) throws IOException
	{
		out.println(features.toString());
		return in.readLine();
	}
}
