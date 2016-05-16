package secureml.securesvm;
///2015 UWT CDS project: privacy preserving machine learning classification 
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class SVM {
	public static boolean runAsAlice(InputStream bobIn, OutputStream bobOut, 
			BigInteger[] inputs, BigInteger modulus, TrustedInitializer ted) throws Exception
	{
		try {
			if (NetworkUtils.bytesOut != null)
				NetworkUtils.bytesOut.appendText("Initiating Private Inner Product---\n");
			BigInteger dotShare = NetworkProtocols.multipartyDotProductAlice(bobIn, bobOut, inputs, 
					ted.getAvailableDotVecs().poll(), modulus);
			if (NetworkUtils.bytesOut != null)
				NetworkUtils.bytesOut.appendText("Private Inner Product Complete---\n");
			
			byte[] xbitShares = NetworkProtocols.bitDecomposeLong(bobIn, bobOut, dotShare.longValue(), 
					ted.getAvailableAnds(), true);
			if (NetworkUtils.bytesOut != null)
			{
				NetworkUtils.bytesOut.appendText("Result Bit Decomposed for Comparison---\n");
				NetworkUtils.bytesOut.appendText("Initiating Comparison Protocol---\n");
			}

			int result = NetworkProtocols.multipartyComparisonOptimized(bobIn, bobOut, xbitShares, new byte[64], 
					ted.getAvailableAnds(), true);

			int bobShare = bobIn.read();
			result ^= bobShare;
			result &= 1;

			return result == 1;
		} catch (Exception e) {
			throw new Exception("SVM failed: " + e.getMessage());
		}
	}

	public static void runAsBob(InputStream aliceIn, OutputStream aliceOut,
			BigInteger[] modelVector, BigInteger modelB, BigInteger modelThreshhold, BigInteger modulus,
			TrustedInitializer ted) throws Exception
	{
		try
		{
			BigInteger dotShare = NetworkProtocols.multipartyDotProductBob(aliceIn, aliceOut, modelVector, 
					ted.getAvailableDotVecs().poll(), ted.getAvailableDotSvals().poll(), modulus).subtract(modelB).mod(modulus);

			byte[] xbitShares = NetworkProtocols.bitDecomposeLong(aliceIn, aliceOut, dotShare.longValue(), ted.getAvailableAnds(), false);

			int result = NetworkProtocols.multipartyComparisonOptimized(aliceIn, aliceOut, xbitShares, 
					MathUtils.byteArrayFromLong(modelThreshhold.longValue()), ted.getAvailableAnds(), false);
			aliceOut.write(result);
		} catch (Exception e)
		{
			throw new Exception("SVM failed: " + e.getMessage());
		}
	}
}
