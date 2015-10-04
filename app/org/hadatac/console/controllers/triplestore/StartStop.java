package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class StartStop extends Controller {

    public static Result index(String oper, String repository) {
	return ok(clean.render("init"));
    }

    public static Result postIndex(String oper, String repository) {
	return ok(clean.render("init"));
    }

}
