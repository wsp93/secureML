package secureml.svm;
import java.io.*;
import java.net.*;
import java.util.ArrayList;


// test git from eclipse
public class SVMClient
{
	public static final String hostName = "140.142.71.66";
	public static final int portNumber = 6666;
//	public static final String textInputFile = "testTextInput.txt";
//	public static final String picInputFile = "testPicInput.txt";
	
	private ArrayList<Double> textFeatures;
	private ArrayList<Integer> picFeatures;
	
	public String predict() {
		try
		{
			Socket socket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			String result = this.runProtocol(in, out);
			
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
	
	/*
	public static void main(String[] args)
	{
		ArrayList<Double> textFeatures = new ArrayList<>(Arrays.asList(0.8148148148148148,
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
		ArrayList<Integer> imageFeatures = new ArrayList<>(Arrays.asList(273,389,272,441,278,493,289,547,301,600,324,649,357,691,402,723,458,733,518,725,576,698,626,663,662,619,685,566,696,510,702,454,705,397,289,337,312,317,346,313,380,324,412,341,486,341,523,321,564,309,604,315,637,334,448,382,446,409,444,438,441,468,414,506,429,511,447,516,466,511,485,505,328,393,349,384,375,384,399,393,374,405,348,406,510,393,532,382,560,383,586,390,562,403,535,404,375,599,404,584,429,573,446,578,463,573,495,584,535,598,497,615,468,622,449,624,430,623,405,617,389,598,430,592,447,594,465,592,517,598,465,594,447,596,430,595));
		SVMClient client = new SVMClient(textFeatures, imageFeatures);
		System.out.println(client.predict());
	}
	*/
	public SVMClient(ArrayList<Double> textFeatures, ArrayList<Integer> imageFeatures) 
	{
		this.textFeatures = textFeatures;
		this.picFeatures = imageFeatures;
	}

	public String runProtocol(BufferedReader in, PrintWriter out) throws IOException
	{
		out.println(textFeatures.toString());
		out.println(picFeatures.toString());
		return in.readLine();
	}
	/*
	private List<BigDecimal> readInputFile(String fileName) throws FileNotFoundException
	{
		Scanner fileScanner = new Scanner(new File(fileName));
		List<BigDecimal> features = new ArrayList<BigDecimal>();
		
		while(fileScanner.hasNextLine())
		{
			try
			{
				features.add(new BigDecimal(fileScanner.nextLine()));
			}
			catch(NumberFormatException e)
			{
				continue;
			}
		}

		fileScanner.close();
		
		return features;
	}
	*/
}
