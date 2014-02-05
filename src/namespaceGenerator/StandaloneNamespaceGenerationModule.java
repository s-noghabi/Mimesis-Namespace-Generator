package namespaceGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
/*
 * This is the class containing the main method for running the program.
 */
public class StandaloneNamespaceGenerationModule {
	private static String configFile;
	
	public static void main(String[] args) throws ConfigurationException {
		if (args.length==0){
			configFile= "../conf/mimesis.properties";
		}
		configFile = "../conf/"+args[0];
		PropertiesConfiguration config = new PropertiesConfiguration(configFile);
		MimesisParameters params = new MimesisParameters(config);
	    
		try {
			
			Random r = new Random();
			
			//Create the File System
		    HierarchicalFileSystemGenerator fsg =  new HierarchicalFileSystemGenerator(r, params);
		    fsg.generateFileSystem();
		    System.out.println("File system generated; last stamp in namespace: " + fsg.getLastStampInSnapshot());
		    
		    //Print the File System
		    BufferedWriter sysOut = new BufferedWriter(new OutputStreamWriter(System.out));
		    fsg.print(sysOut);
		    sysOut.flush();
		    
		    //Get the statistics
		    if (params.printNamespaceStats) {
		    	System.out.println("Printing file system stats to *.cdf files.");
		    	fsg.printStats();
		    	System.out.println("finished!");
		    }
		    sysOut.flush();
		    
			sysOut.close();
			
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}
}

