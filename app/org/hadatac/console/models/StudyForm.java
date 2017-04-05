package org.hadatac.console.models;

import java.util.ArrayList;
import java.util.List;

public class StudyForm {

	public String studyUri;
	public String initialParam;
    public List<String> dataAcquisitions;
    public String startDateTime;
    public String endDateTime;
 
    public StudyForm () {
    	dataAcquisitions = new ArrayList<String>();
    }
    
    public String getUri() {
		return studyUri;
	}

	public void setUri(String uri) {
		this.studyUri = uri;
	}
	
	public String getInitialParameter() {
		return initialParam;
	}

	public void setInitialParameter(String initialParam) {
		this.initialParam = initialParam;
	}

    public List<String> getDataAcquisitions() {
    	return dataAcquisitions;
    }
    
    public void setDataAcquisitions(List<String> dataAcquisition) {
    	this.dataAcquisitions = dataAcquisition;
    }

    public void addDataAcquisition(String dataAcquisition) {
    	this.dataAcquisitions.add(dataAcquisition);
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
