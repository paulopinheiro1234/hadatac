package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadataacquisition.*;

public class MetadataAcquisition extends Controller {
    
    // for /metadataacquisitions HTTP GET requests
   public static Result index() { 
	   return ok(metadataacquisition.render());
   }// /index()    // for /metadataacquisitions HTTP POST requests
   public static Result postIndex() {
       
       return ok(metadataacquisition.render());
       
   }// /postIndex()}
   
}