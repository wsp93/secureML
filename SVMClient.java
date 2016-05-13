import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SVMClient
{
	public static final String hostName = "localhost";
	public static final int portNumber = 1234;
	public static final String inputFile = "testInput.txt";
	
	private List<BigDecimal> features;

	public static void main(String[] args)
	{
		try
		{
			SVMClient client = new SVMClient();
			Socket socket = new Socket(hostName, portNumber);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			client.runProtocol(in, out);
			
			in.close();
			out.close();
			socket.close();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

	public SVMClient() throws FileNotFoundException
	{
		readInputFile(inputFile);
	}

	public void runProtocol(BufferedReader in, PrintWriter out) throws IOException
	{
		out.println(features.toString());
		System.out.println(in.readLine());
	}

	private void readInputFile(String fileName) throws FileNotFoundException
	{
		Scanner fileScanner = new Scanner(new File(fileName));
		features = new ArrayList<BigDecimal>();
		
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
	}
}
