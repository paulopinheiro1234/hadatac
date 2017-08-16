package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class StudyForm {

    public String newStudyUri;
    public String newStudyType;
    public String newLabel;
    public String newTitle;
    public String newProject;
    public String newComment;
    public String newExternalSource;
    public String newInstitution;
    public String newAgent;
    public String newStartDateTime;
    public String newEndDateTime;
    
    public StudyForm () {
    }
    
    public String getNewUri() {
	return newStudyUri;
    }
    
    public void setNewUri(String uri) {
	this.newStudyUri = uri;
    }
    
    public String getNewType() {
	return newStudyType;
    }
    
    public void setNewType(String type) {
	this.newStudyType = type;
    }
    
    public String getNewLabel() {
	return newLabel;
    }
    
    public void setNewLabel(String label) {
	this.newLabel = label;
    }
    
    public String getNewTitle() {
	return newTitle;
    }
    
    public void setNewTitle(String title) {
	this.newTitle = title;
    }
    
    public String getNewProject() {
	return newProject;
    }
    
    public void setNewProject(String project) {
	this.newProject = project;
    }
    
    public String getNewComment() {
	return newComment;
    }
    
    public void setNewComment(String comment) {
	this.newComment = comment;
    }
    
    public String getNewExternalSource() {
	return newExternalSource;
    }
    
    public void setNewExternalSource(String externalSource) {
	this.newExternalSource = externalSource;
    }
    
    public String getNewInstitution() {
	return newInstitution;
    }
    
    public void setNewInstitution(String institution) {
	this.newInstitution = institution;
    }
    
    public String getNewAgent() {
	return newAgent;
    }
    
    public void setNewAgent(String agent) {
	this.newAgent = agent;
    }
    
    public String getNewStartDateTime() {
    	return newStartDateTime;
    }
    
    public void setNewStartDateTime(String startDateTime) {
    	this.newStartDateTime = startDateTime;
    }
    
    public String getNewEndDateTime() {
    	return newEndDateTime;
    }
    
    public void setNewEndDateTime(String endDateTime) {
    	this.newEndDateTime = endDateTime;
    }
}
