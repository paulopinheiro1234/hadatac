package org.hadatac.console.controllers.annotator;

import play.mvc.Controller;
import play.mvc.Result;
import org.hadatac.console.views.html.annotator.*;

public class Downloads extends Controller {

    public static Result uploadCCSV() {
      return ok(completeAnnotation.render());   
    }

    public static Result postUploadCCSV() {
        return ok(completeAnnotation.render());
    }

    public static Result genCCSV() {
        return ok(completeAnnotation.render());   
    }

    public static Result postGenCCSV() {
        return ok(completeAnnotation.render());
    }

    public static Result genPreamble() {
        return ok(completeAnnotation.render());   
    }

    public static Result postGenPreamble() {
        return ok(completeAnnotation.render());
    }

    public static Result cancel() {
        return ok(completeAnnotation.render());   
    }

    public static Result postCancel() {
        return ok(completeAnnotation.render());
    }

}
