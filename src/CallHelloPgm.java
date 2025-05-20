import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CallHelloPgm {

	public static void main(String[] args) {
		 Process theProcess = null;
	      BufferedReader inStream = null;
	 
	      System.out.println("CallHelloPgm.main() invoked");
	 
	      // call the Hello class
	      try
	      {
	    	  Runtime.getRuntime().exec("java QIBMHello");
	          theProcess = Runtime.getRuntime().exec("java QIBMHello");
	      } catch(IOException e)
	      {
	         System.err.println("Error on exec() method");
	         e.printStackTrace();  
	      }
	      // read from the called program's standard output stream
	      try
	      {
	         inStream = new BufferedReader(
	                                new InputStreamReader( theProcess.getInputStream() ));  
	         System.out.println(inStream.readLine());
	      }
	      catch(IOException e)
	      {
	         System.err.println("Error on inStream.readLine()");
	         e.printStackTrace();  
	      }

	}

}
