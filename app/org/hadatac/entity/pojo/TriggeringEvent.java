package org.hadatac.entity.pojo;

public class TriggeringEvent {

	public static final int INITIAL_DEPLOYMENT        = 1;
	public static final int LEGACY_DEPLOYMENT         = 2;
	public static final int CHANGED_CONFIGURATION     = 3;   
	public static final int CHANGED_OWNERSHIP         = 4;   
	public static final int AUTO_CALIBRATION          = 5;   
	public static final int SUSPEND_DATA_ACQUISITION  = 6;  
	public static final int RESUME_DATA_ACQUISITION   = 7;   

	public static final String INITIAL_DEPLOYMENT_NAME        = "Initial Deployment";
	public static final String LEGACY_DEPLOYMENT_NAME         = "Legacy Deployment";
	public static final String CHANGED_CONFIGURATION_NAME     = "Changed Configuration";     // to show changed configuration
 	public static final String CHANGED_OWNERSHIP_NAME         = "Changed Ownership";         // to show previous owner
	public static final String AUTO_CALIBRATION_NAME          = "Auto Calibration";          // to show calibration date/time
	public static final String SUSPEND_DATA_ACQUISITION_NAME  = "Suspend data acquisition";  // to show reason for suspension
        	                                                                                 // e.g., lost of operation conditions
                                                                                             // e.g., lost of measurement conditions
	public static final String RESUME_DATA_ACQUISITION_NAME   = "Resume data acquisition";   // to show reason for suspension

	public String getName(int event_id) {
		String label = "";
		switch (event_id) {
		case INITIAL_DEPLOYMENT: 
			label = INITIAL_DEPLOYMENT_NAME;
			break;
		case LEGACY_DEPLOYMENT:  
			label = LEGACY_DEPLOYMENT_NAME;
            break;
		case CHANGED_CONFIGURATION:
			label = CHANGED_CONFIGURATION_NAME;
			break;
		case CHANGED_OWNERSHIP: 
			label = CHANGED_OWNERSHIP_NAME;   
            break;
		case AUTO_CALIBRATION: 
			label = AUTO_CALIBRATION_NAME;   
			break;
		case SUSPEND_DATA_ACQUISITION: 
			label = SUSPEND_DATA_ACQUISITION_NAME;  
			break;
		case RESUME_DATA_ACQUISITION: 
			label = RESUME_DATA_ACQUISITION_NAME;   
			break;
		}
		return label;
	}
	
	public int getId(String event_name) {
		int id = -1;
		switch (event_name) {
		case INITIAL_DEPLOYMENT_NAME: 
			id = INITIAL_DEPLOYMENT;
			break;
		case LEGACY_DEPLOYMENT_NAME:  
			id = LEGACY_DEPLOYMENT;
            break;
		case CHANGED_CONFIGURATION_NAME:
			id = CHANGED_CONFIGURATION;
			break;
		case CHANGED_OWNERSHIP_NAME: 
			id = CHANGED_OWNERSHIP;   
            break;
		case AUTO_CALIBRATION_NAME: 
			id = AUTO_CALIBRATION;   
			break;
		case SUSPEND_DATA_ACQUISITION_NAME: 
			id = SUSPEND_DATA_ACQUISITION;  
			break;
		case RESUME_DATA_ACQUISITION_NAME: 
			id = RESUME_DATA_ACQUISITION;   
			break;
		}
		return id;
	}
	
}
