package org.hadatac.console.controllers;

import com.typesafe.config.ConfigFactory;
import module.SecurityModule;
import org.pac4j.play.LogoutController;

import javax.inject.Inject;

public class CentralLogoutController extends LogoutController {
    @Inject Application application;

    public CentralLogoutController() {
        setDefaultUrl(ConfigFactory.load().getString("hadatac.console.host")+"/hadatac");
        setLocalLogout(true);
        setCentralLogout(true);
        setLogoutUrlPattern(ConfigFactory.load().getString("hadatac.console.host")+"/.*");
    }
}
