package secureml.securesvm;
///2015 UWT CDS project: privacy preserving machine learning classification 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class NetworkProtocols {
	public static BigInteger multipartyDotProductAlice(InputStream bobIn, OutputStream bobOut, BigInteger[] secret, 
			BigInteger[] x, BigInteger modulus)
	{
		int vectorDimension = x.length;
		try {
			//---Begin Inner Product---
			BigInteger[] y_1 = new BigInteger[vectorDimension];
			if (!NetworkUtils.receiveVector(bobIn, y_1)) System.out.println("Vector receive from Bob failed.");

			BigInteger[] x_1 = new BigInteger[vectorDimension];
			MathUtils.vectorAdd(secret, x, x_1, modulus);

			BigInteger u = MathUtils.getRandomModulo(modulus);
			BigInteger u_1 = MathUtils.innerProduct(secret, y_1, modulus).subtract(u).mod(modulus);

			NetworkUtils.sendVector(bobOut, x_1);
			NetworkUtils.sendBigInteger(bobOut, u_1);
			return u;
		} catch(Exception e)
		{
			System.out.println("Distributed dot product failed: " + e.getMessage());
			return null;
		}
	}

	public static BigInteger multipartyDotProductBob(InputStream aliceIn, OutputStream aliceOut, BigInteger[] secret, 
			BigInteger[] y_0, BigInteger s, BigInteger modulus)
	{
		int vectorDimension = secret.length;
		try {
			//---Begin Dot Product---
			BigInteger[] y_1 = new BigInteger[vectorDimension];
			MathUtils.vectorSub(secret, y_0, y_1, modulus);
			NetworkUtils.sendVector(aliceOut, y_1);

			BigInteger[] x_1 = new BigInteger[vectorDimension];
			if (!NetworkUtils.receiveVector(aliceIn, x_1)) System.out.println("Failed to receive u_1 from Alice");

			BigInteger u_1 = NetworkUtils.receiveBigInteger(aliceIn);

			BigInteger v = MathUtils.innerProduct(x_1, y_0, modulus).add(u_1).mod(modulus).subtract(s).mod(modulus);
			return v;
		} catch (Exception e)
		{
			System.out.println("Distributed dot product failed: " + e.getMessage());
			return null;
		}
	}


	//V 1.1 Logarithmic round (8 for input size 64, might not work for non powers of two, didn't test).
	public static byte multipartyComparisonOptimized(InputStream in, OutputStream out, byte[] xbitShares, 
			byte[] ybitShares, Queue<byte[]> tiAnds, boolean alice)
	{
		//Initialization
		int l = xbitShares.length;

		byte[] c_shares = new byte[l];
		byte[] d_shares = new byte[l];
		byte[] e_shares = new byte[l];

		//Aquire requisite AND randomness
		byte[] uVals = new byte[l], vVals = new byte[l], uvVals = new byte[l];
		for (int i = 0; i < uVals.length; i++)
		{ 
			byte[] r = tiAnds.poll(); 
			for (int p = 0; p < 8; p++)
			{
				uVals[i] = (byte) ((r[0] >> p) & 1); 
				vVals[i] = (byte) ((r[1] >> p) & 1); 
				uvVals[i] = (byte)((r[2] >> p) & 1); 
				i++;
				if (i >= uVals.length) break;
			}
		}

		//d is 1 if y bit > x bit
		d_shares = NetworkUtils.multipartyAnd(in, out, xbitShares, ybitShares, uVals, 
				vVals, uvVals, alice);
		//e is 1 iff y bit = x bit
		for (int i = 0; i < l; i++)
		{
			d_shares[i] ^= ybitShares[i];
			e_shares[i] = (byte) (xbitShares[i] ^ ybitShares[i] ^ (alice ? 0xFF : 0));
		}
		LinkedList<Block> blocks = new LinkedList<Block>();
		for (int j = 0; j < l; j++)
			blocks.add(new Block(e_shares[j]));
		while (blocks.size() > 1)
		{
			byte[] firstArg = new byte[l / 2];
			byte [] secondArg = new byte[l / 2];
			for (int j = 0; j < blocks.size() / 2; j++)
			{
				byte[] p1 = blocks.get(2*j).getArg1ToComp(blocks.get(2*j + 1));
				byte[] p2 = blocks.get(2*j).getArg2ToComp(blocks.get(2*j + 1));
				System.arraycopy(p1, 0, firstArg, j *p1.length, p1.length);
				System.arraycopy(p2, 0, secondArg, j * p2.length, p2.length);
			}
			uVals = new byte[firstArg.length]; vVals = new byte[firstArg.length]; uvVals = new byte[firstArg.length];
			for (int i = 0; i < uVals.length; i++)
			{ 
				byte[] r = tiAnds.poll(); 
				for (int p = 0; p < 8; p++)
				{
					uVals[i] = (byte) ((r[0] >> p) & 1); 
					vVals[i] = (byte) ((r[1] >> p) & 1); 
					uvVals[i] = (byte)((r[2] >> p) & 1); 
					i++;
					if (i >= uVals.length) break;
				}
			}

			byte[] result = NetworkUtils.multipartyAnd(in, out, firstArg, secondArg, uVals, vVals, uvVals, alice); //1 round, log_2{64} occurrences = 6 rounds?

			int index = result.length;
			for (int j = blocks.size() - 2; j >= 0; j-= 2)
			{
				int length = blocks.get(j).blockSize;
				blocks.get(j).mergeCompare(blocks.get(j+1), Arrays.copyOfRange(result, index - length, index)); //Merge with previous block
				index -= length; //backtrack index
				blocks.remove(j + 1); //Remove block that has been absorbed
			}
		}
		uVals = new byte[l]; vVals = new byte[l]; uvVals = new byte[l];
		for (int i = 0; i < uVals.length; i++)
		{ 
			byte[] r = tiAnds.poll(); 
			for (int p = 0; p < 8; p++)
			{
				uVals[i] = (byte) ((r[0] >> p) & 1); 
				vVals[i] = (byte) ((r[1] >> p) & 1); 
				uvVals[i] = (byte)((r[2] >> p) & 1); 
				i++;
				if (i >= uVals.length) break;
			}
		}

		byte[] secondArg = new byte[l];
		System.arraycopy(blocks.get(0).subsetProducts, 1, secondArg, 0, l-1);
		if (alice) secondArg[l-1] = (byte) 0xFF;
		c_shares = NetworkUtils.multipartyAnd(in, out, d_shares, secondArg, uVals, 
				vVals, uvVals, alice);


		byte w = c_shares[0];
		for (int j = 1; j < l; j++) w ^= c_shares[j];
		return w;
	}

	static byte[] packArrays(byte[][] arrays)
	{
		byte[] toReturn = new byte[arrays[0].length*arrays.length];
		for (int p = 0; p < arrays.length; p++)
			System.arraycopy(arrays[p], 0, toReturn, p*arrays[0].length, arrays[0].length);
		return toReturn;
	}

	static byte[][] unpackArrays(byte[] array, int num)
	{
		byte[][] toReturn = new byte[num][];
		for (int j = 0; j < num; j++)
			toReturn[j] = Arrays.copyOfRange(array, j*(array.length/num), (j+1)*(array.length/num));
		return toReturn;
	}

	static byte[] multipartyComparisonParallel(InputStream in, OutputStream out, byte[][] xbitShares, 
			byte[][] ybitShares, Queue<byte[]> tiAnds, boolean alice)
	{
		//Initialization
		int l = xbitShares[0].length;
		int n = xbitShares.length;

		byte[][] c_shares = new byte[n][l];
		byte[][] d_shares = new byte[n][l];
		byte[][] e_shares = new byte[n][l];

		//Aquire requisite AND randomness
		byte[] uVals = new byte[l*n], vVals = new byte[l*n], uvVals = new byte[l*n];
		for (int i = 0; i < uVals.length; i++)
		{ 
			byte[] r = tiAnds.poll(); 
			for (int p = 0; p < 8; p++)
			{
				uVals[i] = (byte) ((r[0] >> p) & 1); 
				vVals[i] = (byte) ((r[1] >> p) & 1); 
				uvVals[i] = (byte)((r[2] >> p) & 1); 
				i++;
				if (i >= uVals.length) break;
			}
		}


		//d is 1 if y bit > x bit
		byte[] result = NetworkUtils.multipartyAnd(in, out, packArrays(xbitShares), packArrays(ybitShares), uVals, 
				vVals, uvVals, alice);
		d_shares = unpackArrays(result, n);
		//e is 1 iff y bit = x bit
		for (int k = 0; k < n; k++)
		{
			for (int i = 0; i < l; i++)
			{
				d_shares[k][i] ^= ybitShares[k][i];
				e_shares[k][i] = (byte) (xbitShares[k][i] ^ ybitShares[k][i] ^ (alice ? 0xFF : 0));
			}
		}
		LinkedList<Block>[] blocks = new LinkedList[n];
		for (int k = 0; k < n; k++)
		{
			blocks[k] = new LinkedList<Block>();
			for (int j = 0; j < l; j++)
				blocks[k].add(new Block(e_shares[k][j]));
		}
		while (blocks[0].size() > 1)
		{
			byte[][] firstArg = new byte[n][l/2];
			byte[][] secondArg = new byte[n][l/2];
			for (int k = 0; k < n; k++)
			{
				for (int j = 0; j < blocks[k].size() / 2; j++)
				{
					byte[] p1 = blocks[k].get(2*j).getArg1ToComp(blocks[k].get(2*j + 1));
					byte[] p2 = blocks[k].get(2*j).getArg2ToComp(blocks[k].get(2*j + 1));
					System.arraycopy(p1, 0, firstArg[k],  j * p1.length, p1.length);
					System.arraycopy(p2, 0, secondArg[k], j * p2.length, p2.length);
				}
			}
			uVals = new byte[firstArg[0].length*n]; vVals = new byte[firstArg[0].length*n]; uvVals = new byte[firstArg[0].length*n];
			for (int i = 0; i < uVals.length; i++)
			{ 
				byte[] r = tiAnds.poll(); 
				for (int p = 0; p < 8; p++)
				{
					uVals[i] = (byte) ((r[0] >> p) & 1); 
					vVals[i] = (byte) ((r[1] >> p) & 1); 
					uvVals[i] = (byte)((r[2] >> p) & 1); 
					i++;
					if (i >= uVals.length) break;
				}
			}


			result = NetworkUtils.multipartyAnd(in, out, packArrays(firstArg), packArrays(secondArg), uVals, vVals, uvVals, alice); //1 round, log_2{64} occurrences = 6 rounds?

			for (int k = 0; k < n; k++)
			{
				byte[][] splitResult = unpackArrays(result, n);
				int index = splitResult[k].length;
				for (int j = blocks[k].size() - 2; j >= 0; j-= 2)
				{
					//System.out.print(j + " " + (j+1) + "\n");
					int length = blocks[k].get(j).blockSize;
					blocks[k].get(j).mergeCompare(blocks[k].get(j+1), Arrays.copyOfRange(splitResult[k], index - length, index)); //Merge with previous block
					index -= length; //backtrack index
					blocks[k].remove(j + 1); //Remove block that has been absorbed
				}
			}
		}

		uVals = new byte[l*n]; vVals = new byte[l*n]; uvVals = new byte[l*n];
		for (int i = 0; i < uVals.length; i++)
		{ 
			byte[] r = tiAnds.poll(); 
			for (int p = 0; p < 8; p++)
			{
				uVals[i] = (byte) ((r[0] >> p) & 1); 
				vVals[i] = (byte) ((r[1] >> p) & 1); 
				uvVals[i] = (byte)((r[2] >> p) & 1); 
				i++;
				if (i >= uVals.length) break;
			}
		}

		byte[][] secondArg = new byte[n][l];
		for (int k = 0; k < n; k++)
		{
			System.arraycopy(blocks[k].get(0).subsetProducts, 1, secondArg[k], 0, l-1);
			if (alice) secondArg[k][l-1] = (byte) 0xFF;
		}
		result = NetworkUtils.multipartyAnd(in, out, packArrays(d_shares), packArrays(secondArg), uVals, 
				vVals, uvVals, alice);
		c_shares = unpackArrays(result, n);

		byte[] w = new byte[n];
		for (int k = 0; k < n; k++)
			w[k] = c_shares[k][0];
		for (int k = 0; k < n; k++)
			for (int j = 1; j < l; j++) 
				w[k] ^= c_shares[k][j];
		return w;
	}

	static byte[] bytesOfLong(long value)
	{
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) bytes[i] = (byte) ((value >> (8*i) ) & 0xFF); 
		return bytes;
	}

	static long longFromBytes(byte[] bytes)
	{
		long value = 0;
		for (int i = 0; i < 8; i++) value |= (((long)(bytes[i]) & 0xFF) << (8*i)) ;
		return value;
	}

	//A helper class to track and organize arguments for the parallel AND operations.
	//Suboptimal, as it uses a byte for each bit (8x space wasted), but raw bitwise operations 
	//were messing with my brain too much
	private static class Block
	{
		//---For Comparison usage---
		byte[] subsetProducts;

		Block(byte firstValue)
		{
			subsetProducts = new byte[1];
			subsetProducts[0] = firstValue;
			blockSize = 1;
		}

		//Should multiply all subsets with the product of the entire next block.
		byte[] getArg1ToComp(Block nextBlock)
		{
			return subsetProducts;
		}
		byte[] getArg2ToComp(Block nextBlock)
		{
			byte[] bytes = new byte[blockSize];
			Arrays.fill(bytes, nextBlock.subsetProducts[0]);
			return bytes;
		}

		void mergeCompare(Block nextBlock, byte[] result)
		{
			subsetProducts = Arrays.copyOf(result, 2*blockSize);
			System.arraycopy(nextBlock.subsetProducts, 0, subsetProducts, blockSize, blockSize);
			blockSize *= 2;
		}

		//---For Bit Decomposition usage---
		byte[] forCarryInZero, forCarryInOne;
		byte carryOutForZeroIn, carryOutForOneIn;
		int blockSize;
		boolean first;
		Block(byte x0, byte x1, byte c0, byte c1, boolean isFirst)
		{
			blockSize = 1;
			forCarryInZero = new byte[]{x0};
			forCarryInOne = new byte[]{x1};
			carryOutForZeroIn = c0;
			carryOutForOneIn = c1;
			first = isFirst;
		}

		//forCarryInZero will be lastBlock.carryOutForZeroIn & forCarryInOne ^ !lastBlock.carryOutForZeroIn & forCarryInZero
		//forCarryInOne will be lastBlock.carryOutForOneIn & forCarryInOne ^ !lastBlock.carryOutForOneIn & forCarryInZero
		//carryOutForZeroIn will be lastBlock.carryOutForZeroIn & carryOutForOneIn & !lastBlock.carryOutForZeroIn & carryOutForOneIn
		//carryOutForOneIn will be lastBlock.carryOutForOneIn & carryOutForOneIn & !lastBlock.carryOutForOneIn & carryOutForOneIn
		byte[] getArg1ToMerge(Block lastBlock)
		{
			byte[] toReturn;
			if (lastBlock.first) //Don't need forCarryInOne, is guaranteed as zero
			{
				toReturn = new byte[2 + blockSize*2];
				System.arraycopy(forCarryInZero, 0, toReturn, 0, blockSize);
				System.arraycopy(forCarryInOne, 0, toReturn, blockSize, blockSize);
				toReturn[blockSize * 2] = carryOutForZeroIn;
				toReturn[blockSize * 2 + 1] = carryOutForOneIn;
			}
			else
			{
				toReturn = new byte[4 + blockSize*4];
				System.arraycopy(forCarryInZero, 0, toReturn, 0, blockSize);
				System.arraycopy(forCarryInOne, 0, toReturn, blockSize, blockSize);
				System.arraycopy(forCarryInZero, 0, toReturn, 2*blockSize, blockSize);
				System.arraycopy(forCarryInOne, 0, toReturn, 3*blockSize, blockSize);
				toReturn[blockSize * 4] = carryOutForZeroIn;
				toReturn[blockSize * 4 + 1] = carryOutForOneIn;
				toReturn[blockSize * 4 + 2] = carryOutForZeroIn;
				toReturn[blockSize * 4 + 3] = carryOutForOneIn;
			}
			return toReturn;
		}
		byte[] getArg2ToMerge(Block lastBlock, boolean alice)
		{
			byte[] toReturn;
			if (lastBlock.first) //Don't need forCarryInOne, is guaranteed as zero
			{
				toReturn = new byte[2 + blockSize*2];
				Arrays.fill(toReturn, 0, blockSize, (byte)(lastBlock.carryOutForZeroIn ^ (alice ? 0 : 1)));
				Arrays.fill(toReturn, blockSize, 2*blockSize, lastBlock.carryOutForZeroIn);
				toReturn[blockSize * 2] = (byte)(lastBlock.carryOutForZeroIn ^ (alice ? 0 : 1));
				toReturn[blockSize * 2 + 1] = lastBlock.carryOutForZeroIn;
			}
			else
			{
				toReturn = new byte[4 + blockSize*4];
				Arrays.fill(toReturn, 0, blockSize, (byte)(lastBlock.carryOutForZeroIn ^ (alice ? 0 : 1)));
				Arrays.fill(toReturn, blockSize, 2*blockSize, lastBlock.carryOutForZeroIn);
				Arrays.fill(toReturn, 2*blockSize, 3*blockSize, (byte)(lastBlock.carryOutForOneIn ^ (alice ? 0 : 1)));
				Arrays.fill(toReturn, 3*blockSize, 4*blockSize, lastBlock.carryOutForOneIn);
				toReturn[blockSize * 4] = (byte)(lastBlock.carryOutForZeroIn ^ (alice ? 0 : 1));
				toReturn[blockSize * 4 + 1] = lastBlock.carryOutForZeroIn;
				toReturn[blockSize * 4 + 2] = (byte)(lastBlock.carryOutForOneIn ^ (alice ? 0 : 1));
				toReturn[blockSize * 4 + 3] = lastBlock.carryOutForOneIn;
			}
			return toReturn;
		}

		void merge(Block lastBlock, byte[] result)
		{
			if (lastBlock.first)
			{
				lastBlock.forCarryInZero = Arrays.copyOf(lastBlock.forCarryInZero, blockSize * 2);
				System.arraycopy(result, 0, lastBlock.forCarryInZero, blockSize, blockSize);
				for (int p = 0; p < blockSize; p++)
					lastBlock.forCarryInZero[blockSize + p] ^= result[blockSize + p];

				lastBlock.carryOutForZeroIn = (byte) (result[blockSize * 2] ^ result[blockSize*2 + 1]);
			}
			else
			{
				lastBlock.forCarryInZero = Arrays.copyOf(lastBlock.forCarryInZero, blockSize * 2);
				lastBlock.forCarryInOne = Arrays.copyOf(lastBlock.forCarryInOne, blockSize * 2);

				System.arraycopy(result, 0, lastBlock.forCarryInZero, blockSize, blockSize);
				System.arraycopy(result, 2 * blockSize, lastBlock.forCarryInOne, blockSize, blockSize);
				for (int p = 0; p < blockSize; p++)
				{
					lastBlock.forCarryInZero[blockSize + p] ^= result[blockSize + p];
					lastBlock.forCarryInOne[blockSize + p] ^= result[3*blockSize + p];
				}

				lastBlock.carryOutForZeroIn = (byte) (result[blockSize * 4] ^ result[blockSize*4 + 1]);
				lastBlock.carryOutForOneIn = (byte) (result[blockSize * 4 + 2] ^ result[blockSize*4 + 3]);
			}

			lastBlock.blockSize *= 2;
		}
	}

	//v 1.1, parallel, logarithmic round complexity, fixed at input/output size of 64 (or fewer) bits.
	public static byte[] bitDecomposeLong(InputStream in, OutputStream out, long x, Queue<byte[]> tiAnds, boolean alice)
	{
		long xForCarry0, xForCarry1;
		long carriesFor0, carriesFor1;
		byte[] allOnes = new byte[Long.SIZE/8];
		Arrays.fill(allOnes, (byte)0xFF);

		//Want ([x]a & [x]b) | (x[a] ^ x[b]) & [c=0/1]
		//     ---Gate 1---          ---Gate 2---
		//               ---Gate 3---

		byte[] firstArg = new byte[Long.SIZE/8 * 3];
		byte[] secondArg = new byte[Long.SIZE/8 * 3];
		if (alice)
		{
			firstArg = Arrays.copyOf(bytesOfLong(x), Long.SIZE/8 * 3);// Gate 1 ([x]a & [x]b)
			System.arraycopy(allOnes, 0, secondArg, Long.SIZE/8 * 2, Long.SIZE/8); //To make carry = 1 (1's ^ 0's)
		}
		else
		{
			secondArg = Arrays.copyOf(bytesOfLong(x), Long.SIZE/8 * 3);
		}
		//Blocks two and three of Arg 1
		System.arraycopy(bytesOfLong(x), 0, firstArg, Long.SIZE/8, Long.SIZE/8); //Gate 2: For carry = 0 
		System.arraycopy(bytesOfLong(x), 0, firstArg, Long.SIZE/8 * 2, Long.SIZE/8); //Gate 2: For carry = 1 

		//Aquire requisite AND randomness
		byte[] uVals = new byte[3*Long.SIZE/8], vVals = new byte[3*Long.SIZE/8], uvVals = new byte[3*Long.SIZE/8];
		for (int j = 0; j < Long.SIZE/8*3; j++)
		{ byte[] vals = tiAnds.poll(); uVals[j] = vals[0]; vVals[j] = vals[1]; uvVals[j] = vals[2]; }

		//result = {Gate 1 result} | {Gate 2 result w/c=0} | {Gate 2 result w/c=1}
		byte[] result = NetworkUtils.multipartyAnd(in, out, firstArg, secondArg, uVals, vVals, uvVals, alice);

		//Invert all values (only one party) for NAND of Inverted values --> OR gate
		if (alice)
			for (int k = 0; k < result.length; k++) result[k] ^= 0xFF; 

		firstArg = new byte[2*Long.SIZE/8];
		secondArg = new byte[2*Long.SIZE/8];
		System.arraycopy(result, 0, firstArg, 0, Long.SIZE/8);
		System.arraycopy(result, 0, firstArg, Long.SIZE/8, Long.SIZE/8);
		System.arraycopy(result, Long.SIZE/8, secondArg, 0, Long.SIZE/8);
		System.arraycopy(result, 2*Long.SIZE/8, secondArg, Long.SIZE/8, Long.SIZE/8);

		//Acquire requisite AND randomness
		uVals = new byte[2*Long.SIZE/8]; vVals = new byte[2*Long.SIZE/8]; uvVals = new byte[2*Long.SIZE/8];
		for (int j = 0; j < Long.SIZE/8*2; j++)	{byte[] vals = tiAnds.poll();	uVals[j] = vals[0];	vVals[j] = vals[1];	uvVals[j] = vals[2];}

		//Gate 3
		//Result = {Gate 1 OR Gate 2(c=0)} | {Gate 1 OR Gate 2(c=1)}
		result = NetworkUtils.multipartyAnd(in, out, firstArg, secondArg, uVals, vVals, uvVals, alice);
		if (alice)
			for (int k = 0; k < result.length; k++) result[k] ^= 0xFF; 
		xForCarry0 = x;
		xForCarry1 = (alice ? x : ~x); //All values will be inverted if the carry ins are 1.
		carriesFor0 = longFromBytes(Arrays.copyOfRange(result, 0, Long.SIZE/8));
		carriesFor1 = longFromBytes(Arrays.copyOfRange(result, Long.SIZE/8, Long.SIZE/8*2));

		//--- Setup complete, rounds = 2, begin merging ---

		//Initialize blocks at one bit each
		LinkedList<Block> blocks = new LinkedList<Block>();
		blocks.add(new Block((byte)(xForCarry0 & 1), (byte)(xForCarry1 & 1), (byte)(carriesFor0 & 1), (byte)(carriesFor1 & 1), true)); //First bit has guaranteed carry in = 0, e.g. special
		for (int j = 1; j < Long.SIZE; j++) 
			blocks.add(new Block((byte)((xForCarry0>>j) & 1), (byte)((xForCarry1>>j) & 1), (byte)((carriesFor0>>j) & 1), (byte)((carriesFor1>>j) & 1), false));

		while (blocks.size() > 1)
		{
			firstArg = new byte[0];
			secondArg = new byte[0];
			LinkedList<Integer> resultLength = new LinkedList<Integer>(); //To track how much of the result is for each block (because first block is different size)
			for (int j = 0; j < blocks.size() / 2; j++)
			{
				byte[] p1 = blocks.get(2*j + 1).getArg1ToMerge(blocks.get(2*j));
				byte[] p2 = blocks.get(2*j + 1).getArg2ToMerge(blocks.get(2*j), alice);
				int oldLen = firstArg.length;
				firstArg = Arrays.copyOf(firstArg, oldLen + p1.length); //Repeatedly reallocate space, could be more efficient if reallocate only once
				System.arraycopy(p1, 0, firstArg, oldLen, p1.length);
				secondArg = Arrays.copyOf(secondArg, oldLen + p2.length);
				System.arraycopy(p2, 0, secondArg, oldLen, p2.length);
				resultLength.add(p1.length);
			}
			uVals = new byte[firstArg.length]; vVals = new byte[firstArg.length]; uvVals = new byte[firstArg.length];
			for (int i = 0; i < uVals.length; i++)
			{ 
				byte[] r = tiAnds.poll(); 
				for (int p = 0; p < 8; p++)
				{
					uVals[i] = (byte) ((r[0] >> p) & 1); 
					vVals[i] = (byte) ((r[1] >> p) & 1); 
					uvVals[i] = (byte)((r[2] >> p) & 1); 
					i++;
					if (i >= uVals.length) break;
				}
			}
			result = NetworkUtils.multipartyAnd(in, out, firstArg, secondArg, uVals, vVals, uvVals, alice); //1 round, log_2{64} occurrences = 6 rounds?

			int index = result.length;
			for (int j = blocks.size() - 1; j > 0; j-= 2)
			{
				int length = resultLength.removeLast();
				blocks.get(j).merge(blocks.get(j-1), Arrays.copyOfRange(result, index - length, index)); //Merge with previous block
				index -= length; //backtrack index, primarily because the first block has a different input size to the AND
				blocks.remove(j); //Remove block
			}
		}

		return blocks.get(0).forCarryInZero;
	}


	public static long[] multipartyValueSelectAlice(InputStream in, OutputStream out, long[] values, int num, TrustedInitializer ted) throws IOException
	{
		byte[] arg2 = new byte[values.length * Long.SIZE/8 * num];
		for (int i = 0; i < values.length; i++)
		{
			System.arraycopy(bytesOfLong(values[i]), 0, arg2, i*Long.SIZE/8, Long.SIZE/8);
		}
		for (int j = 0; j < num; j++)
		{
			System.arraycopy(arg2, 0, arg2, j*values.length*Long.SIZE/8, values.length*Long.SIZE/8);
		}
		byte[] uVals = new byte[values.length * Long.SIZE/8 * num], 
				vVals = new byte[values.length * Long.SIZE/8 * num], 
				uvVals = new byte[values.length * Long.SIZE/8 * num];
		for (int j = 0; j < uVals.length; j++)	
		{
			byte[] vals = ted.getAvailableAnds().poll();	
			uVals[j] = vals[0];	vVals[j] = vals[1];	uvVals[j] = vals[2];
		}
	//	System.out.println(arg2.length);
		byte[] result = NetworkUtils.multipartyAnd(in, out, new byte[values.length * Long.SIZE/8 * num], arg2, uVals, vVals, uvVals, true);
		long[] shares = new long[num];
		for (int j = 0; j < num; j++)
		{
			shares[j] = 0;
			for (int i = 0; i < values.length; i++)
			{
				shares[j] ^= longFromBytes(Arrays.copyOfRange(result, j*values.length*Long.SIZE/8 + i*Long.SIZE/8, 
						j*values.length*Long.SIZE/8 + (i+1)*Long.SIZE/8));
			}
		}
		
		return shares;
	}

	public static long[] multipartyValueSelectBob(InputStream in, OutputStream out, byte[][] selectionVectors, TrustedInitializer ted) throws IOException
	{
		int num = selectionVectors.length;
		int l = selectionVectors[0].length;
		byte[] arg1 = new byte[num * l * Long.SIZE/8];
		for (int i = 0; i < num; i++)
		{
			for (int j = 0; j < l; j++)
				Arrays.fill(arg1, i*l*Long.SIZE/8 + j*Long.SIZE/8, i*l*Long.SIZE/8 + (j+1)*Long.SIZE/8, selectionVectors[i][j]);
		}
		byte[] uVals = new byte[arg1.length], 
				vVals = new byte[arg1.length], 
				uvVals = new byte[arg1.length];
		for (int j = 0; j < uVals.length; j++)	
		{
			byte[] vals = ted.getAvailableAnds().poll();	
			uVals[j] = vals[0];	vVals[j] = vals[1];	uvVals[j] = vals[2];
		}
		//System.out.println(arg1.length);
		byte[] result = NetworkUtils.multipartyAnd(in, out, arg1, new byte[l * Long.SIZE/8 * num], uVals, vVals, uvVals, false);
		long[] shares = new long[num];
		for (int j = 0; j < num; j++)
		{
			shares[j] = 0;
			for (int i = 0; i < l; i++)
			{
				shares[j] ^= longFromBytes(Arrays.copyOfRange(result, j*l*Long.SIZE/8 + i*Long.SIZE/8, 
						j*l*Long.SIZE/8 + (i+1)*Long.SIZE/8));
			}
		}
		return shares;
	}

}


