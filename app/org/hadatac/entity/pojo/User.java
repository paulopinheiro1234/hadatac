package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.views.html.deployments.newDeployment;
import org.hadatac.data.loader.util.Sparql;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class User implements Comparable<User> {
	
	public static String INDENT1 = "     ";
	
	public static String INSERT_LINE1 = "INSERT DATA {  ";
    
    public static String LINE3 = INDENT1 + "a         prov:Person, foaf:Person;  ";
    
    public static String MBOX_PREDICATE =     INDENT1 + "foaf:mbox        ";
    
    public static String TIME_XMLS =   "\"^^<http://www.w3.org/2001/XMLSchema#dateTime> .";
    
    public static String LINE_LAST = "}  ";
	
	private String uri;
	private String name;
	private String email;
	private String homepage;
	private String label;
	private User immediateGroup;
	private boolean administrator;
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		System.out.println("setName: " + name);
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public User getImmediateGroup() {
		return immediateGroup;
	}

	public void setImmediateGroup(User immediateGroup) {
		this.immediateGroup = immediateGroup;
	}
	
	public boolean isAdministrator() {
		return administrator;
	}

	public void setAdministrator(boolean administrator) {
		this.administrator = administrator;
	}
	
	public String getGroupNamesUri() {
		String list = "";
		Map<String, String> map = getGroupNames();
		Iterator<String> i = map.keySet().iterator();
		while (i.hasNext()) {
			list += i.next();
			if (i.hasNext()) {
				list += ",";
			}
		}
		return list;
	}
	
	public Map<String, String> getGroupNames() {
		Map<String, String> nameList = new HashMap<String,String>();
		User user = User.find(uri);
		if (user != null && user.getUri() != null && user.getName() != null) {
			nameList.put(user.getUri(),user.getName());
			while (user.getImmediateGroup() != null) {
				user = user.getImmediateGroup();
				if (user.getName() != null) {
    				nameList.put(user.getUri(), user.getName());
				}
			}
		}
		return nameList;
	}
	
	public void save() {
		User tmpUser = User.find(uri);

		boolean updateAdministrator = this.administrator != tmpUser.isAdministrator();
			
        if (updateAdministrator) {
        	//"DELETE DATA { <http://example/book3>  dc:title  x }"
        	//"INSERT DATA { <http://example/book3>  dc:title  y }"
        }
        
        String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
    	insert += "<" + this.getUri() + ">  ";
    	insert += MBOX_PREDICATE + "\"" + this.email + "\" ;   ";
    	insert += LINE_LAST;
    	
    	System.out.println("!!!! INSERT USER QUERY\n" + insert);
        
        UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL));
        processor.execute();
        
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(Play.application().configuration().getString("hadatac.solr.triplestore")
        		+ "/store_users/update?commit=true");
        try {
			httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static User create() {
		User user = new User();
		return user;
	}
	
	public static List<User> find() {
		List<User> users = new ArrayList<User>();
		String queryString = 
				"PREFIX prov: <http://www.w3.org/ns/prov#>  " +
        		"PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
				"SELECT ?uri WHERE { " +
				"  ?uri a foaf:Person . " +
				"} ";
		
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			System.out.println("URI from main query: " + soln.getResource("uri").getURI());
			User user = find(soln.getResource("uri").getURI());
			users.add(user);
		}			

		java.util.Collections.sort((List<User>) users);
		return users;
	}
	
	public static User find(String uri) {
		User user = null;
		Model modelPublic;
		Model modelPrivate;
		Statement statement;
		RDFNode object;
		
		user = new User();
		user.setUri(uri);
		
		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexecPrivate = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.PERMISSIONS_SPARQL), query);
		modelPrivate = qexecPrivate.execDescribe();

		StmtIterator stmtIteratorPrivate = modelPrivate.listStatements();
		while (stmtIteratorPrivate.hasNext()) {
			statement = stmtIteratorPrivate.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				user.setLabel(object.asLiteral().getString());
				System.out.println("label: " + object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/member")) {
		    	User group = new User();
		    	group.setUri(object.asResource().getURI());
			    user.setImmediateGroup(group);
			} else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
				user.setName(object.asLiteral().getString());
				System.out.println("name: " + object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
				user.setEmail(object.asLiteral().getString());
				System.out.println("mbox: " + object.asLiteral().getString());
			}
			//else if (statement.getPredicate().getURI().equals("")) {
				//if (object.asLiteral().getString().equals("true")) {
				  //  user.setAdministrator(true);
				//} else {
				 //   user.setAdministrator(false);
				//}
				//System.out.println("mbox: " + object.asLiteral().getString());
		    //}
		}
		
		QueryExecution qexecPublic = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
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
				System.out.println("mbox: " + object.asLiteral().getString());
			}
		}
		
		return user;
	}
	
	@Override
    public int compareTo(User another) {
        //return this.getName().compareTo(another.getName());
		return this.getUri().compareTo(another.getUri());
    }
}
