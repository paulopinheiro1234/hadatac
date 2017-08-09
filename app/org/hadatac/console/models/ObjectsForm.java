package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class ObjectsForm {

    public List<String> newLabel;
    public List<String> newType;
    public List<String> newOriginalId;
    public List<String> newIsFrom;
    public List<String> newAtLocation;
    public List<String> newAtTime;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public ObjectsForm () {
	newLabel = new ArrayList<String>();
	newType = new ArrayList<String>();
	newOriginalId = new ArrayList<String>();
	newIsFrom = new ArrayList<String>();
	newAtLocation = new ArrayList<String>();
	newAtTime = new ArrayList<String>();
    }
    
    public List<String> getNewLabel() {
	return newLabel;
    }
    
    public void setNewLabel(List<String> label) {
	this.newLabel = label;
    }
    
    public List<String> getNewType() {
	return newType;
    }
    
    public void setNewType(List<String> type) {
	this.newType = type;
    }
    
    public List<String> getNewOriginalId() {
	return newOriginalId;
    }
    
    public void setNewOriginalId(List<String> originalId) {
	this.newOriginalId = originalId;
    }
    
    public List<String> getNewIsFrom() {
	return newIsFrom;
    }
    
    public void setNewIsFrom(List<String> isFrom) {
	this.newIsFrom = isFrom;
    }
    
    public List<String> getNewAtLocation() {
	return newAtLocation;
    }
    
    public void setNewAtLocation(List<String> atLocation) {
	this.newAtLocation = atLocation;
    }
    
    public List<String> getNewAtTime() {
	return newAtTime;
    }
    
    public void setNewAtTime(List<String> atTime) {
	this.newAtTime = atTime;
    }
    
}
