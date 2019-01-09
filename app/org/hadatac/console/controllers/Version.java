package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

public class Version extends Controller {
	
    public Result index() {
        String version = "1.0.38";
        
        return ok(new Html("<div class=\"container-fluid\"><h4>"
                + "Version: " + version
                + "</h4></div>"));
    }

    public Result postIndex() {
        return index();
    }
}
