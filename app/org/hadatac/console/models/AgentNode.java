package org.hadatac.console.models;

public class AgentNode {

	public static final int AGENT = 0;
	public static final int ORGANIZATION = 1;
	public static final int PERSON = 2;
	public static final int GROUP = 3;
	
    private String name;
    private String uri;
    private int type;
    private String email;
    private String memberOf;
    
    public AgentNode(String u, String n, int t, String e, String mOf) {
        name = n;
        uri = u;
        type = t;
        email = e;
        memberOf = mOf;
    }
 
    public String getName() {
        return name;
    }	    
        
    public String getURI() {
        return uri;
    }	    
        
    public int getType() {
        return type;
    }	    
        
    public String getEmail() {
        return email;
    }	    
        
    public String getMemberOf() {
        return memberOf;
    }
}
