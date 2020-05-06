package org.hadatac.data.loader;

import java.io.File;
import java.io.IOException;

import org.hadatac.data.loader.util.Arguments;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.utils.Feedback;

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
		files.setCCSVFile(inputFile, inputFile.getName());
		
		files.openFile("log", "w");
		files.writeln("log", "[START] " + arguments.getInputPath() + " generating measurements.");
		
		/*
		if (arguments.getInputType().equals("CCSV")) {
		    Parser parser = new Parser();
			if (arguments.isPv()) {
				parser.validate(Feedback.COMMANDLINE, files);
			} else {
				parser.validate(Feedback.COMMANDLINE, files);
				parser.index(Feedback.COMMANDLINE);
			}
			}*/
		
		files.writeln("log", "[END] " + arguments.getInputPath() + " generating measurements.");
		files.closeFile("log", "w");
		//System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] end of file " + ccsvFile.getName());
	}
}
