package org.hadatac.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.HADatAcClass;
import org.hadatac.utils.ApiUtil;
import org.hadatac.utils.CollectionUtil;
import play.mvc.Controller;
import play.mvc.Result;

public class ClassPage extends Controller {

    public Result getHADatAcClass(String classUri){
        System.out.println("Inside ClassPage.getHADatAcClass");

        ObjectMapper mapper = new ObjectMapper();

        HADatAcClass hadatacClass = HADatAcClass.find(classUri);
        /*


        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + classUri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        StmtIterator stmtIterator = model.listStatements();

        // returns null if not statement is found
        if (!stmtIterator.hasNext()) {
            return notFound(ApiUtil.createResponse("No class found for uri [" + classUri + "]", false));
        }

        Object typeClass = new Object();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            System.out.println("predicate: " + statement.getPredicate().getURI());
        }

        String typeUri = null;
        */

        System.out.println("[RestAPI] class: " + classUri);

        try {
            // get the list of variables in that study
            // serialize the Study object first as ObjectNode
            //   as JsonNode is immutable and meant to be read-only
            ObjectNode obj = mapper.convertValue(hadatacClass, ObjectNode.class);
            JsonNode jsonObject = mapper.convertValue(obj, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest(ApiUtil.createResponse("Error parsing class", false));
        }
    }

}
