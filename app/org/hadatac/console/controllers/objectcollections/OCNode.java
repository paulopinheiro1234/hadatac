package org.hadatac.console.controllers.objectcollections;

public class OCNode {

    public static final int STUDY = 0;
    public static final int COLLECTION = 1;
    public static final int SPACECOLLECTION = 2;
    public static final int TIMECOLLECTION = 3;
    public static final int OBJECT = 1;
    public static final int SPACEOBJECT = 2;
    public static final int TIMEOBJECT = 3;
    
    private String name;
    private String uri;
    private int type;
    private String memberOf;
    
    public OCNode(String u, String n, int t, String mOf) {
        name = n;
        uri = u;
        type = t;
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
        
    public String getMemberOf() {
        return memberOf;
    }
}
