import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import java.util.*;

public class SVMServer
{
	public static final String MODEL_FILE = "SVM_04_29.txt";
	public static final int SERVER_PORT = 1234;
	
	private List<List<BigDecimal>> weights;
	private List<BigDecimal> intercepts;
	private List<Boolean> evaluations;
	
	public static void main(String[] args) throws Exception
	{	
		try(ServerSocket serverSocket = new ServerSocket(SERVER_PORT))
		{
			
			while(true)
			{
				Socket clientSocket = serverSocket.accept();
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				SVMServer server = new SVMServer(MODEL_FILE);
				server.sendEvaluations(out, in.readLine());
				
				clientSocket.close();
				out.close();
				in.close();
			}
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	public SVMServer(String fileName) throws FileNotFoundException
	{
		weights = new ArrayList<List<BigDecimal>>();
		intercepts = new ArrayList<BigDecimal>();
		evaluations = new ArrayList<Boolean>();
		
		getModel(fileName);
	}
	
	public void sendEvaluations(PrintWriter out, String clientInput)
	{
		List<BigDecimal> inputList = StringUtils.stringToDoubleArray(clientInput);
		
		for(int i = 0; i < weights.size(); i++)
		{
			Boolean eval = SVM.evaluate(inputList, weights.get(i), intercepts.get(i));
			evaluations.add(eval);
		}
		
		out.println(evaluations.toString());
	}
	
	private void getModel(String filename) throws FileNotFoundException
	{
		Scanner fileScanner = new Scanner(new File(filename));
		
		while(fileScanner.hasNextLine())
		{
			List<BigDecimal> personality = StringUtils.extractDecimals(fileScanner.nextLine());
			if(personality.isEmpty()) continue;
			weights.add(personality);
		}
		
		for(List<BigDecimal> personality : weights)
		{
			int lastElementIndex = personality.size() - 1;
			intercepts.add(personality.remove(lastElementIndex));
		}
	}
}
