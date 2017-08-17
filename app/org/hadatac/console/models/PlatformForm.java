package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class PlatformForm {

    public String newPlatformUri;
    public String newPlatformType;
    public String newLabel;
    public String newComment;
    
    public PlatformForm () {
    }
    
    public String getNewUri() {
	return newPlatformUri;
    }
    
    public void setNewUri(String uri) {
	this.newPlatformUri = uri;
    }
    
    public String getNewType() {
	return newPlatformType;
    }
    
    public void setNewType(String type) {
	this.newPlatformType = type;
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
