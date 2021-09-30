package org.hadatac.console.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;


import play.mvc.*;
import play.mvc.Http.Session;
import play.mvc.Result;
import play.routing.JavaScriptReverseRouter;



public class JS extends Controller {

    public Result jsRoutesMain() {
		/*
		return ok(Routes.javascriptRouter("jsRoutesMain",
				routes.javascript.Signup.forgotPassword()))
				.as("text/javascript");
		*/
        return ok(JavaScriptReverseRouter.create("jsRoutesMain","","",
                //routes.javascript.Signup.forgotPassword()))
                routes.javascript.URILookup.processUri()))
                .as("text/javascript");
    }

}