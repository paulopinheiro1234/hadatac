package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class ObjectCollectionForm {

    public String newUri;
    public String newType;
    public String newLabel;
    public String newComment;
    public String newHasScopeUri;
    public List<String> spaceUri;
    public List<String> timeUri;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public ObjectCollectionForm () {
	spaceUri = new ArrayList<String>();
	timeUri = new ArrayList<String>();
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
    
    public String getNewLabel() {
	return newLabel;
    }
    
    public void setNewLabel(String label) {
	this.newLabel = label;
    }
    
    public String getNewComment() {
	return newComment;
    }
    
    public void setNewComment(String comment) {
	this.newComment = comment;
    }
    
    public String getNewHasScopeUri() {
	return newHasScopeUri;
    }
    
    public void setNewHasScopeUri(String hasScopeUri) {
	this.newHasScopeUri = hasScopeUri;
    }
    
    public List<String> getSpaceUri() {
	return spaceUri;
    }
    
    public void spaceUri(List<String> spaceUri) {
	this.spaceUri = spaceUri;
    }
    
    public void addSpaceUri(String spaceUri) {
	this.spaceUri.add(spaceUri);
    }
    
    public List<String> getTimeUri() {
	return timeUri;
    }
    
    public void setTimeUri(List<String> timeUri) {
	this.timeUri = timeUri;
    }
    
    public void addTimeUri(String timeUri) {
	this.timeUri.add(timeUri);
    }
    
}
