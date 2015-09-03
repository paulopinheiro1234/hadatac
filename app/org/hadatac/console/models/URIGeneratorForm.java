package org.hadatac.console.models;

import java.util.Date;

public class URIGeneratorForm {

	/* 
	 * Each URI Generator has a fixed ID, which is a Long int value encoded in the hexadecimal format.	
	 */
	
    public String ownerURI;
    public String description;
    public Long generatedID;
    public Date dateTime;
 
    public String getOwnerURI() {
    	return ownerURI;
    }
    
    public void setOwnerURI(String ownerURI) {
    	this.ownerURI = ownerURI;
    }

    public String getDescription() {
    	return description;
    }
    
    public void setDescription(String description) {
    	this.description = description;
    }

    public String getGeneratedID() {
    	return generatedID.toString();
    }
    
    public void setGeneratedID(Long generatedID) {
    	this.generatedID = generatedID;
    }

    public Date getDateTime() {
    	return dateTime;
    }
    
    public void setDateTime(Date dateTime) {
    	this.dateTime = dateTime;
    }
    
}
