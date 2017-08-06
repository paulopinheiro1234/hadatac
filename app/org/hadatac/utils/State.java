package org.hadatac.utils;

public class State {

    private int current;
	
    public static final String ACTIVE_NAME            = "active";
    public static final String CLOSED_NAME            = "closed";
    public static final String ALL_NAME               = "all";
    
    public static final int ACTIVE  = 1;
    public static final int CLOSED  = 2;
    public static final int ALL     = 3;
    public static final int CHANGED = 4;
    
    public static final int PROCESSED  	= 4;
    public static final int UNPROCESSED = 5;
    
    public State () {
	current = ACTIVE;
    }
	
    public State (int current) {
	this.current = current;
    }
    
    public int getCurrent() {
	return current;
    }
    
}
