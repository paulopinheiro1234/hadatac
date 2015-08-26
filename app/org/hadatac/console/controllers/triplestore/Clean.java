package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;

public class Clean extends Controller {

    public static Result clean(String oper) {
	return ok(clean.render(oper));
    }

    public static Result postClean(String oper) {
	return ok(clean.render(oper));
    }

}
