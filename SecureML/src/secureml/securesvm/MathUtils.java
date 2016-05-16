package secureml.securesvm;
import java.math.BigInteger;
import java.util.Random;

public class MathUtils {

	public static Random prng = new Random();
	
	public static byte[] byteArrayFromLong(long value)
	{
		byte[] array = new byte[64];
		for (int k = 0; k < 64; k++) array[k] = (byte) ((value >> k) & 1);
		return array;
	}
	
	public static BigInteger innerProduct(BigInteger[] vec1, BigInteger[] vec2, BigInteger modulus)
	{
		if (vec1.length != vec2.length || modulus.compareTo(BigInteger.ONE) < 1) return null;
		BigInteger sum = BigInteger.ZERO;
		for (int j = 0; j < vec1.length; j++)
		{
			sum = sum.add(vec1[j].multiply(vec2[j])).mod(modulus);
		}
		return sum;
	}
	
	public static BigInteger getRandomModulo(BigInteger modulus)
	{
		BigInteger temp = new BigInteger(modulus.bitLength(), prng);
		while (temp.compareTo(modulus) != -1) //u value >= modulus
		{
			temp = new BigInteger(modulus.bitLength(), prng);
		}
		return temp;
	}

	public static boolean vectorAdd(BigInteger[] x, BigInteger[] y, BigInteger[] result, BigInteger modulus)
	{
		if (x.length != y.length) return false;
		for (int j = 0; j < x.length; j++)
			result[j] = x[j].add(y[j]).mod(modulus);
		return true;
	}

	
	/**
	 * A helper function for modular subtraction of 2 vectors
	 * @param x Vector base
	 * @param y Vector to subtract
	 * @param result Where difference vector goes
	 * @param modulus Modular base
	 * @return False if vector dimension mismatch, true otherwise on successful subtraction
	 */
	public static boolean vectorSub(BigInteger[] x, BigInteger[] y, BigInteger[] result, BigInteger modulus)
	{
		if (x.length != y.length) return false;
		for (int j = 0; j < x.length; j++)
			result[j] = x[j].subtract(y[j]).mod(modulus);
		return true;
	}
	
}
