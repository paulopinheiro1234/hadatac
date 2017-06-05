package org.hadatac.utils;

public class NameSpace {

	public static final String OBOE                    = "oboe";
	public static final String OBOE_CORE               = "oboe-core";
	public static final String OBOE_STANDARDS          = "oboe-standards";
	public static final String OBOE_CHEMISTRY          = "oboe-chemistry";
	public static final String OBOE_ECOLOGY            = "oboe-ecology";
	public static final String OBOE_CHARACTERISTICS    = "oboe-characteristics";
	public static final String RDF                     = "rdf";
	public static final String RDFS                    = "rdfs";
	public static final String XSD                     = "xsd";
	public static final String OWL                     = "owl";
	public static final String VSTOI                   = "vstoi";
	public static final String PROV                    = "prov";
	public static final String HASNETO                 = "hasneto";
	public static final String HASCO               		= "hasco";
	public static final String FOAF                    = "foaf";
	public static final String HADATAC_SN              = "hadatac-sn";
	public static final String HADATAC_ENTITIES        = "hadatac-entities";
	public static final String HADATAC_STANDARDS       = "hadatac-standards";
	
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
