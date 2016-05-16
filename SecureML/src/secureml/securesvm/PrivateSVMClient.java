package secureml.securesvm;
//2016 UWT CDS project: Privacy Preserving personality prediction
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;

import javafx.scene.control.TextArea;

public class PrivateSVMClient {
	
	public static PrivateSVMClient globalClient;

	public static long featureMultiplier = 100000;

	public static String trustedInitializerAddress = "secureml.insttech.washington.edu";
	public static final int trustedInitializerPort = 1235;

	private static final BigInteger modulus = BigInteger.ONE.shiftLeft(64);

	private static String[] models = {"ext", "neu", "agr", "con", "opn"}; //The model names we're using, i.e. neuro, extro
	public static int numFeatures = 43;
	private static String serverAddress = "secureml.insttech.washington.edu";
	private static int serverPort = 1432;

	private TrustedInitializer ted;
	private Socket server;

	public PrivateSVMClient(String[] addresses) {
		if (addresses.length == 2)
		{
			trustedInitializerAddress = addresses[0];
			serverAddress = addresses[1];
		}

		try 
		{
			long startTime = System.currentTimeMillis();
			server = new Socket(serverAddress, serverPort);
			InputStream input = server.getInputStream();

			//Establish sessionID
			BigInteger sessionID = NetworkUtils.receiveBigInteger(input);

			//Connect to TI
			Socket trustedInitializer = new Socket(trustedInitializerAddress, trustedInitializerPort);
			OutputStream tiOut = trustedInitializer.getOutputStream();

			//----Retrieve desired pre-randomness----
			NetworkUtils.sendBigInteger(tiOut, sessionID); //Send SessionID to TI for pairing
			tiOut.write(0); //0 for Alice (1 for Bob) should probably define that somewhere clearly

			ted = new TrustedInitializer();
			ted.retrieveAnds(trustedInitializer, 900);
			ted.retrieveDotProducts(trustedInitializer, modulus, models.length,
					numFeatures, true);

			trustedInitializer.close();
			//---------End TI portion ----------

			System.out.println("Closing communication with TI - Time Elapsed: " +  
					(System.currentTimeMillis() - startTime) + " ms");
		} catch (Exception e)
		{
			System.out.println("Failed miserably: " + e.getMessage());
			System.exit(1);
		}
	}

	public boolean[] run(double[] values, TextArea bytesOut) {
		try {
			OutputStream output = server.getOutputStream();
			InputStream input = server.getInputStream();

			NetworkUtils.bytesOut = bytesOut;
			
			//Convert Alice's secret values into integer values
			BigInteger[] aliceSecret = new BigInteger[numFeatures]; //Data vector

			boolean[] results = new boolean[5];
			for (int k = 0; k < numFeatures; k++)
			{
				aliceSecret[k] = BigInteger.valueOf((long)(values[k]*featureMultiplier )).mod(modulus);
			}
			long startTime = System.currentTimeMillis();
			for (int j = 0; j < models.length; j++)
			{
				bytesOut.appendText("Beginning SVM Instance " + j + "---\n");
				results[j] = SVM.runAsAlice(input, output, aliceSecret, modulus, ted);

				bytesOut.appendText("SVM " + j + " Result = " + results[j] + " ---\n");
				System.out.println("Final classification: " + (results[j] ? 1 : 0)  +	" - Remaining: " + 
						ted.getAvailableAnds().size());
			}
			System.out.println("Total classification duration: " + (System.currentTimeMillis() - startTime));

			server.close();
			return results;
		} catch (Exception e)
		{
			System.err.println("Failed miserably: " + e.getMessage());
		}
		return null;
	}	

	public static void main(String[] args) {

		double[] values = new double[numFeatures];
		values = new double[]{ 6.59793814e-01,   3.40206186e-01,   8.33333333e-02,   1.18055556e-01,
				   8.33333333e-02,   1.25000000e-01,   1.73611111e-01,   1.66666667e-01,
				   4.86111111e-02,   2.01388889e-01,   3.57377049e+00,   1.17287630e+00,
				   1.00894188e+00,   6.71136811e+03,   1.26214605e+01,   2.80585693e+02,
				   2.70592444e+04,   1.49656781e+03,   4.54554396e+02,   2.23253353e+02,
				   2.46146051e+02,   2.63166915e+02,   1.39910581e+01,   1.22146051e+01,
				   6.71000000e+02,   1.01666667e+01,   4.30700447e+01,   1.19225037e+01,
				   0.00000000e+00,   0.00000000e+00,   1.51515151e+00,   9.68703428e+00,
				   7.45156483e-01,   0.00000000e+00,   0.00000000e+00,   1.49031297e-01,
				   0.00000000e+00,   0.00000000e+00,   0.00000000e+00,   4.02384501e+00,
				   0.00000000e+00,   2.98062593e-01,   1.49031297e+01};
		Scanner key = new Scanner(System.in);

		PrivateSVMClient ppClient = new PrivateSVMClient(args);
		key.nextLine();
		//ppClient.run(values);
	}

}
