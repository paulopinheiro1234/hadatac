package org.hadatac.console.controllers;

import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.triplestore.LoadOnt;
import org.hadatac.console.views.html.version;
import org.hadatac.entity.pojo.Ontology;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class Version extends Controller {
	
    public Result index() {
        String code_version = "1.2.3";
        String base_ontology = ConfigFactory.load().getString("hadatac.community.ont_prefix");
        String loaded_base_ontology = NameSpaces.getInstance().getNameByAbbreviation(base_ontology);
        String loaded_base_ontology_version = Ontology.getVersionFromAbbreviation(base_ontology);
        String propfile = LoadOnt.getNameLastLoadedNamespace();
        return ok(version.render(code_version, base_ontology, loaded_base_ontology, loaded_base_ontology_version, propfile));
    }

    public Result postIndex() {
        return index();
    }
}
