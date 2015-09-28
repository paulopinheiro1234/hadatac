package org.hadatac.entity.pojo;

public class TriggeringEvent {

	public static final String INITIAL_DEPLOYMENT        = "Initial Deployment";
	public static final String LEGACY_DEPLOYMENT         = "Legacy Deployment";
	public static final String CHANGED_CONFIGURATION     = "Changed Configuration";     // to show changed configuration
	public static final String CHANGED_OWNERSHIP         = "Changed Ownership";         // to show previous owner
	public static final String AUTO_CALIBRATION          = "Auto Calibration";          // to show calibration date/time
	public static final String SUSPEND_DATA_ACQUISITION  = "Suspend data acquisition";  // to show reason for suspension
	                                                                                    // e.g., lost of operation conditions
                                                                                        // e.g., lost of measurement conditions
	public static final String RESUME_DATA_ACQUISITION   = "Resume data acquisition";   // to show reason for suspension

	private String uri;
	private String label;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}
