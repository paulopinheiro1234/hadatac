package org.hadatac.utils;

public class Feedback {

    public static final int WEB = 0;
    public static final int COMMANDLINE = 1;
     	
    public static String println(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	} else {
        	//System.out.println("BEFORE: " + str);
    		str = str.replace("<", "&lt;").replace(">", "&gt;");
        	//System.out.println("AFTER : " + str);
    		str += "<br>";
    	}
    	return str;
    }

    public static String print(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	}
    	//System.out.println("BEFORE: " + str);
		str = str.replace("<", "&lt;").replace(">", "&gt;");
    	//System.out.println("AFTER : " + str);
    	return str;
    }
}	
	
