package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.hadatac.data.loader.util.Sparql;
import org.hadatac.utils.Collections;

import play.Play;

public class User implements Comparable<User> {
	
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
			    user.immediateGroup = User.find(object.asResource().getURI());
			} //else if (statement.getPredicate().getURI().equals("")) {
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
			} //else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/homepage")) {
				//user.setHomepage(object.asLiteral().getString());
				//System.out.println("homepage: " + object.asLiteral().getString());
			//}
		}
		
		return user;
	}
	
	@Override
    public int compareTo(User another) {
        return this.getName().compareTo(another.getName());
    }
	
}
