package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.management.*;

public class Management extends Controller {
	
    public static Result cleanTS() {

       return ok(clean.render());
        
    }

    public static Result postCleanTS() {
        
        return ok(clean.render());
        
    }

    public static Result loadOntTS() {

       return ok(loadOnt.render());
        
    }

    public static Result postLoadOntTS() {
        
        return ok(loadOnt.render());
        
    }

    public static Result loadKbTS() {

       return ok(loadKb.render());
        
    }

    public static Result postLoadKbTS() {
        
        return ok(loadKb.render());
        
    }

}
