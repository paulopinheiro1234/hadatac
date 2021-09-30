package org.hadatac.console.controllers;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SignUp;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.providers.MyUsernamePasswordAuthProvider;
import org.hadatac.console.views.html.triplestore.notRegistered;
import org.pac4j.core.exception.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * An example of form processing.
 *
 * https://playframework.com/documentation/latest/JavaForms
 */
@Singleton
public class WidgetController extends Controller {

    private final Form<MyUsernamePasswordAuthProvider> form;
    private MessagesApi messagesApi;
    private final List<SignUp> signUps;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    public WidgetController(FormFactory formFactory, MessagesApi messagesApi){
        this.form = formFactory.form(MyUsernamePasswordAuthProvider.class);
        this.messagesApi = messagesApi;
        this.signUps = com.google.common.collect.Lists.newArrayList(
                new SignUp("Data 1", "a", "a","a"),
                new SignUp("Data 2", "b","b","b"),
                new SignUp("Data 3", "c", "c","c")
        );
    }

    public Result listWidgets(Http.Request request) throws TechnicalException {
        return ok(org.hadatac.console.views.html.listWidgets.render(asScala(signUps), form, request, messagesApi.preferred(request)));

    }

    @SubjectNotPresent
    public Result createUser(Http.Request request) throws TechnicalException {
        final Form<MyUsernamePasswordAuthProvider> boundForm = form.bindFromRequest(request);
        if (SysUser.existsSolr()) { // only check for pre-registration if it is not the first user signing up
            if (!UserManagement.isPreRegistered(boundForm.get().getEmail())) {
                return ok(notRegistered.render());
            }
        }

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest(org.hadatac.console.views.html.listWidgets.render(asScala(signUps), boundForm, request, messagesApi.preferred(request)));
        } else {
            MyUsernamePasswordAuthProvider data = boundForm.get();
            if (data.validate()!=null){
//                messagesApi.preferred(request).at("Your e-mail has already been validated.");
                return redirect(org.hadatac.console.controllers.routes.WidgetController.listWidgets())
                        .flashing("error",data.validate());
            }
            signUps.add(new SignUp(data.getName(), data.getEmail(), data.getPassword(), data.getRepeatPassword()));
            try {
                LinkedAccount linkedAccount = new LinkedAccount();
                linkedAccount.providerKey ="password"; //TODO : generalize later
                linkedAccount.providerUserId=data.getHashedPassword();
            String userUri = UserManagement.getUriByEmail(data.getEmail()); //TODO: fix it
            final SysUser newUser = SysUser.create(data, userUri, linkedAccount);
            System.out.println("commit done");
//            solrClient.close();
            } catch (Exception e) {
                System.out.println("[ERROR] User.getAuthUserFindSolr - Exception message: " + e.getMessage());
            }

            return redirect(org.hadatac.console.controllers.routes.Portal.index());
                    //routes.WidgetController.listWidgets())
                    //.flashing("info", "User added!");
        }
    }
}