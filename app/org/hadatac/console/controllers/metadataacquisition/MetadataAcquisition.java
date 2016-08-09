package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;
<<<<<<< HEAD
//import org.hadatac.console.views.html.metadataacquisition.metadataacquisition;
//import org.hadatac.console.views.html.metadata.metadata;

import org.hadatac.console.views.html.metadataacquisition.*;

public class MetadataAcquisition extends Controller {

	// for /metadataacquisition HTTP GET requests
	public static Result index() {       
		return ok(metadataacquisition.render());
	}
	//        return ok(metadata.render());
	// /index()    

	// for /metadataacquisition HTTP POST requests
	public static Result postIndex() {

		return ok(metadataacquisition.render());
		//        return ok(metadata.render());

	}// /postIndex()
=======
import org.hadatac.console.views.html.metadataacquisition.*;

public class MetadataAcquisition extends Controller {
    
    // for /metadataacquisitions HTTP GET requests
   public static Result index() { 
	   return ok(metadataacquisition.render());
   }// /index()    // for /metadataacquisitions HTTP POST requests
   public static Result postIndex() {
       
       return ok(metadataacquisition.render());
       
   }// /postIndex()}
   
>>>>>>> a8aa82048d32cef6d9ffebc4094b3b6d17a3bf91
}