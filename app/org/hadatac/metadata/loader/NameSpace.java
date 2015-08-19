package org.hadatac.metadata.loader;

public class NameSpace {

	private String nsAbbrev = null;
	private String nsName = null;
	private String nsType = null;
	private String nsURL = null;
	
	public NameSpace () {
	}
	
	public NameSpace (String abbrev, String name, String type, String url) {
		nsAbbrev = abbrev;
		nsName = name;
		nsType = type;
		nsURL = url;
	}   
	
	public String getAbbreviation() {
		return nsAbbrev;
	}
	
	public void setAbbreviation(String abbrev) {
		nsAbbrev = abbrev;		
	}
	
	public String getName() {
		return nsName;
	}
	
	public void setName(String name) {
		nsName = name;
	}
	
	public String getType() {
		return nsType;
	}
	
	public void setType(String type) {
		nsType = type;
	}
	
	public String getURL() {
		return nsURL;
	}

	public void setURL(String url) {
		nsURL = url;
	}
	
	public String toString() {
		if (nsAbbrev == null) {
			return "null";
		}
		String showType = "null";
		if (nsType != null)
			showType = nsType;
		if (nsURL == null)
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", NO URL)";
		else 
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", " + nsURL + ")";
	}
}
