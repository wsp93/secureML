/**
 * An unencrypted Support Vector Machine.
 */
import java.math.BigDecimal;
import java.util.*;
 
public class SVM
{
	public static final BigDecimal THRESHOLD = new BigDecimal(0.0);	// For classification, we check the sign of wx - b against this value
	
	/**
	 * Evaluates which class the input belongs to.
	 * If the value wx - b is negative, then the function returns true. 
	 * @param input The client's feature values; "x" in wx - b
	 * @param weights The weight vector; "w" in wx - b
	 * @param intercept "b" in wx - b
	 */
	public static boolean evaluate(BigDecimal[] input, BigDecimal[] weights, BigDecimal intercept)
	{
		BigDecimal innerProduct = LinearAlgebra.innerProduct(input, weights);
		return evalLine(innerProduct, intercept);
	}
	
	/**
	 * Accepts lists instead of arrays.
	 */
	public static boolean evaluate(List<BigDecimal> input, List<BigDecimal> weights, BigDecimal intercept)
	{
		BigDecimal innerProduct = LinearAlgebra.innerProduct(input, weights);
		return evalLine(innerProduct, intercept);
	}
	
	private static boolean evalLine(BigDecimal innerProduct, BigDecimal intercept)
	{
		BigDecimal lineEval = innerProduct.subtract(intercept);
		return lineEval.compareTo(THRESHOLD) < 0;
	}
}
