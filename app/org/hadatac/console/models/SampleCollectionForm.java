package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class SampleCollectionForm {

    public String newUri;
    public String newType;
    public String newLabel;
    public String newComment;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public SampleCollectionForm () {
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
    
}
