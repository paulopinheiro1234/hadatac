package org.hadatac.data.loader.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileFactory {

	File ccsvFile;
	File csvFile;
	File xcsvFile;
	File ttlFile;
	File logFile;
	
	FileReader ccsvFileReader;
	FileReader csvFileReader;
	
	FileWriter csvFileWriter;
	FileWriter xcsvFileWriter;
	FileWriter ttlFileWriter;
	FileWriter logFileWriter;
	
	BufferedReader ccsvBufferedReader;
	BufferedReader csvBufferedReader;
	
	BufferedWriter csvBufferedWriter;
	BufferedWriter xcsvBufferedWriter;
	BufferedWriter ttlBufferedWriter;
	BufferedWriter logBufferedWriter;
	
	Arguments arguments;
	
	String fileName;
	
	public FileFactory (Arguments arguments) {
		this.arguments = arguments;
	}
	
        public String getFileName() {
	    return fileName;
        }

	public void setCCSVFile(File file, String name) {
		ccsvFile = file;
		fileName = name;
	}
	public void setCSVFile(File file, String name) {
		csvFile = file;
		fileName = name;
	}
	
	public void openFile(String type, String mode) throws IOException {
		if (type.equals("ccsv")) {
			ccsvFileReader = new FileReader(ccsvFile);
			ccsvBufferedReader = new BufferedReader(ccsvFileReader);
		} else if (type.equals("csv")) {
		        //csvFile = new File(arguments.getTempPath() + fileName + ".temp.csv");
			if (mode.equals("w")) {
				csvFileWriter = new FileWriter(csvFile);
				csvBufferedWriter = new BufferedWriter(csvFileWriter);
			} else if (mode.equals("r")) {
				csvFileReader = new FileReader(csvFile);
				csvBufferedReader = new BufferedReader(csvFileReader);
			}
		} else if (type.equals("xcsv")) {
			xcsvFile = new File(arguments.getOutputPath() + fileName + ".xcsv");
			xcsvFileWriter = new FileWriter(xcsvFile);
			xcsvBufferedWriter = new BufferedWriter(xcsvFileWriter);
		} else if (type.equals("ttl")) {
			ttlFile = new File(arguments.getOutputPath() + fileName + ".ttl");
			ttlFileWriter = new FileWriter(ttlFile);
			ttlBufferedWriter = new BufferedWriter(ttlFileWriter);
		} else if (type.equals("log")) {
			logFile = new File(arguments.getLogPath() + "hadatac-loader-" + LocalDateTime.now().toLocalDate().format(DateTimeFormatter.ISO_DATE) + ".log");
			logFileWriter = new FileWriter(logFile);
			logBufferedWriter = new BufferedWriter(logFileWriter);
		}
	}
	
	public void closeFile(String type, String mode) throws IOException {
		if (type.equals("ccsv")) {
			ccsvBufferedReader.close();
			ccsvFileReader.close();
		} else if (type.equals("csv")) {
			csvFile = new File(arguments.getTempPath() + fileName + ".temp.csv");
			if (mode.equals("w")) {
				csvBufferedWriter.close();
				csvFileWriter.close();
			} else if (mode.equals("r")) {
				csvBufferedReader.close();
				csvFileReader.close();
			}
		} else if (type.equals("xcsv")) {
			xcsvBufferedWriter.close();
			xcsvFileWriter.close();
		} else if (type.equals("ttl")) {
			ttlBufferedWriter.close();
			ttlFileWriter.close();
		} else if (type.equals("log")) {
			logBufferedWriter.close();
			logFileWriter.close();
		}
	}
	
	public BufferedReader getReader(String type) {
		if (type.equals("csv")) {
			return csvBufferedReader;
		} else if (type.equals("ccsv")) {
			return ccsvBufferedReader;
		} else {
			return null;
		}
	}
	
	public BufferedWriter getWriter(String type) {
		if (type.equals("csv")) {
			return csvBufferedWriter;
		} else if (type.equals("xcsv")) {
			return xcsvBufferedWriter;
		} else if (type.equals("ttl")) {
			return ttlBufferedWriter;
		} else if (type.equals("log")) {
			return logBufferedWriter;
		} else {
			return null;
		}
	}
	
	public void write(String type, String str) throws IOException {
		if (type.equals("csv")) {
			csvBufferedWriter.write(str);
		} else if (type.equals("xcsv")) {
			xcsvBufferedWriter.write(str);
		} else if (type.equals("ttl")) {
			ttlBufferedWriter.write(str);
		} else if (type.equals("log")) {
			logBufferedWriter.write(LocalDateTime.now().toString() + " - " + str);
		}
	}
	
	public void writeln(String type, String str) throws IOException {
		if (type.equals("csv")) {
			csvBufferedWriter.write(str + "\n");
		} else if (type.equals("xcsv")) {
			xcsvBufferedWriter.write(str + "\n");
		} else if (type.equals("ttl")) {
			ttlBufferedWriter.write(str + "\n");
		} else if (type.equals("log")) {
			logBufferedWriter.write(LocalDateTime.now().toString() + " - " + str + "\n");
		}
	}
}
