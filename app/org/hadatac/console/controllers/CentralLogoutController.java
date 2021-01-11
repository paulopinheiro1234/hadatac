package org.hadatac.console.controllers;

import org.pac4j.play.LogoutController;

public class CentralLogoutController extends LogoutController {

    public CentralLogoutController() {
        setDefaultUrl("http://localhost:9000/hadatac");
        setLocalLogout(true);
        setCentralLogout(true);
        setLogoutUrlPattern("http://localhost:9000/.*");
    }
}
