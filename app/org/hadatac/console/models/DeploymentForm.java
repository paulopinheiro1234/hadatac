package org.hadatac.console.models;

import java.util.Date;

public class DeploymentForm {

    public String platform;
    public String instrument;
    public String detector;
    public Date startDateTime;
 
    public String getPlatform() {
    	return platform;
    }
    
    public void setPlatform(String platform) {
    	this.platform = platform;
    }

    public String getInstrument() {
    	return instrument;
    }
    
    public void setInstrument(String instrument) {
    	this.instrument = instrument;
    }

    public String getDetector() {
    	return detector;
    }
    
    public void setDetector(String detector) {
    	this.detector = detector;
    }

    public Date getStartDateTime() {
    	return startDateTime;
    }
    
    public void setStartDateTime(Date startDateTime) {
    	this.startDateTime = startDateTime;
    }


}
