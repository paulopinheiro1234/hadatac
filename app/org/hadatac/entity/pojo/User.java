package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class User {
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
	
	public boolean isAdminstrator() {
		return administrator;
	}

	public void setAdminisrator(boolean administrator) {
		this.administrator = administrator;
	}
	
	public Map<String, String> getGroupNames(String uri) {
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
		    } else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/member")) {
			     user.immediateGroup = User.find(object.asResource().getURI());
		    }
		}
		
		QueryExecution qexecPublic = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		modelPublic = qexecPublic.execDescribe();
		
		StmtIterator stmtIteratorPublic = modelPublic.listStatements();
		while (stmtIteratorPublic.hasNext()) {
			statement = stmtIteratorPrivate.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/name")) {
				user.setName(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/mbox")) {
				user.setEmail(object.asLiteral().getString());
			} else if (statement.getPredicate().getURI().equals("http://xmlns.com/foaf/0.1/homepage")) {
				user.setHomepage(object.asLiteral().getString());
			}
		}
		
		return user;
	}
	
}
