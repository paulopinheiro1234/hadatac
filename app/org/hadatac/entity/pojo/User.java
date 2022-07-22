package org.hadatac.entity.pojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.LinkedAccount;
import org.hadatac.console.models.SysUser;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.vocabularies.FOAF;
import org.hadatac.vocabularies.HASCO;
import org.hadatac.vocabularies.RDFS;

public class User extends Agent {

    private String email;
    private String homepage;
    private String comment;
    private String org_uri;
    private String immediateGroupUri;

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getLabel() {
        if (this.label == null || label.isEmpty()) {
            return this.name;
        }
        return this.label;
    }

    @Override
    public String getTypeUri() {
        if (this.typeUri == null || typeUri.isEmpty()) {
            return FOAF.PERSON;
        }
        return this.typeUri;
    }

    @Override
    public String getHascoTypeUri() {
        if (this.hascoTypeUri == null || hascoTypeUri.isEmpty()) {
            return HASCO.USER;
        }
        return this.hascoTypeUri;
    }

    @JsonIgnore
    public String getImmediateGroupUri() {
        return immediateGroupUri;
    }

    public void setImmediateGroupUri(String group_uri) {
        this.immediateGroupUri = group_uri;
    }

    public String getOrgUri(){
        return org_uri;
    }

    public void setOrgUri(String uri){
        org_uri = uri;
    }

    @JsonIgnore
    public boolean isAdministrator() {
        SysUser user = SysUser.findByEmail(getEmail());
        if(null != user){
            return user.isDataManager();
        }
        return false;
    }

    @JsonIgnore
    public boolean isValidated() {
        SysUser user = SysUser.findByEmail(getEmail());
        if(null != user) {
            return user.isEmailValidated();
        }
        return false;
    }

    @JsonIgnore
    public void getGroupNames(List<String> accessLevels) {
        if(getImmediateGroupUri() != null) {
            User user = UserGroup.find(getImmediateGroupUri());
            if(user != null){
                System.out.println("Access Level: " + user.getUri());
                accessLevels.add(user.getUri());
                user.getGroupNames(accessLevels);
            }
        }
    }

    public void save() {
        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += "INSERT DATA {  ";
        insert += "<" + this.getUri() + "> a <" + FOAF.PERSON + "> . \n";
        insert += "<" + this.getUri() + "> hasco:hascoType <" + HASCO.USER + "> . \n";
        insert += "<" + this.getUri() + "> foaf:mbox \"" + this.email + "\" . ";
        insert += "<" + this.getUri() + "> sio:SIO_000095 \"Public\" . ";
        insert += "}  ";
        //System.out.println("!!!! INSERT USER");

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, 
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }
    }

    public static User create() {
        User user = new User();
        return user;
    }

    public static String outputAsTurtle() {
        String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ";
        Query query = QueryFactory.create(queryString);

        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_SPARQL), query);
        Model model = qexec.execConstruct();

        File ttl_file = new File(UserManagement.getTurtlePath());
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(ttl_file);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        RDFDataMgr.write(outputStream, model, Lang.TURTLE);

        String result = "";
        try {
            result = new String(Files.readAllBytes(
                    Paths.get(UserManagement.getTurtlePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @JsonIgnore
    public static List<String> getUserEmails() {
        List<String> emails = new ArrayList<String>();
        for (User user : find()) {
            emails.add(user.getEmail());
        }

        return emails;
    }

    @JsonIgnore
    public static List<String> getUserURIs() {
        List<String> listUri = new ArrayList<String>();
        for (User user : find()) {
            listUri.add(user.getUri());
        }

        return listUri;
    }

    public static List<User> find() {
        List<User> users = new ArrayList<User>();
        String queryString = "PREFIX prov: <http://www.w3.org/ns/prov#> " +
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                        "SELECT ?uri WHERE { " +
                        "  ?uri a foaf:Person . " +
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

    public static User findByEmail(String email) {
        String queryString =
                "PREFIX prov: <http://www.w3.org/ns/prov#> " +
                "PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
                "SELECT ?uri WHERE { " +
                "  ?uri a foaf:Person . " +
                "  ?uri foaf:mbox \"" + email + "\" . " +
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_SPARQL), queryString);

        if (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            User user = find(soln.getResource("uri").getURI());
            if (null != user) {
                return user;
            }
        }
        return null;
    }


    public static User find(String uri) {	
        User user = null;

        boolean bHasEmail = false;
        String queryString = "DESCRIBE <" + uri + ">";

        Statement statement;
        RDFNode object;

        Model modelPrivate = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.PERMISSIONS_SPARQL), queryString);
        
        if (!modelPrivate.isEmpty()) {
            user = new User();
        }

        StmtIterator stmtIteratorPrivate = modelPrivate.listStatements();
        while (stmtIteratorPrivate.hasNext()) {
            statement = stmtIteratorPrivate.next();
            if (!statement.getSubject().getURI().equals(uri)) {
                continue;
            }

            object = statement.getObject();
            if (statement.getPredicate().getURI().equals(RDFS.COMMENT)) {
                user.setComment(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals(URIUtils.replacePrefixEx("sio:SIO_000095"))) {
                if(object.toString().equals("Public") || object.toString().equals("")){
                    user.setImmediateGroupUri("Public");
                }
                else{
                    user.setImmediateGroupUri(object.asResource().toString());
                }
            }
            else if (statement.getPredicate().getURI().equals(FOAF.GIVEN_NAME)) {
                user.setGivenName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals(FOAF.FAMILY_NAME)) {
                user.setFamilyName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals(FOAF.NAME)) {
                user.setName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals(FOAF.MBOX)) {
                user.setEmail(object.asLiteral().getString());
                bHasEmail = true;
            }
            else if (statement.getPredicate().getURI().equals(FOAF.HOMEPAGE)) {
                String homepage = object.asLiteral().getString();
                if(homepage.startsWith("<") && homepage.endsWith(">")){
                    homepage = homepage.replace("<", "");
                    homepage = homepage.replace(">", "");
                }
                user.setHomepage(homepage);
            }
        }

        Model modelPublic = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);        
        
        if (!modelPublic.isEmpty() && user == null) {
            user = new User();
        }

        StmtIterator stmtIteratorPublic = modelPublic.listStatements();
        while (stmtIteratorPublic.hasNext()) {
            statement = stmtIteratorPublic.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
                user.setName(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
                user.setEmail(object.asLiteral().getString());
                bHasEmail = true;
            }
        }

        if (bHasEmail) {
            user.setUri(uri);
            return user;
        }

        return null;
    }

    public static void changeAccessLevel(String uri, String group_uri) {
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String command = NameSpaces.getInstance().printSparqlNameSpaceList();
        if(group_uri.equals("Public")){
            command += "DELETE { <" + uri + "> sio:SIO_000095 \"" + group_uri + "\" .  } \n"
                    + "INSERT { <" + uri + "> sio:SIO_000095 \"" + group_uri + "\" . } \n "
                    + "WHERE { } \n";
        }
        else{
            command += "DELETE { <" + uri + "> sio:SIO_000095 <" + group_uri + "> .  } \n"
                    + "INSERT { <" + uri + "> sio:SIO_000095 <" + group_uri + "> . } \n "
                    + "WHERE { } \n";
        }

        UpdateRequest req = UpdateFactory.create(command);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
        processor.execute();
    }

    public static void deleteUser(String uri, boolean deleteAuth, boolean deleteMember) {
        if (deleteMember) {
            for(User user : UserGroup.findMembers(uri)){
                changeAccessLevel(user.getUri(), User.find(uri).getImmediateGroupUri());
            }
        }

        if (deleteAuth){
            User user = User.find(uri);
            if(null != user){
                SysUser sys_user = SysUser.findByEmail(user.getEmail());
                if(null != sys_user){
                    for (LinkedAccount acc : LinkedAccount.findByIdSolr(sys_user)) {
                        acc.delete();
                    }
                    sys_user.delete();
                }
            }
        }

        String queryString = "";
        queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
        queryString += "DELETE WHERE { <" + uri + "> ?p ?o . } ";
        UpdateRequest req = UpdateFactory.create(queryString);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
        processor.execute();
    }

}
