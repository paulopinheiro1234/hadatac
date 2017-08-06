package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class ObjectsForm {

    public String newUri;
    public String newType;
    public String newQuantity;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public ObjectsForm () {
    }
    
    public String getNewUri() {
	return newUri;
    }
    
    public void setNewUri(String uri) {
	this.newUri = uri;
    }
    
    public String getNewType() {
	return newType;
    }
    
    public void setNewType(String type) {
	this.newType = type;
    }
    
    public String getNewQuantity() {
	return newQuantity;
    }
    
    public void setNewQuantity(String quantity) {
	this.newQuantity = quantity;
    }
    
}
