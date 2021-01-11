package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;

public class UserGroup extends User {

    public static User find(String uri) {
        User user = null;

        String queryString = "DESCRIBE <" + uri + ">";

        Statement statement;
        RDFNode object;

        Model modelPrivate = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.PERMISSIONS_SPARQL), queryString);

        if (!modelPrivate.isEmpty()) {
            user = new User();
            user.setUri(uri);
        }

        StmtIterator stmtIteratorPrivate = modelPrivate.listStatements();
        while (stmtIteratorPrivate.hasNext()) {
            statement = stmtIteratorPrivate.next();
            if (!statement.getSubject().getURI().equals(uri)) {
                continue;
            }

            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                user.setComment(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("sio:SIO_000095"))) {
                if(object.toString().equals("Public") || object.toString().equals("")) {
                    user.setImmediateGroupUri("Public");
                } else {
                    user.setImmediateGroupUri(object.asResource().toString());
                }
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/givenName")) {
                user.setGivenName(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/familyName")) {
                user.setFamilyName(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
                user.setName(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
                user.setEmail(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/homepage")) {
                String homepage = object.asLiteral().getString();
                if (homepage.startsWith("<") && homepage.endsWith(">")) {
                    homepage = homepage.replace("<", "");
                    homepage = homepage.replace(">", "");
                }
                user.setHomepage(homepage);
            }
        }

        Model modelPublic = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!modelPublic.isEmpty()) {
            user = new User();
            user.setUri(uri);
        }

        StmtIterator stmtIteratorPublic = modelPublic.listStatements();
        while (stmtIteratorPublic.hasNext()) {
            statement = stmtIteratorPublic.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
                user.setName(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
                user.setEmail(object.asLiteral().getString());
            }
        }

        return user;
    }

    public static List<User> find() {
        List<User> users = new ArrayList<User>();
        String queryString =
                "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "SELECT ?uri WHERE { " +
                        "  ?uri a prov:Group . " +
                        "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            User user = find(soln.getResource("uri").getURI());
            if(null != user){
                users.add(user);
            }
        }

        java.util.Collections.sort((List<User>) users);
        return users;
    }

    public static List<User> findMembers(String group_uri) {
        System.out.println("findMembers(" + group_uri + ") is called.");
        List<User> users = new ArrayList<User>();
        String queryString =
                "PREFIX prov: <http://www.w3.org/ns/prov#>  " +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "PREFIX hadatac: <http://hadatac.org/ont/hadatac#> " +
                        "SELECT ?uri WHERE { " +
                        "  ?uri a foaf:Person . " +
                        "  ?uri sio:SIO_000095 <" + group_uri + "> . " +
                        "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            User user = find(soln.getResource("uri").getURI());
            users.add(user);
        }

        java.util.Collections.sort((List<User>) users);
        return users;
    }
}
