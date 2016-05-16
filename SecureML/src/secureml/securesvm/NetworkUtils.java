package secureml.securesvm;
///2015 UWT CDS project: privacy preserving machine learning classification 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

import javafx.scene.control.TextArea;

public class NetworkUtils {

	static TextArea bytesOut;
	static int roundCount = 0;
	
	public static void sendBigInteger(OutputStream out, BigInteger toSend) throws IOException
	{
		out.write(toSend.bitLength());
		out.write(toSend.toByteArray());
	}

	public static BigInteger receiveBigInteger(InputStream in) throws IOException
	{
		int bit_len = in.read();
		byte[] bytes = new byte[bit_len / 8 + 1];
		if (in.read(bytes) < bytes.length) return null;
		return new BigInteger(bytes);
	}

	public static void sendVector(OutputStream out, BigInteger[] vector) throws IOException
	{
		if (bytesOut != null)
		{
			bytesOut.appendText("Sending Encrypted Vector---\n");
		}
		for (int j = 0; j < vector.length; j++)
		{
//			if (bytesOut != null)
//			{
//				bytesOut.appendText("" + vector[j].bitLength() + vector[j]);
//			}
			out.write(vector[j].bitLength());
			out.write(vector[j].toByteArray());
		}
	}
	
	public static void sendVector(OutputStream out, byte[] vector) throws IOException
	{
		roundCount++;
		out.write(vector);
	}

	public static boolean receiveVector(InputStream in, BigInteger[] vector) throws IOException
	{
		roundCount++;
		for (int j = 0; j < vector.length; j++)
		{
			int bit_len = in.read();
			byte[] bytes = new byte[bit_len / 8 + 1];
			for (int p = 0; p < bit_len/8 + 1; p++) bytes[p] = (byte)in.read();
//			if (in.read(bytes) == -1) return false;
			vector[j] = new BigInteger(bytes);
		}
		return true;
	}
	
	public static boolean receiveVector(InputStream in, byte[] vector) throws IOException
	{
		for (int j = 0; j < vector.length; j++)
		{
			vector[j] = (byte) in.read();
			if (vector[j] == -1) return false;
		}
		return true;
	}

	public static BigInteger[] multipartyMultiply(InputStream in, OutputStream out, BigInteger[] x_share, 
			BigInteger[] y_share, BigInteger[] u_share, BigInteger[] v_share, BigInteger[] uv_share, BigInteger modulus, boolean alice)
	{
		roundCount++;
		try {
			int L = x_share.length;
			BigInteger[] c = new BigInteger[L], d = new BigInteger[L];
			for (int k = 0; k < L; k++)
			{
				c[k] = u_share[k].subtract(x_share[k]).mod(modulus);
				sendBigInteger(out, c[k]);
				d[k] = v_share[k].subtract(y_share[k]).mod(modulus);
				sendBigInteger(out, d[k]);
			}
			//Open values
			out.flush();

			for (int j = 0; j < L; j++)
			{
				c[j] = c[j].add(receiveBigInteger(in)).mod(modulus);
				d[j] = d[j].add(receiveBigInteger(in)).mod(modulus);
			}

			BigInteger[] newShares = new BigInteger[L];
			for (int p = 0; p < L; p++)
			{
				newShares[p] = uv_share[p];
				newShares[p] = newShares[p].subtract(c[p].multiply(v_share[p]).mod(modulus)).mod(modulus);
				newShares[p] = newShares[p].subtract(d[p].multiply(u_share[p]).mod(modulus)).mod(modulus);
				if (alice) newShares[p] = newShares[p].add(c[p].multiply(d[p]).mod(modulus)).mod(modulus);
			}
			return newShares;
		} catch (Exception e)
		{
			System.out.println("Distributed parallel multiply failed: " + e.getMessage());
			return null;
		}
	}

	public static BigInteger multipartyMultiply(InputStream in, OutputStream out, BigInteger x_share, 
			BigInteger y_share, BigInteger u_share, BigInteger v_share, BigInteger uv_share, BigInteger modulus, boolean alice)
	{
		roundCount++;
		try {
			BigInteger c = u_share.subtract(x_share).mod(modulus);
			BigInteger d = v_share.subtract(y_share).mod(modulus);
			//Open values
			sendBigInteger(out, c);
			sendBigInteger(out, d);
			out.flush();

			c = c.add(receiveBigInteger(in)).mod(modulus);
			d = d.add(receiveBigInteger(in)).mod(modulus);

			BigInteger newShare = uv_share;
			newShare = newShare.subtract(c.multiply(v_share).mod(modulus)).mod(modulus);
			newShare = newShare.subtract(d.multiply(u_share).mod(modulus)).mod(modulus);
			if (alice) newShare = newShare.add(c.multiply(d).mod(modulus)).mod(modulus);
			return newShare;
		} catch (Exception e)
		{
			System.out.println("Distributed multiply failed: " + e.getMessage());
			return null;
		}
	}

	public static byte[] multipartyAnd(InputStream in, OutputStream out, byte[] x, 
			byte[] y, byte[] u, byte[] v, byte[] uv, boolean alice)
	{
		roundCount++;
		try {
			if (x.length != y.length || x.length != u.length || x.length != v.length)
				throw new Exception("Argument lengths do not match");
			int L = x.length;
			byte[] c = new byte[L];
			byte[] d = new byte[L];
			for (int j = 0; j < L; j++)
			{
				c[j] = (byte) (u[j] ^ x[j]);
				d[j] = (byte) (v[j] ^ y[j]);
			}
			
			for (int j = 0; j < L; j++)
				out.write(c[j]);
		//	System.out.println("Wrote C");
			out.write(d);
		//	System.out.println("Wrote D");
			out.flush();
		//	System.out.println("Flushed");

			byte[] c_in= new byte[L], d_in = new byte[L];
			for (int j = 0; j < L; j++){
		//		System.out.println("Read " + j);
				c_in[j] = (byte) in.read();
			}
			for (int j = 0; j < L; j++){
		//		System.out.println("Read " + j);
				d_in[j] = (byte) in.read();
			}
		//	System.out.println("Read finished");
		//	int l1 = in.read(c_in);
	//		if (l1 != L || in.read(d_in) != L)
	//		{
	//	//		throw new IOException("Did not receive proper length response in parallel multiparty AND.");
	//		}

			byte[] newShares = new byte[L];
			for (int j = 0; j < L; j++)
			{
				c[j] ^= c_in[j];
				d[j] ^= d_in[j];

				newShares[j] = (byte) (uv[j] ^ (c[j] & v[j]) ^ (d[j] & u[j]));
				if (alice) newShares[j] ^= c[j] & d[j];
			}

			return newShares;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public static byte multipartyAnd(InputStream in, OutputStream out, byte x_share, 
			byte y_share, byte u_share, byte v_share, byte uv_share, boolean alice)
	{
		roundCount++;
		try {
			int c = u_share ^ x_share;
			int d = v_share ^ y_share;
			//Open values
			out.write(c);
			out.write(d);
			out.flush();

			c ^= in.read();
			d ^= in.read();

			int newShare = uv_share;
			newShare ^= c & v_share;
			newShare ^= d & u_share;
			if (alice) newShare ^= (c & d);
			return (byte)newShare;
		} catch (Exception e)
		{
			System.out.println("Distributed AND failed: " + e.getMessage());
			return -1;
		}
	}
	
}
