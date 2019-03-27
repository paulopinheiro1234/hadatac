package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileTemplate {

    public static String GENERIC = "GENERIC";
    public static String CHEAR = "CHEAR";

    public static List<String> TEMPLATETYPES = new ArrayList<String>(Arrays.asList(GENERIC, CHEAR));

    public String name;
    public String template;
    public String path;

    public static FileTemplate DPL_GEN = new FileTemplate("DPL", "GENERIC", "public/example/data/templates/generic/DPL.xlsx");
    public static FileTemplate STD_GEN = new FileTemplate("STD", "GENERIC", "public/example/data/templates/generic/STD.xlsx");
    public static FileTemplate SSD_GEN = new FileTemplate("SSD", "GENERIC", "public/example/data/templates/generic/SSD.xlsx");
    public static FileTemplate SDD_GEN = new FileTemplate("SDD", "GENERIC", "public/example/data/templates/generic/SDD.xlsx");
    public static FileTemplate OAS_GEN = new FileTemplate("OAS", "GENERIC", "public/example/data/templates/generic/OAS.csv");
    public static FileTemplate DPL_CHEAR = new FileTemplate("DPL", "CHEAR", "public/example/data/templates/chear/DPL.xlsx");
    public static FileTemplate STD_CHEAR = new FileTemplate("STD", "CHEAR", "public/example/data/templates/chear/STD.xlsx");
    public static FileTemplate SSD_CHEAR = new FileTemplate("SSD", "CHEAR", "public/example/data/templates/chear/SSD.xlsx");
    public static FileTemplate SDD_CHEAR = new FileTemplate("SDD", "CHEAR", "public/example/data/templates/chear/SDD.xlsx");
    public static FileTemplate OAS_CHEAR = new FileTemplate("OAS", "CHEAR", "public/example/data/templates/chear/OAS.csv");

    public static List<FileTemplate> FILETEMPLATES = 
    		new ArrayList<FileTemplate>(Arrays.asList(DPL_GEN, STD_GEN, SSD_GEN, SDD_GEN, OAS_GEN, 
    					DPL_CHEAR, STD_CHEAR, SSD_CHEAR, SDD_CHEAR, OAS_CHEAR));

    public FileTemplate(String name, String template, String path) {
    	this.name = name;
    	this.template = template;
    	this.path = path;
    }
    
    public static FileTemplate find(String name, String template) {
    	for (FileTemplate tp : FILETEMPLATES) {
    		if (tp.getName().equals(name) && tp.getTemplate().equals(template)) {
    			return tp;
    		}
    	}
    	return null;
    }
    
    public String getName() {
    	return name;
    }

    public void setName(String name) {
    	this.name = name;
    }
    
    public String getTemplate() {
    	return template;
    }

    public void setTemplate(String template) {
    	this.template = template;
    }
    
    public String getPath() {
    	return path;
    }

    public void setPath(String path) {
    	this.path = path;
    }
        
}
