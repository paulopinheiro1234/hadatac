package org.hadatac.utils;

public class Feedback {

    public static final int WEB = 0;
    public static final int COMMANDLINE = 1;
     	
    public static String println(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	} else {
    		str = str.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    		str += "<br>";
    	}
    	return str;
    }

    public static String print(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	}
    	else {
    		str = str.replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    	}
    	return str;
    }
}	
	
