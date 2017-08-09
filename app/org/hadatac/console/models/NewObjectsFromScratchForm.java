package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class NewObjectsFromScratchForm {

    public String newUri;
    public String newType;
    public String newQuantity;
    public String useDomain;
    public String useSpace;
    public String useTime;
    public String newMultiplier;
    public String newLabelPrefix;
    public String newLabelQualifier;

    // STUDY information is not passed through the form since it cannot change
    //public String newStudyUri;
    
    public NewObjectsFromScratchForm () {
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
    
    public String getUseDomain() {
	return useDomain;
    }
    
    public void setUseDomain(String useDomain) {
	this.useDomain = useDomain;
    }
    
    public String getUseSpace() {
	return useSpace;
    }
    
    public void setUseSpace(String useSpace) {
	this.useSpace = useSpace;
    }
    
    public String getUseTime() {
	return useTime;
    }
    
    public void setUseTime(String useTime) {
	this.useTime = useTime;
    }
    
    public String getNewMultiplier() {
	return newMultiplier;
    }
    
    public void setNewMultiplier(String multiplier) {
	this.newMultiplier = multiplier;
    }
    
    public String getNewLabelPrefix() {
	return newLabelPrefix;
    }
    
    public void setNewLabelPrefix(String labelPrefix) {
	this.newLabelPrefix = labelPrefix;
    }
    
    public String getNewLabelQualifier() {
	return newLabelQualifier;
    }
    
    public void setNewLabelQualifier(String labelQualifier) {
	this.newLabelQualifier = labelQualifier;
    }
    
}
