package org.hadatac.console.controllers.triplestore;

import org.hadatac.utils.CollectionUtil;
import play.mvc.*;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
//import controllers.AuthApplication;
import org.hadatac.console.views.html.triplestore.*;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;

public class ExportKB extends Controller {

    private static final String DOWNLOAD_FILE_NAME = "export_triples.ttl";

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String oper) {
        return ok(exportKB.render(oper, ""));
    }

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(String oper) {
        return ok(exportKB.render(oper, ""));
    }

//    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result exportFile(String oper) {
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                    "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ";
            Query query = QueryFactory.create(queryString);

            QueryExecution qexec = QueryExecutionFactory.sparqlService(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
            Model model = qexec.execConstruct();
            qexec.close();

            System.out.println("Model size: " + model.size());

            RDFDataMgr.write(new FileOutputStream(ConfigProp.getPathDownload() + DOWNLOAD_FILE_NAME, false),
                    model, RDFFormat.TURTLE);

            System.out.println("Export finished");

            return ok(new File(ConfigProp.getPathDownload() + DOWNLOAD_FILE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest("Export failed ...");
        }
    }
}