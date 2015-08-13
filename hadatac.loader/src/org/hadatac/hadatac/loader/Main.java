package org.hadatac.hadatac.loader;

import java.io.File;
import java.io.IOException;

import org.hadatac.hadatac.loader.ccsv.CCSVParser;
import org.hadatac.hadatac.loader.util.Arguments;
import org.hadatac.hadatac.loader.util.FileFactory;

public class Main {
	
	static FileFactory files;
	
	public static void main(String[] args) throws IOException {
		File inputFile;
		
		Arguments arguments = new Arguments();
		try {
			arguments.parse(args);
		} catch (Exception e) {
			return;
		}
		
		inputFile = new File(arguments.getInputPath());
		files = new FileFactory(arguments);
		files.setFile(inputFile, inputFile.getName());
		
		files.openFile("log", "w");
		files.writeln("log", "[START] " + arguments.getInputPath() + " generating measurements.");
		
		if (arguments.getInputType().equals("CCSV")) {
			if (arguments.isPv()) {
				//validateCCSV(inputFile);
			} else {
				new CCSVParser().parse(files);
			}
		}
		
		files.writeln("log", "[END] " + arguments.getInputPath() + " generating measurements.");
		files.closeFile("log", "w");
		//System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] end of file " + ccsvFile.getName());
	}
}
