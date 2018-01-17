package org.hadatac.console.controllers.triplestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import play.mvc.*;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class LoadOnt extends Controller {
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result loadOnt(String oper) {
    	
    	List<String> cacheList = new ArrayList<String>();
    	File folder = new File(NameSpaces.CACHE_PATH);
    	
    	// if the directory does not exist, create it
    	if (!folder.exists()) {
    	    System.out.println("creating directory: " + NameSpaces.CACHE_PATH);
    	    try{
    	    	folder.mkdir();
    	    } 
    	    catch(SecurityException se){
    	    	System.out.println("Failed to create directory.");
    	    }
    	    System.out.println("DIR created");
    	}
    	
    	String name = "";
    	for (final File fileEntry : folder.listFiles()) {
    	    if (!fileEntry.isDirectory()) {
    	    	name = fileEntry.getName();
    	    	if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
    	    		name = name.substring(NameSpaces.CACHE_PREFIX.length());
    	    		cacheList.add(name);
    	    		//System.out.println(name);
    	    	}
    	    }
    	}

    	return ok(loadOnt.render(oper, cacheList));
    }

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postLoadOnt(String oper) {
    	
    	List<String> cacheList = new ArrayList<String>();
    	File folder = new File(NameSpaces.CACHE_PATH); 
    	String name = "";
    	
    	for (final File fileEntry : folder.listFiles()) {
    	    if (!fileEntry.isDirectory()) {
    	    	name = fileEntry.getName();
    	    	if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
    	    		name = name.substring(NameSpaces.CACHE_PREFIX.length());
    	    		cacheList.add(name);
    	    		//System.out.println(name);
    	    	}
    	    }
    	}

    	return ok(loadOnt.render(oper, cacheList));
    }

    public static String playLoadOntologies(String oper) {
	     NameSpaces.getInstance();
	     MetadataContext metadata = new 
	    		 MetadataContext("user", 
	    				         "password", 
	    				         ConfigFactory.load().getString("hadatac.solr.triplestore"), 
	    				         false);
	     return metadata.loadOntologies(Feedback.WEB, oper);
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result eraseCache() {
    	List<String> cacheList = new ArrayList<String>();
    	File folder = new File(NameSpaces.CACHE_PATH); 
    	String name = "";
    	
    	for (final File fileEntry : folder.listFiles()) {
    	    if (!fileEntry.isDirectory()) {
    	    	name = fileEntry.getName();
    	    	if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
    	    		fileEntry.delete();
    	    	}
    	    }
    	}

    	return ok(loadOnt.render("init", cacheList));
   }

}
