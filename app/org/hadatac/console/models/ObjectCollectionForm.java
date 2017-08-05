package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class ObjectCollectionForm {

    public String newUri;
    public String newType;
    public String newLabel;
    public String newComment;
    public String newHasScopeUri;
    public List<String> newSpaceScopeUris;
    public List<String> newTimeScopeUris;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public ObjectCollectionForm () {
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
    
    public List<String> getNewSpaceScopeUris() {
	return newSpaceScopeUris;
    }
    
    public void setNewSpaceScopeUri(List<String> spaceScopeUris) {
	this.newSpaceScopeUris = spaceScopeUris;
    }
    
    public List<String> getNewTimeScopeUris() {
	return newTimeScopeUris;
    }
    
    public void setNewTimeScopeUris(List<String> timeScopeUris) {
	this.newTimeScopeUris = timeScopeUris;
    }
    
}
