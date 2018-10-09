package org.hadatac.console.controllers.metadata;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.controllers.schema.EditingOptions;

public class BrowseOntology extends Controller {

    public Result index(String oper) {
	return ok(browseOntology.render(oper, EditingOptions.getEntities(), EditingOptions.getAttributes(), EditingOptions.getUnits()));
    }

    public Result postIndex(String oper) {
	return index(oper);
    }

    public Result graphIndex(String oper) {
    return ok(browseKnowledgeGraph.render(oper, EditingOptions.getHierarchy()));
    }

}
