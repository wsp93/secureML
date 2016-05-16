package secureml.securesvm;
///2015 UWT CDS project: privacy preserving machine learning classification 
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class TrustedInitializer {

	//Packet form {session id, Alice = 0 | Bob = 1} | {request type, modulus length, modulus, vector length (if needed)/number of ANDs[4bytes]}
	private static final int RANDOM_DOT_PRODUCT = 1;
	private static final int RANDOM_MULTIPLICATION = 2;
	private static final int RANDOM_BITWISE_AND = 3;

	public static final int myPort = 1235;

	Queue<byte[]> andShareValues;
	Queue<BigInteger[]> dotProductShareValuesVec;
	Queue<BigInteger> dotProductShareValuesB;
	Queue<BigInteger[]> multiplicationShareValues;

	private Random prng;

	public static void main(String[] args) {
		new TrustedInitializer().serveValues();
	}

	TrustedInitializer()
	{

	}

	//********************Functions for Alice and Bob to interface with TI ********************//
	public boolean loadAndsFromFile(String fileName)
	{
		if (andShareValues == null)
			andShareValues = new LinkedList<byte[]>();
			return true;
	}
	public boolean loadDotsFromFile(String fileName)
	{
		if (dotProductShareValuesVec == null)
		{
			dotProductShareValuesVec = new LinkedList<BigInteger[]>();
			dotProductShareValuesB = new LinkedList<BigInteger>();
		}
		return true;
	}
	public boolean loadMultiplicationsFromFile(String fileName)
	{
		if (multiplicationShareValues == null)
			multiplicationShareValues = new LinkedList<BigInteger[]>();
			return true;
	}
	public boolean retrieveAnds(Socket TI, int numAnds)
	{
		if (andShareValues == null)
		{
			andShareValues = new LinkedList<byte[]>();
		}

		try {
			OutputStream tiOut = TI.getOutputStream();
			InputStream tiIn = TI.getInputStream();
				tiOut.write(RANDOM_BITWISE_AND);
				tiOut.write(numAnds);
				tiOut.write(numAnds>>8);
				tiOut.write(numAnds>>16);
				tiOut.write(numAnds>>24);
				tiOut.flush();
				for (int j = 0; j < numAnds; j++)
				{
					byte[] data = new byte[3];
					for (int q = 0; q < 3; q++)
					{
						int z = tiIn.read();
						if (z == -1)throw new Exception("Received improper response from TI");
						data[q] = (byte)z;
					}
					andShareValues.add(data);
				}
		} catch(Exception e)
		{
			System.out.println("Error retrieving AND values: " + e.getClass() + "\t"+ e.getMessage());
			return false;
		}
		return true;
	}

	public boolean retrieveMultiplications(Socket TI, BigInteger modulus, int numMults)
	{
		if (multiplicationShareValues == null)
			multiplicationShareValues = new LinkedList<BigInteger[]>();
			try
			{
				OutputStream tiOut = TI.getOutputStream();
				InputStream tiIn = TI.getInputStream();
				tiOut.write(RANDOM_MULTIPLICATION);
				NetworkUtils.sendBigInteger(tiOut, modulus);
				for (int j = 0; j < numMults; j++)
				{
					tiOut.flush();
					BigInteger[] vals = new BigInteger[3];
					if ((vals[0] = NetworkUtils.receiveBigInteger(tiIn)) == null)
						throw new Exception("Error receiving precomputed product");
					if ((vals[1] = NetworkUtils.receiveBigInteger(tiIn)) == null)
						throw new Exception("Error receiving precomputed product");
					if ((vals[2] = NetworkUtils.receiveBigInteger(tiIn)) == null)
						throw new Exception("Error receiving precomputed product");
				}
				return true;
			} catch (Exception e)
			{
				System.out.println("Failure: " + e.getMessage());
				return false;
			}
	}

	public boolean retrieveDotProducts(Socket TI, BigInteger modulus, int numDots, int vectorDimension, boolean alice)
	{
		if (dotProductShareValuesVec == null)
		{
			dotProductShareValuesVec = new LinkedList<BigInteger[]>();
			dotProductShareValuesB = new LinkedList<BigInteger>();
		}
		try
		{
			OutputStream tiOut = TI.getOutputStream();
			InputStream tiIn = TI.getInputStream();
			tiOut.write(RANDOM_DOT_PRODUCT);
			NetworkUtils.sendBigInteger(tiOut, modulus);
			tiOut.write(vectorDimension);
			tiOut.write(vectorDimension>>8);
			tiOut.write(vectorDimension>>16);
			tiOut.write(vectorDimension>>24);
			tiOut.write(numDots);
			tiOut.write(numDots>>8);
			tiOut.write(numDots>>16);
			tiOut.write(numDots>>24);
			tiOut.flush();

			for (int j = 0; j < numDots; j++)
			{
				BigInteger[] x = new BigInteger[vectorDimension];
				if (!NetworkUtils.receiveVector(tiIn, x)) 
					throw new Exception("Problem receiving dot product from TI");
				if (!alice)
				{
					BigInteger b = NetworkUtils.receiveBigInteger(tiIn);
					dotProductShareValuesB.add(b);
				}
				dotProductShareValuesVec.add(x);
			}
			return true;
		} catch (Exception e)
		{
			System.out.println("Failure: " + e.getMessage());
			return false;
		}
	}

	public Queue<byte[]> getAvailableAnds() { return andShareValues; }
	public Queue<BigInteger[]> getAvailableMults() { return multiplicationShareValues; }
	public Queue<BigInteger[]> getAvailableDotVecs() { return dotProductShareValuesVec; }
	public Queue<BigInteger> getAvailableDotSvals() { return dotProductShareValuesB; }

	//*********************Functions the TI uses to serve values for Alice and Bob *********************//
	public void serveValues()
	{
		//Give initialization data
		ServerSocket connections = null;
		prng = new Random();
		try {
			connections = new ServerSocket(myPort);
			while (true)
			{
				Socket firstParty = connections.accept();
				Socket secondParty = connections.accept();

				InputStream firstIn = firstParty.getInputStream();
				InputStream secondIn = secondParty.getInputStream();

				BigInteger sessionID1 = NetworkUtils.receiveBigInteger(firstIn);
				BigInteger sessionID2 = NetworkUtils.receiveBigInteger(secondIn);
				if (!sessionID1.equals(sessionID2))
				{
					System.out.println("Parties not claiming same session");
					continue;
				}
				OutputStream aliceOut = null;
				OutputStream bobOut = null;

				//Establish which channel is Alice/Bob, should always be Alice then Bob in this setup, but more robust JIC
				int firstName = firstIn.read();
				if (firstName == 0)
				{
					System.out.println("Alice connected first");
					aliceOut = firstParty.getOutputStream();
					bobOut = secondParty.getOutputStream();
				}
				else if (firstName == 1)
				{	
					System.out.println("Bob connected first");
					bobOut = firstParty.getOutputStream();
					aliceOut = secondParty.getOutputStream();
				}
				int secondName = secondIn.read();
				if (secondName == firstName)
				{
					System.out.println("Parties both claim to be same player");
					continue;
				}

				//----Begin parsing requests and returning randomized data----
				int firstByte, secondByte;
				//Read in next request, exit if different or end of stream
				while ((firstByte = firstIn.read()) == (secondByte = secondIn.read()) && firstByte != -1) 
				{
					if (firstByte == RANDOM_DOT_PRODUCT)
					{
						System.out.println("Furnishing a random dot product");

						BigInteger modulus1 = NetworkUtils.receiveBigInteger(firstIn);
						BigInteger modulus2 = NetworkUtils.receiveBigInteger(secondIn);
						if (!modulus1.equals(modulus2))
						{
							System.out.println("Client modulus mismatch");
							break;
						}

						//Read in vector length
						int a = firstIn.read(), b = firstIn.read(), c = firstIn.read(), d = firstIn.read();
						int l1 = a + (b<<8) + (c<<16) + (d<<24);
						a = secondIn.read();
						b = secondIn.read();
						c = secondIn.read();
						d = secondIn.read();
						int l2 = a + (b<<8) + (c<<16) + (d<<24);
						
						a = firstIn.read();
						b = firstIn.read();
						c = firstIn.read();
						d = firstIn.read();
						int count1 = a + (b<<8) + (c<<16) + (d<<24);
						
						a = secondIn.read();
						b = secondIn.read();
						c = secondIn.read();
						d = secondIn.read();
						int count2 = a + (b<<8) + (c<<16) + (d<<24);
						if (count1 != count2)
						{
							System.out.println("clients disagree on vector length");
							break;
						}
						System.out.println("Inputs received, beginning creation and distribution.");
						for (int j = 0; j < count1; j++)
							provideRandomizedDotProduct(aliceOut, bobOut, l1, modulus1);
						aliceOut.flush();
						bobOut.flush();
						System.out.println("Random dot product distribution complete.");
						//Request fulfilled
					} else if (firstByte == RANDOM_MULTIPLICATION)
					{
						System.out.println("Furnishing a random product");
						//Read in modulus byte length

						BigInteger modulus1 = NetworkUtils.receiveBigInteger(firstIn);
						BigInteger modulus2 = NetworkUtils.receiveBigInteger(secondIn);
						if (!modulus1.equals(modulus2))
						{
							System.out.println("Client modulus mismatch");
							break;
						}

						//method to provideRandomizedProduct
						provideRandomizedProduct(aliceOut, bobOut, modulus1);

						System.out.println("Random product distribution complete.");
					} else if (firstByte == RANDOM_BITWISE_AND)
					{
					//	System.out.println("Furnishing random binary products");
						int a = firstIn.read(), b = firstIn.read(), c = firstIn.read(), d = firstIn.read();
						int count1 = a + (b<<8) + (c<<16) + (d<<24);
						a = secondIn.read();
						b = secondIn.read();
						c = secondIn.read();
						d = secondIn.read();
						int count2 = a + (b<<8) + (c<<16) + (d<<24);
						if (count1 != count2)
						{System.out.println("Error!!");/*Something bad has happened*/}
						byte[] x_a = new byte[count1];
						byte[] x_b = new byte[count1];
						byte[] y_a = new byte[count1];
						byte[] y_b = new byte[count1];
						byte[] r = new byte[count1];
						byte[] x = new byte[count1];
						byte[] y = new byte[count1];
						byte[] prod = new byte[count1];
						prng.nextBytes(x_a);
						prng.nextBytes(x_b);
						prng.nextBytes(y_a);
						prng.nextBytes(y_b);
						prng.nextBytes(r);
						for (int j = 0; j < count1; j++)
						{
							x[j] = (byte) (x_a[j] ^ x_b[j]);
							y[j] = (byte) (y_a[j] ^ y_b[j]);
							prod[j] = (byte) (x[j] & y[j]);
							aliceOut.write(new byte[] {x_a[j], y_a[j], r[j]});
							bobOut.write(new byte[] {x_b[j], y_b[j], (byte)(r[j] ^ prod[j])});
						}
					}
				}
			}
		} catch (Exception e)
		{
			System.out.println("Epic failure: " + e.getMessage());
		} 

	}

	//Gives a false if protocol fails for some weird reason
	public boolean provideRandomizedDotProduct(OutputStream alice, OutputStream bob, int vectorDimension, BigInteger modulus)
	{
		try {
			BigInteger[] x = new BigInteger[vectorDimension];
			BigInteger[] y = new BigInteger[vectorDimension];
			BigInteger s = BigInteger.ZERO;
			for (int k = 0; k < vectorDimension; k++)
			{
				x[k] = MathUtils.getRandomModulo(modulus);
				y[k] = MathUtils.getRandomModulo(modulus);
				s = s.add( x[k].multiply(y[k]).mod(modulus) ).mod(modulus); //s = (s + (x*y) mod n) mod n
			}

			//Could be done more efficiently by sending values in the above loop, but clearer code this way
			NetworkUtils.sendVector(alice, x);
			NetworkUtils.sendVector(bob, y);

			//Send Bob s
			NetworkUtils.sendBigInteger(bob, s);
			return true;
		} catch (Exception e)
		{
			System.out.println("Failed in generation phase: " + e.getMessage() + "  " + e.getClass());
			return false;
		}
	}

	public boolean provideRandomizedProduct(OutputStream alice, OutputStream bob, BigInteger modulus)
	{
		try {
			BigInteger u_1 = MathUtils.getRandomModulo(modulus);
			BigInteger u_2 = MathUtils.getRandomModulo(modulus);
			BigInteger v_1 = MathUtils.getRandomModulo(modulus);
			BigInteger v_2 = MathUtils.getRandomModulo(modulus);
			BigInteger u = u_1.add(u_2).mod(modulus);
			BigInteger v = v_1.add(v_2).mod(modulus);
			BigInteger uv = u.multiply(v).mod(modulus);
			BigInteger r = MathUtils.getRandomModulo(modulus);

			NetworkUtils.sendBigInteger(alice, u_1);			
			NetworkUtils.sendBigInteger(alice, v_1);			
			NetworkUtils.sendBigInteger(alice, r);			

			NetworkUtils.sendBigInteger(bob, u_2);
			NetworkUtils.sendBigInteger(bob, v_2);
			NetworkUtils.sendBigInteger(bob, uv.subtract(r).mod(modulus));

			return true;
		} catch (Exception e)
		{
			System.out.println("Failed in generation phase: " + e.getMessage() + "  " + e.getClass());
			return false;
		}
	}

}

