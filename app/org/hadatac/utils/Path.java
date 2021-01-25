package org.hadatac.utils;

import org.hadatac.console.controllers.sandbox.Sandbox;

public class Path {
    
    private String label;
    private String field;
    private String configFilePath;
    
    public Path(String label, String configFilePath, String field) {
        this.label = label;
        this.configFilePath = configFilePath;
        this.field = field;
    }
    
    public String getLabel() {
        return label;
    }
    
    public String getField() {
        return field;
    }
    
    public String getConfigFilePath() {
        return configFilePath;
    }
    
    public String getPath() {
        //String path = ConfigProp.getPropertyValue(getConfigFilePath(), getField());
        String path = "";
	if(getLabel().equals("UNPROC")) {
		return ConfigProp.getPathUnproc();
	}
	else if(getLabel().equals("PROC")) { 
		return ConfigProp.getPathProc();
	}
	else if(getLabel().equals("DOWNLOAD")) {
		return ConfigProp.getPathWorking();
	}
	else if(getLabel().equals("WORKING")) {
		return ConfigProp.getPathWorking();
	}
	else {
        	return path;
	}
    }
}
