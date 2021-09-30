package org.hadatac.console.controllers.restapi;

import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.FormFactory;

import java.util.List;

import javax.inject.Inject;

import org.hadatac.console.views.html.restapi.*;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class ExampleCalls extends Controller {

    public Result index() {
        return ok(examplecalls.render());
    }

    public Result postIndex() {
        return index();
    }

}
