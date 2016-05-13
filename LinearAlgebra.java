import java.util.List;
import java.math.BigDecimal;

public class LinearAlgebra
{
	public static BigDecimal innerProduct(List<BigDecimal> x, List<BigDecimal> y)
	{
		return innerProduct(x.toArray(new BigDecimal[x.size()]), 
							y.toArray(new BigDecimal[y.size()]));
	}
	
	public static BigDecimal innerProduct(BigDecimal[] x, BigDecimal[] y)
	{
		checkLengthsEqual(x, y);
		BigDecimal sum = new BigDecimal(0.0);
		
		for(int i = 0; i < x.length; i++)
		{
			BigDecimal product = x[i].multiply(y[i]);
			sum = sum.add(product);
		}
		
		return sum;
	}
	
	private static void checkLengthsEqual(BigDecimal[] x, BigDecimal[] y)
	{
		if(x.length != y.length)
		{
			throw new IllegalArgumentException("Vector Lengths different: " + x.length + ", " + y.length);
		}
	}
}
