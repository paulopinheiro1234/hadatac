package org.hadatac.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;

import play.Environment;
import play.mvc.Controller;

public class ReadResource extends Controller{

    private final Environment environment;

    @Inject
    public ReadResource(Environment environment){
         this.environment = environment;
    }

    public String readResourceAsStream(String filename) {
    	InputStream resource = environment.resourceAsStream(filename);
    	StringWriter writer = new StringWriter();
    	try {
			IOUtils.copy(resource, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	String str = writer.toString();    
    	return str;
    }
}
