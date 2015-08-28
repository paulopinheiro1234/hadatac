package org.hadatac.metadata.model;

public class SpreadsheetParsingResult {

    private String message;
    private String turtle;
    
    public SpreadsheetParsingResult(String msg, String ttl) {
        message = msg;
        turtle = ttl;
    }
 
    public String getMessage() {
        return message;
    }	    
        
    public String getTurtle() {
        return turtle;
    }	    
        
}
