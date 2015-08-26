package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;

public class LoadOnt extends Controller {

    public static Result loadOnt(String oper) {
	return ok(loadOnt.render(oper));
    }

    public static Result postLoadOnt(String oper) {
	return ok(loadOnt.render(oper));
    }

}
