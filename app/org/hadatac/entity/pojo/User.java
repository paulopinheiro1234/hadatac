package org.hadatac.entity.pojo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class User implements Comparable<User> {
	private String uri;
	private String given_name;
	private String family_name;
	private String name;
	private String email;
	private String homepage;
	private String comment;
	private String org_uri;
	private String immediateGroupUri;
	
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
	
	public String getGroupNamesUri() {
		String list = "";
		Map<String, String> nameList = new HashMap<String,String>();
		getGroupNames(nameList);
		Iterator<String> i = nameList.keySet().iterator();
		while (i.hasNext()) {
			list += i.next();
			if (i.hasNext()) {
				list += ",";
			}
		}
		return list;
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
	
	public void getGroupNames(Map<String, String> nameList) {
		if(getImmediateGroupUri() != null) {
			User user = UserGroup.find(getImmediateGroupUri());
			if(user != null){
				if (user.getName() != null) {
	    			nameList.put(user.getUri(), user.getName());
	    			user.getGroupNames(nameList);
				}
			}
		}
	}
	
	public void save() {
        String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += "INSERT DATA {  ";
    	insert += "<" + this.getUri() + "> a foaf:Person . \n";
    	insert += "<" + this.getUri() + ">  ";
    	insert += " foaf:mbox " + "\"" + this.email + "\" . ";
    	insert += "<" + this.getUri() + ">  ";
    	insert += " hadatac:isMemberOfGroup " + "\"Public\" . ";
    	insert += "}  ";
    	System.out.println("!!!! INSERT USER QUERY\n" + insert);
        
        UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, 
        		Collections.getCollectionsName(Collections.PERMISSIONS_UPDATE));
        processor.execute();
	}
	
	public static User create() {
		User user = new User();
		return user;
	}
	
	public static File outputAsTurtle() {
		String queryString = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ";
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
		Model model = qexec.execConstruct();
		
		File ttl_file = new File(UserManagement.getTurtlePath());
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(ttl_file);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		}
	    RDFDataMgr.write(outputStream, model, Lang.TURTLE);
        
		return ttl_file;
	}
	
	public static List<User> find() {
		List<User> users = new ArrayList<User>();
		String queryString = 
				"PREFIX prov: <http://www.w3.org/ns/prov#> " +
        		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT ?uri WHERE { " +
				"  ?uri a foaf:Person . " +
				"} ";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			User user = find(soln.getResource("uri").getURI());
			if(null != user){
				users.add(user);
			}
		}			

		java.util.Collections.sort((List<User>) users);
		return users;
	}
	
	public static User find(String uri) {	
		User user = new User();

		boolean bHasEmail = false;
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		
		Model modelPublic;
		Model modelPrivate;
		Statement statement;
		RDFNode object;
		
		QueryExecution qexecPrivate = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
		modelPrivate = qexecPrivate.execDescribe();

		StmtIterator stmtIteratorPrivate = modelPrivate.listStatements();
		
		while (stmtIteratorPrivate.hasNext()) {
			statement = stmtIteratorPrivate.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
				user.setComment(object.asLiteral().getString());
				System.out.println("comment: " + object.asLiteral().getString());
		    }
			else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hadatac#isMemberOfGroup")) {
				if(object.toString().equals("Public") || object.toString().equals("")){
					user.setImmediateGroupUri("Public");
					System.out.println("memberOfUri: " + "Public");
				}
				else{
					user.setImmediateGroupUri(object.asResource().toString());
					System.out.println("memberOfUri: " + object.asResource().toString());
				}
			}
			else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/givenName")) {
				user.setGivenName(object.asLiteral().getString());
				System.out.println("given_name: " + object.asLiteral().getString());
			}
			else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/familyName")) {
				user.setFamilyName(object.asLiteral().getString());
				System.out.println("family_name: " + object.asLiteral().getString());
			}
			else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
				user.setName(object.asLiteral().getString());
				System.out.println("name: " + object.asLiteral().getString());
			}
			else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
				user.setEmail(object.asLiteral().getString());
				bHasEmail = true;
				System.out.println("mbox: " + object.asLiteral().getString());
			}
			else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/homepage")) {
				String homepage = object.asLiteral().getString();
				if(homepage.startsWith("<") && homepage.endsWith(">")){
					System.out.println("homepage: " + homepage);
					homepage = homepage.replace("<", "");
					homepage = homepage.replace(">", "");
				}
				user.setHomepage(homepage);
				System.out.println("homepage: " + homepage);
		    }
		}
		
		QueryExecution qexecPublic = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		modelPublic = qexecPublic.execDescribe();
		
		StmtIterator stmtIteratorPublic = modelPublic.listStatements();
		while (stmtIteratorPublic.hasNext()) {
			statement = stmtIteratorPublic.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
				user.setName(object.asLiteral().getString());
				System.out.println("name: " + object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
				user.setEmail(object.asLiteral().getString());
				bHasEmail = true;
				System.out.println("mbox: " + object.asLiteral().getString());
			}
		}
		
		if(bHasEmail){
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
		
		String command = "";
		System.out.println("group_uri is " + group_uri);
		if(group_uri.equals("Public")){
			command = "DELETE { <" + uri + "> <http://hadatac.org/ont/hadatac#isMemberOfGroup> \"" + group_uri + "\" .  } \n"
					+ "INSERT { <" + uri + "> <http://hadatac.org/ont/hadatac#isMemberOfGroup> \"" + group_uri + "\" . } \n "
					+ "WHERE { } \n";
		}
		else{
			command = "DELETE { <" + uri + "> <http://hadatac.org/ont/hadatac#isMemberOfGroup> <" + group_uri + "> .  } \n"
					+ "INSERT { <" + uri + "> <http://hadatac.org/ont/hadatac#isMemberOfGroup> <" + group_uri + "> . } \n "
					+ "WHERE { } \n";
		}
		
		System.out.println(command);
		UpdateRequest req = UpdateFactory.create(command);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				req, Collections.getCollectionsName(Collections.PERMISSIONS_UPDATE));
		processor.execute();
	}
	
	public static void deleteUser(String uri) {
		for(User user : UserGroup.findMembers(uri)){
			changeAccessLevel(user.getUri(), User.find(uri).getImmediateGroupUri());
		}
		
		User user = User.find(uri);
		if(null != user){
			SysUser sys_user = SysUser.findByEmail(user.getEmail());
			if(null != sys_user){
				sys_user.delete();
			}
		}
		
		String queryString = "";
		queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		queryString += "DELETE WHERE { <" + uri + "> ?p ?o . } ";
		System.out.println(queryString);
		UpdateRequest req = UpdateFactory.create(queryString);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				req, Collections.getCollectionsName(Collections.PERMISSIONS_UPDATE));
		processor.execute();
	}
	
	@Override
    public int compareTo(User another) {
		return this.getUri().compareTo(another.getUri());
    }
}
