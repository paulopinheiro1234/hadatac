package org.hadatac.console.controllers.dataacquisitionfacet;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.dataacquisitionfacet.*;

public class DataAcquisitionFacet extends Controller {
    
    // for /dataacquisitions HTTP GET requests
   public static Result index() { 
	   return ok(dataacquisitionfacet.render());
   }// /index()    // for /dataacquisitions HTTP POST requests
   
   public static Result postIndex() {
       
       return ok(dataacquisitionfacet.render());
       
   }// /postIndex()}
   
}