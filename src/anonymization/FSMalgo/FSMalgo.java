import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
Class used to test different FSM algorithm out of the main class
*/


public class FSMalgo {


	public static void append(File aFile, String content) {
		try {
			PrintStream p = new PrintStream(new BufferedOutputStream(new FileOutputStream(aFile, true)));
			p.println(content);
			p.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(aFile);
		}
	}


	public static void main(String [] args) {
		//append(new File("somet/gspanInput"), "t # 0");
		// final Process p;
  //   	String line;
  //   	String path;
  //   	String[] params = new String [5];

  //  		params[0] = "java";
  //       params[1] = "-jar gsd.jar";
  //   	params[2] = "-d Chemical_340";
  //   	params[3] = "-r somet/output";
  //       params[4] = "-s 3";

    	try {
        Process p = Runtime.getRuntime().exec("java -jar gsd.jar -d somet/gspanInput -r somet/output -s 3");
        int result = p.waitFor();
        if (result != 0) {
           System.out.println("Process failed with status: " + result);
        }
		}catch (Exception e){ 
                System.out.println(" procccess not read"+e);
        }
	}
		

}