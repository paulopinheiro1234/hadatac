package org.hadatac.metadata.loader;

public class Feedback {

    public static final int WEB = 0;
    public static final int COMMANDLINE = 1;
     	
    public static String println(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	} else {
    		str += "<br>";
    	}
    	return str;
    }

    public static String print(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	}
    	return str;
    }
    
}	
	
