package org.hadatac.console.controllers.sandbox;

import java.util.List;

import play.mvc.*;
import play.mvc.Result;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.controllers.routes;
import org.hadatac.console.views.html.sandbox.*;
import org.hadatac.entity.pojo.OperationMode;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class Sandbox extends Controller {
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        List<OperationMode> modes = OperationMode.findAll();
        if (modes.size() == 0) {
            return ok(sandbox.render(true, "", OperationMode.REGULAR, 
                    "Sandbox mode is OFF. You are allowed to open the SANDBOX mode now.",
                    routes.Portal.index().url()));
        } else if (modes.size() == 1) {
            OperationMode mode = modes.get(0);
            if (mode.getUserEmail().equals(user.getEmail())) {
                if (mode.getOperationMode().equals(OperationMode.SANDBOX)) {
                    return ok(sandbox.render(true, mode.getUserEmail(), OperationMode.SANDBOX, 
                            "Sandbox mode is ON. You are allowed to exit the SANDBOX mode now.",
                            routes.Portal.index().url()));
                }
            } else {
                return ok(sandbox.render(false, mode.getUserEmail(), OperationMode.SANDBOX, 
                        "Another user " + mode.getUserEmail() + " is using the SANDBOX mode. You are NOT allowed to operate now.",
                        routes.Portal.index().url()));
            }
        }
        
        return badRequest("Internal errors occurred for Sandbox Management!");
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
        return index();
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result startSandboxMode() {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        List<OperationMode> modes = OperationMode.findAll();
        if (modes.size() == 0) {
            OperationMode mode = new OperationMode();
            mode.setUserEmail(user.getEmail());
            mode.setOperationMode(OperationMode.SANDBOX);
            mode.save();
        }

        return redirect(org.hadatac.console.controllers.sandbox.routes.Sandbox.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result exitSandboxMode() {
        final SysUser user = AuthApplication.getLocalUser(session());
        
        OperationMode mode = OperationMode.findByEmail(user.getEmail());
        if (mode != null) {
            mode.delete();
        }

        return redirect(org.hadatac.console.controllers.sandbox.routes.Sandbox.index());
    }
}
