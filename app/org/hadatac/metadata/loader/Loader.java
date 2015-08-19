package org.hadatac.metadata.loader;

import java.util.HashMap;
import java.util.Map;

import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.NameSpaces;
import org.hadatac.metadata.loader.SpreadsheetProcessing;

public class Loader {

	private static boolean clean = false;
	private static boolean showHelp = false;
	private static boolean loadOntology = false;
	private static boolean loadXls = false;
    private static boolean verbose = false;
	private static Map<String, String> argsMap = new HashMap<String, String>();
	private static MetadataContext metadata = null;
		
	public static MetadataContext getMetadataContext() {
		return metadata;
	}
	
	private static void printHelp() {
		System.out.println("Usage: hasnetoloader [options] -u username -p password -k knowldgeBaseURL[-i inputXLS]");
		System.out.println("       -i inputXLS: generate ttl and load it into knowledge base;");
		System.out.println("                    inputXLS parsing warnings and errors are printed as they");
		System.out.println("                    are identified, if any");
		System.out.println("       -c : clears knowldge base");
		System.out.println("       -o : loads associated ontologies");
		System.out.println("       -v : verbose mode on, including curl's outputs");
		System.out.println("       -h : this help");
		System.out.println("");
		System.out.println("Example: hasnetoloader -c -o -u user -p abcde1234 -k http://localhost/slor4 -i myspreadsheet.xlsx ");
		System.out.println("         this command will clear the knowledgbase, load associated ontologies, convert myspreadsheet.xlsx");
		System.out.println("         into turtle (ttl), and load the turtle into the knowledgebase.");
	}
	
	private static boolean parseArguments(String[] args) {
		if (args.length == 0) {
			printHelp();
			return false;
		}
		for (int i=0; i < args.length; i++) {
			args[i] = args[i].toLowerCase().trim();
			if ( (!args[i].equals("-i")) &&
				 (!args[i].equals("-c")) &&
				 (!args[i].equals("-o")) &&
				 (!args[i].equals("-v")) &&
				 (!args[i].equals("-h")) &&
				 (!args[i].equals("-u")) &&
				 (!args[i].equals("-p")) &&
				 (!args[i].equals("-k")) ) {
				System.out.println("Argument " + args[i] + " is invalid.\n");
				return false;
			}
			if (args[i].equals("-c")) {
				clean = true;
			}
			if (args[i].equals("-o")) {
				loadOntology = true;
			}
			if (args[i].equals("-i")) {
				loadXls = true;
			}
			if (args[i].equals("-v")) {
				verbose = true;
			}
			if (args[i].equals("-h")) {
				showHelp = true;
			}
			if ( (args[i].equals("-i")) ||
			     (args[i].equals("-u")) ||
				 (args[i].equals("-p")) ||
				 (args[i].equals("-k")) ) {
				if (i == (args.length - 1)) {
					System.out.println("Argument " + args[i] + " misses associated value.\n");
					return false;
				} 
				if (args[i + 1].startsWith("-")) {
					System.out.println("Argument " + args[i] + " misses associated value.\n");					
					return false;
				}
				argsMap.put(args[i], args[i + 1]);
				i++;
			}
		}
		return true;
		
	}
	
	private static boolean validArguments (String[] args) {
		if (!parseArguments(args)) {
			return false;
		}
		if (argsMap.get("-u") == null) {
			System.out.println("Argument -u is missing.\n");
			return false;
		} 
		if (argsMap.get("-p") == null) {
			System.out.println("Argument -p is missing.\n");
			return false;
		} 
		if (argsMap.get("-k") == null) {
			System.out.println("Argument -k is missing.\n");
			return false;
		} 
		return true;
	}
	
	public static void main(String[] args) {
		if (validArguments(args))  {

			if (showHelp) {
				printHelp();
			} else {
			
				NameSpaces.getInstance();
				
				if (verbose) {
					System.out.println("Verbose mode is on");
				}
				
				metadata = new MetadataContext(argsMap.get("-u"), argsMap.get("-p"), argsMap.get("-k"), verbose);

				if (clean) {
					System.out.println("Executing CLEAN");			
					metadata.clean();
					System.out.println("");			
				}

				if (loadOntology) {
					System.out.println("Executing LOADONTOLOGY");						
					metadata.loadOntologies();
					System.out.println("");			
				}
		
				if (loadXls) {
					System.out.println("Executing LOADXLS");						
					String ttl = "";
					ttl = SpreadsheetProcessing.generateTTL(argsMap.get("-i"));
					//System.out.println(ttl);			
				}
			}
		}
	}
}
