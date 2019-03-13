package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileType {

    public String name;
    public String longName;
    public String path;
    public String suffix;

    public static FileType DPL = new FileType("DPL", "Deployment Specification", "public/example/data/templates/DPL-Template.xlsx", ".xlsx");
    public static FileType STD = new FileType("STD", "Study Specification", "public/example/data/templates/STD.csv", ".csv");
    public static FileType SSD = new FileType("SSD", "Semantic Study Design", "public/example/data/templates/SSD-Template.xlsx", ".xlsx");
    public static FileType SDD = new FileType("SDD", "Semantic Data Dictionary", "public/example/data/templates/SDD-Template.xlsx", ".xlsx");
    public static FileType OAS = new FileType("OAS", "Object Access Specification", "public/example/data/templates/OAS-Template.xlsx", ".xlsx");

    public static List<FileType> FILETYPES = new ArrayList<FileType>(Arrays.asList(DPL, STD, SSD, SDD, OAS));

    public static FileType find(String name) {
    	for (FileType ft : FILETYPES) {
    		if (ft.getName().equals(name)) {
    			return ft;
    		}
    	}
    	return null;
    }
    
    public FileType(String name, String longName, String path, String suffix) {
    	this.name = name;
    	this.longName = longName;
    	this.path = path;
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
    
    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }
        
    public String getSuffix() {
	return suffix;
    }

    public void setSuffix(String suffix) {
	this.suffix = suffix;
    }
        
}
