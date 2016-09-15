package org.hadatac.console.controllers.metadataacquisition;

import play.Play;
import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.utils.Collections;

public class DataAcquisitionFacet extends Controller {
    
    // for /dataacquisitions HTTP GET requests
   public static Result index() { 
	   String collection = Play.application().configuration().getString("hadatac.console.host_deploy") + 
   			request().path() + "/solrsearch";
	   return ok(dataacquisitionfacet.render(collection));
   }// /index()    // for /dataacquisitions HTTP POST requests
   
   public static Result postIndex() {
       return index();
   }// /postIndex()}
   
}