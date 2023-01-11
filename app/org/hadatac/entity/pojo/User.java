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

import com.typesafe.config.ConfigFactory;
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
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONArray;

public class User implements Comparable<User> {

    private static final String basePrefix = ConfigProp.getBasePrefix();

    //public static String USER_GRAPH = "http://hadatac.org/kb/" + basePrefix + "/users";

    private String uri;
    private String given_name;
    private String family_name;
    private String name;
    private String email;
    private String homepage;
    private String comment;
    private String org_uri;
    private String immediateGroupUri;
    private String faceted_data_study;
    private String faceted_data_object;
    private String faceted_data_entity_characteristic;
    private String faceted_data_unit;
    private String faceted_data_time;
    private String faceted_data_space;
    private String faceted_data_platform;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getGivenName() {
        return given_name;
    }

    public void setGivenName(String name) {
        this.given_name = name;
    }

    public String getFamilyName() {
        return family_name;
    }

    public void setFamilyName(String name) {
        this.family_name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public String getFacetedDataStudy() {
        return faceted_data_study;
    }

    public void setFacetedDataStudy(String faceted_data_study) {
        this.faceted_data_study = faceted_data_study;
    }

    public String getFacetedDataObject() {
        return faceted_data_object;
    }

    public void setFacetedDataObject(String faceted_data_object) {
        this.faceted_data_object = faceted_data_object;
    }

    public String getFacetedDataEntityCharacteristic() {
        return faceted_data_entity_characteristic;
    }

    public void setFacetedDataEntityCharacteristic(String faceted_data_entity_characteristic) {
        this.faceted_data_entity_characteristic = faceted_data_entity_characteristic;
    }

    public String getFacetedDataUnit() {
        return faceted_data_unit;
    }

    public void setFacetedDataUnit(String faceted_data_unit) {
        this.faceted_data_unit = faceted_data_unit;
    }

    public String getFacetedDataTime() {
        return faceted_data_time;
    }

    public void setFacetedDataTime(String faceted_data_time) {
        this.faceted_data_time = faceted_data_time;
    }

    public String getFacetedDataSpace() {
        return faceted_data_space;
    }

    public void setFacetedDataSpace(String faceted_data_space) {
        this.faceted_data_space = faceted_data_space;
    }

    public String getFacetedDataPlatform() {
        return faceted_data_platform;
    }

    public void setFacetedDataPlatform(String faceted_data_platform) {
        this.faceted_data_platform = faceted_data_platform;
    }

    public boolean isAdministrator() {
        SysUser user = SysUser.findByEmail(getEmail());
        if(null != user){
            return user.isDataManager();
        }
        return false;
    }

    public boolean isValidated() {
        SysUser user = SysUser.findByEmail(getEmail());
        if(null != user) {
            return user.isEmailValidated();
        }
        return false;
    }

    public void getGroupNames(List<String> accessLevels) {
        if(getImmediateGroupUri() != null) {
            User user = UserGroup.find(getImmediateGroupUri());
            if(user != null){
                //System.out.println("Access Level: " + user.getUri());
                accessLevels.add(user.getUri());
                user.getGroupNames(accessLevels);
            }
        }
    }

    public void save() {
        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += "INSERT DATA {  ";
        //insert += "  GRAPH <" + USER_GRAPH + "> { ";
        insert += "     <" + this.getUri() + "> a foaf:Person . \n";
        insert += "     <" + this.getUri() + "> foaf:mbox " + "\"" + this.email + "\" . ";
        insert += "     <" + this.getUri() + "> sio:SIO_000095 " + "\"Public\" . ";
        if (this.faceted_data_study != null && !this.faceted_data_study.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasStudyFacetStatus " + "\"" + this.faceted_data_study + "\" . ";
        }
        if (this.faceted_data_object != null && !this.faceted_data_object.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasObjectFacetStatus " + "\"" + this.faceted_data_object + "\" . ";
        }
        if (this.faceted_data_entity_characteristic != null && !this.faceted_data_entity_characteristic.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasEntityCharacteristicFacetStatus " + "\"" + this.faceted_data_entity_characteristic + "\" . ";
        }
        if (this.faceted_data_unit != null && !this.faceted_data_unit.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasUnitFacetStatus " + "\"" + this.faceted_data_unit + "\" . ";
        }
        if (this.faceted_data_time != null && !this.faceted_data_time.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasTimeFacetStatus " + "\"" + this.faceted_data_time + "\" . ";
        }
        if (this.faceted_data_space != null && !this.faceted_data_space.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasSpaceFacetStatus " + "\"" + this.faceted_data_space + "\" . ";
        }
        if (this.faceted_data_platform != null && !this.faceted_data_platform.isEmpty()) {
            insert += "     <" + this.getUri() + "> hasco:hasPlatformFacetStatus " + "\"" + this.faceted_data_platform + "\" . ";
        }
        //insert += "  }";
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

    public static List<String> getUserEmails() {
        List<String> emails = new ArrayList<String>();
        for (User user : find()) {
            emails.add(user.getEmail());
        }

        return emails;
    }

    public static List<String> getUserURIs() {
        List<String> listUri = new ArrayList<String>();
        for (User user : find()) {
            listUri.add(user.getUri());
        }

        return listUri;
    }

    public static List<User> find() {
        List<User> users = new ArrayList<User>();
        String queryString = 
                "PREFIX prov: <http://www.w3.org/ns/prov#> " +
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
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
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
            else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/givenName")) {
                user.setGivenName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/familyName")) {
                user.setFamilyName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
                user.setName(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
                user.setEmail(object.asLiteral().getString());
                bHasEmail = true;
            }
            else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/homepage")) {
                String homepage = object.asLiteral().getString();
                if(homepage.startsWith("<") && homepage.endsWith(">")){
                    homepage = homepage.replace("<", "");
                    homepage = homepage.replace(">", "");
                }
                user.setHomepage(homepage);
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStudyFacetStatus")) {
                user.setFacetedDataStudy(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasObjectFacetStatus")) {
                user.setFacetedDataObject(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasEntityCharacteristicFacetStatus")) {
                user.setFacetedDataEntityCharacteristic(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasUnitFacetStatus")) {
                user.setFacetedDataUnit(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasTimeFacetStatus")) {
                user.setFacetedDataTime(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSpaceFacetStatus")) {
                user.setFacetedDataSpace(object.asLiteral().getString());
            }
            else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasPlatformFacetStatus")) {
                user.setFacetedDataPlatform(object.asLiteral().getString());
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

        if(bHasEmail){
            user.setUri(uri);
            return user;
        }

        return null;
    }

    public static User findByEmail(String email) {
        String queryString =
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
        if (group_uri.equals("Public")) {
            command += "DELETE { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> sio:SIO_000095 \"" + group_uri + "\" . "
                    //+ "    } "
                    + "} \n"
                    + "INSERT { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> sio:SIO_000095 \"" + group_uri + "\" . "
                    //+ "    } "
                    + "} \n "
                    + "WHERE { } \n";
        } else{
            command += "DELETE { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> sio:SIO_000095 <" + group_uri + "> .  "
                    //+ "    } "
                    + "} \n"
                    + "INSERT { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> sio:SIO_000095 <" + group_uri + "> . "
                    //+ "    } "
                    + "} \n "
                    + "WHERE { } \n";
        }

        UpdateRequest req = UpdateFactory.create(command);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
        processor.execute();
    }

    public void updateFacetPreferences() {
        try {
            String command = NameSpaces.getInstance().printSparqlNameSpaceList();
            command += "DELETE { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> hasco:hasStudyFacetStatus ?o1 . "
                    + "      <" + uri + "> hasco:hasObjectFacetStatus ?o2 . "
                    + "      <" + uri + "> hasco:hasEntityCharacteristicFacetStatus ?o3 . "
                    + "      <" + uri + "> hasco:hasUnitFacetStatus ?o4 . "
                    + "      <" + uri + "> hasco:hasTimeFacetStatus ?o5 . "
                    + "      <" + uri + "> hasco:hasSpaceFacetStatus ?o6 . "
                    + "      <" + uri + "> hasco:hasPlatformFacetStatus ?o7 . "
                    //+ "    } "
                    + "} \n"
                    + "WHERE { "
                    + "      <" + uri + "> hasco:hasStudyFacetStatus ?o1 . "
                    + "      <" + uri + "> hasco:hasObjectFacetStatus ?o2 . "
                    + "      <" + uri + "> hasco:hasEntityCharacteristicFacetStatus ?o3 . "
                    + "      <" + uri + "> hasco:hasUnitFacetStatus ?o4 . "
                    + "      <" + uri + "> hasco:hasTimeFacetStatus ?o5 . "
                    + "      <" + uri + "> hasco:hasSpaceFacetStatus ?o6 . "
                    + "      <" + uri + "> hasco:hasPlatformFacetStatus ?o7 . "
                    + "} \n";

            UpdateRequest req = UpdateFactory.create(command);
            UpdateProcessor processor1 = UpdateExecutionFactory.createRemote(
                    req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
            processor1.execute();
            command = NameSpaces.getInstance().printSparqlNameSpaceList();
            command += "INSERT { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> hasco:hasStudyFacetStatus \"" + this.faceted_data_study + "\"  . "
                    + "      <" + uri + "> hasco:hasObjectFacetStatus  \"" + this.faceted_data_object + "\"  . "
                    + "      <" + uri + "> hasco:hasEntityCharacteristicFacetStatus  \"" + this.faceted_data_entity_characteristic + "\"  . "
                    + "      <" + uri + "> hasco:hasUnitFacetStatus  \"" + this.faceted_data_unit + "\"  . "
                    + "      <" + uri + "> hasco:hasTimeFacetStatus  \"" + this.faceted_data_time + "\"  . "
                    + "      <" + uri + "> hasco:hasSpaceFacetStatus  \"" + this.faceted_data_space + "\"  . "
                    + "      <" + uri + "> hasco:hasPlatformFacetStatus  \"" + this.faceted_data_platform + "\"  . "
                    //+ "    } "
                    + "} \n "
                    + "WHERE { "
                    + "} \n";

            req = UpdateFactory.create(command);
            UpdateProcessor processor2 = UpdateExecutionFactory.createRemote(
                    req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
            processor2.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        queryString += "DELETE WHERE { "
                    //+ "    GRAPH <" + USER_GRAPH + "> { "
                    + "      <" + uri + "> ?p ?o . "
                    //+ "    } "
                    + " } ";
        UpdateRequest req = UpdateFactory.create(queryString);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
        processor.execute();
    }

    public String toGuiJson() {

        boolean updated = false;
        JSONArray gui = new JSONArray();

        if (this.faceted_data_study == null) {
            this.faceted_data_study = "on";
            updated = true;
        }
        if (this.faceted_data_study.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_object == null) {
            this.faceted_data_object = "on";
            updated = true;
        }
        if (this.faceted_data_object.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_entity_characteristic == null) {
            this.faceted_data_entity_characteristic = "on";
            updated = true;
        }
        if (this.faceted_data_entity_characteristic.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_unit == null) {
            this.faceted_data_unit = ConfigProp.getFacetedDataUnit();
            updated = true;
        }
        if (this.faceted_data_unit.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_time == null) {
            this.faceted_data_time = ConfigProp.getFacetedDataTime();
            updated = true;
        }
        if (this.faceted_data_time.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_space == null) {
            this.faceted_data_space = ConfigProp.getFacetedDataSpace();
            updated = true;
        }
        if (this.faceted_data_space.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (this.faceted_data_platform == null) {
            this.faceted_data_platform = ConfigProp.getFacetedDataPlatform();
            updated = true;
        }
        if (this.faceted_data_platform.equals("on")) {
            gui.add(true);
        } else {
            gui.add(false);
        }

        if (updated) {
            updateFacetPreferences();
        }
        return gui.toJSONString();
    }




    @Override
    public int compareTo(User another) {
        return this.getUri().compareTo(another.getUri());
    }

//    public static void updateUser(String uri, boolean updateUser, boolean deleteMember) {
//        if (deleteMember) {
//            for(User user : UserGroup.findMembers(uri)){
//                changeAccessLevel(user.getUri(), User.find(uri).getImmediateGroupUri());
//            }
//        }
//
//        if (deleteAuth){
//            User user = User.find(uri);
//            if(null != user){
//                SysUser sys_user = SysUser.findByEmail(user.getEmail());
//                if(null != sys_user){
//                    for (LinkedAccount acc : LinkedAccount.findByIdSolr(sys_user)) {
//                        acc.delete();
//                    }
//                    sys_user.delete();
//                }
//            }
//        }
//
//        String queryString = "";
//        queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
//        queryString += "DELETE WHERE { <" + uri + "> ?p ?o . } ";
//        UpdateRequest req = UpdateFactory.create(queryString);
//        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
//                req, CollectionUtil.getCollectionPath(CollectionUtil.Collection.PERMISSIONS_UPDATE));
//        processor.execute();
//    }
}
