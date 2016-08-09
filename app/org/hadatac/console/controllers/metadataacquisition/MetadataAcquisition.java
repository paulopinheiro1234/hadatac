package org.hadatac.console.controllers.metadataacquisition;

import play.mvc.Controller;
import play.mvc.Result;
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
}