package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileType {

    public String name;
    public String longName;
    public String suffix;

    public static FileType DPL = new FileType("DPL", "Deployment Specification", ".xlsx");
    public static FileType STD = new FileType("STD", "Study Specification", ".xlsx");
    public static FileType SSD = new FileType("SSD", "Semantic Study Design", ".xlsx");
    public static FileType SDD = new FileType("SDD", "Semantic Data Dictionary", ".xlsx");
    public static FileType STR = new FileType("STR", "Stream Specification", ".csv");

    public static List<FileType> FILETYPES = new ArrayList<FileType>(Arrays.asList(DPL, STD, SSD, SDD, STR));

    public static FileType find(String name) {
    	for (FileType ft : FILETYPES) {
    		if (ft.getName().equals(name)) {
    			return ft;
    		}
    	}
    	return null;
    }
    
    public FileType(String name, String longName, String suffix) {
    	this.name = name;
    	this.longName = longName;
    	this.suffix = suffix;
    }
    
    public String getName() {
    	return name;
    }

    public void setName(String name) {
    	this.name = name;
    }
    
    public String getLongName() {
    	return longName;
    }

    public void setLongName(String longName) {
    	this.longName = longName;
    }
    
    public String getSuffix() {
    	return suffix;
    }

    public void setSuffix(String suffix) {
    	this.suffix = suffix;
    }
        
}
