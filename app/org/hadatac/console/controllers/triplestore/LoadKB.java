package org.hadatac.console.controllers.triplestore;

import play.*;
import play.mvc.*;
import play.libs.*;

import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.metadata.loader.NameSpaces;

public class LoadKB extends Controller {

    public static Result loadKB(String oper) {
	return ok(loadKB.render(oper));
    }

    public static Result postLoadKB(String oper) {
	return ok(loadKB.render(oper));
    }

}
