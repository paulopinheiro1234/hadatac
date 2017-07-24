package org.hadatac.data.loader.util;

import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.args4j.ExampleMode.ALL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class Arguments {
	
    @Option(name="-i",required=true,usage="input file",metaVar="input_file")
	private String inputPath;
    
    @Option(name="-o",required=false,usage="output path for normalized csv",metaVar="output_path")
	private String outputPath = "tmp/";
    
    @Option(name="-t",required=false,usage="temporary path",metaVar="temp_path")
	private String tempPath = "tmp/";
	
    @Option(name="-l",required=false,usage="log path",metaVar="log_path")
	private String logPath = "logs/";
    
    @Option(name="-m",required=true,usage="input type",metaVar="input_type")
	private String inputType;
    
    @Option(name="-s",required=false,usage="what to parse: both/data/metadata",metaVar="what_to_parse")
	private String steps = "both";
    
    @Option(name="-v",required=false,usage="verbose mode")
	private boolean verbose = false;
    
    @Option(name="-pv",required=false,usage="preamble validation only mode")
	private boolean pv = false;
    
    @Argument
	private List<String> arguments = new ArrayList<String>();
    
    public void parse(String []args) throws Exception {
	CmdLineParser parser = new CmdLineParser(this);
		
	parser.setUsageWidth(80);
	
	try {
	    parser.parseArgument(args);
	    
	    /*
	      if (arguments.isEmpty()) {
	      throw new CmdLineException(parser,"No argument is given");
	      }*/
	} catch (CmdLineException e) {
	    System.err.println(e.getMessage());
	    System.err.println("java SampleMain [options...] arguments...");
	    parser.printUsage(System.err);
	    System.err.println();
	    System.err.println("  Example: java SampleMain"+parser.printExample(ALL));
	    
	    throw new Exception();
	}
	
	//System.out.println(out);
	/*
	  System.out.println("-i was " + inputPath);
	  System.out.println("-o was " + outputPath);
	  System.out.println("-t was " + tempPath);
	  System.out.println("-l was " + logPath);
	  System.out.println("-s was " + steps);
	  System.out.println("-pv was " + pv);
	  
	  System.out.println("other arguments are:");
	  for( String s : arguments )
	  System.out.println(s);
        */
    }
    
    public String getLogPath() {
	return logPath;
    }
    
    public void setLogPath(String logPath) {
	this.logPath = logPath;
    }
    
    public String getInputPath() {
	return inputPath;
    }
    
    public void setInputPath(String inputPath) {
	this.inputPath = inputPath;
    }
    
    public String getOutputPath() {
	return outputPath;
    }
    
    public void setOutputPath(String outputPath) {
	this.outputPath = outputPath;
    }
    
    public String getTempPath() {
	return tempPath;
    }
    
    public void setTempPath(String tempPath) {
	this.tempPath = tempPath;
    }
    
    public boolean isVerbose() {
	return verbose;
    }
    
    public void setVerbose(boolean verbose) {
	this.verbose = verbose;
    }
    
    public boolean isPv() {
	return pv;
    }
    
    public void setPv(boolean pv) {
	this.pv = pv;
    }
    
    public String getInputType() {
	return inputType;
    }
    
    public void setInputType(String inputType) {
	this.inputType = inputType;
    }
}
