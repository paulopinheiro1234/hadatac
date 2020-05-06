package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class DeploymentForm {

    public String deploymentUri;
    public String dataAcquisitionUri;
    public String initialParam;
    public String platform;
    public String hasFirstCoordinate;
    public String hasSecondCoordinate;
    public String instrument;
    public List<String> detectors;
    public String startDateTime;
    public String endDateTime;
    public String type;
 
    public DeploymentForm () {
    	detectors = new ArrayList<String>();
    }
    
    public String getUri() {
	return deploymentUri;
    }
    
    public void setUri(String uri) {
	this.deploymentUri = uri;
    }
    
    public String getDataAcquisitionUri() {
	return dataAcquisitionUri;
    }
    
    public void setDataAcquisitionUri(String dataAcquisitionUri) {
	this.dataAcquisitionUri = dataAcquisitionUri;
    }
    
    public String getInitialParameter() {
	return initialParam;
    }
    
    public void setInitialParameter(String initialParam) {
	this.initialParam = initialParam;
    }
    
    public String getType() {
	return type;
    }
    
    public void setType(String type) {
	this.type = type;
    }
    
    public String getPlatform() {
    	return platform;
    }
    
    public void setPlatform(String platform) {
    	this.platform = platform;
    }
    
    public String getHasFirstCoordinate() {
    	return hasFirstCoordinate;
    }
    
    public void setHasFirstCoordinate(String hasFirstCoordinate) {
    	this.hasFirstCoordinate = hasFirstCoordinate;
    }

    public String getHasSecondCoordinate() {
    	return hasSecondCoordinate;
    }
    
    public void setHasSecondCoordinate(String hasSecondCoordinate) {
    	this.hasSecondCoordinate = hasSecondCoordinate;
    }

    public String getInstrument() {
    	return instrument;
    }
    
    public void setInstrument(String instrument) {
    	this.instrument = instrument;
    }

    public List<String> getDetectors() {
    	return detectors;
    }
    
    public void setDetectors(List<String> detector) {
    	this.detectors = detector;
    }

    public void addDetector(String detector) {
    	this.detectors.add(detector);
    }

    public String getStartDateTime() {
    	return startDateTime;
    }
    
    public void setStartDateTime(String startDateTime) {
    	this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
    	return endDateTime;
    }
    
    public void setEndDateTime(String endDateTime) {
    	this.endDateTime = endDateTime;
    }
}
