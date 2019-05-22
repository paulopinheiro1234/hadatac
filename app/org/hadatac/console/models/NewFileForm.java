package org.hadatac.console.models;

public class NewFileForm {

    public String newType;
    public String newTemplate;
    public String newName;
    public String newDest;
    
    public NewFileForm () {
    }
    
    public String getNewType() {
    	return newType;
    }
    
    public void setNewType(String newType) {
    	this.newType = newType;
    }
    
    public String getNewTemplate() {
    	return newTemplate;
    }
    
    public void setNewTemplate(String newTemplate) {
    	this.newTemplate = newTemplate;
    }
    
    public String getNewName() {
    	return newName;
    }
    
    public void setNewName(String newName) {
    	this.newName = newName;
    }
    
    public String getNewDest() {
    	return newDest;
    }
    
    public void setNewDest(String newDest) {
    	this.newDest = newDest;
    }
    
}
