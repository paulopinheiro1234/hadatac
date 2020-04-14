package org.hadatac.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.DataAcquisitionSchema;

public class NameSpace {

	@Field("abbreviation")
	private String nsAbbrev = "";

	@Field("name_str")
	private String nsName = "";

	@Field("mime_type_str")
	private String nsType = "";

	@Field("url_str")
	private String nsURL = "";

	@Field("number_of_loaded_triples_int")
	private int numberOfLoadedTriples = 0;

	@Field("priority_int")
	private int priority = -1;

	public NameSpace () {
	}

	public NameSpace (String abbrev, String name, String type, String url, int priority) {
		nsAbbrev = abbrev;
		nsName = name;
		nsType = type;
		nsURL = url;
		this.priority = priority;
	}

	public String getAbbreviation() {
		return nsAbbrev;
	}

	public void setAbbreviation(String abbrev) {
		nsAbbrev = abbrev;
	}

	public String getName() {
		return nsName;
	}

	public void setName(String name) {
		nsName = name;
	}

	public String getType() {
		return nsType;
	}

	public void setType(String type) {
		nsType = type;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

   public int getPriority() {
	    return priority;
    }

	public String getURL() {
		return nsURL;
	}

	public void setURL(String url) {
		nsURL = url;
	}

	public int getNumberOfLoadedTriples() {
	    return numberOfLoadedTriples;
    }

	public void setNumberOfLoadedTriples(int numberOfLoadedTriples) {
	    this.numberOfLoadedTriples = numberOfLoadedTriples;
    }

	public String toString() {
		if (nsAbbrev == null) {
			return "null";
		}
		String showType = "null";
		if (nsType != null)
			showType = nsType;
		if (nsURL == null)
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", NO URL)";
		else
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", " + nsURL + ")";
	}

	public void updateLoadedTripleSize() {
        try {
            String queryString = "SELECT (COUNT(*) as ?tot) \n"
                    + "FROM <" + getName() + "> \n"
                    + "WHERE { ?s ?p ?o . } \n";

            ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                    CollectionUtil.Collection.METADATA_SPARQL), queryString);
            QuerySolution soln = resultsrw.next();

            setNumberOfLoadedTriples(Integer.valueOf(soln.getLiteral("tot").getValue().toString()).intValue());
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getOntologyURIs() {
        List<String> uris = new ArrayList<String>();
        try {
           String queryString = "SELECT ?uri \n"
                   + "FROM <" + getName() + "> \n"
                   + "WHERE { \n"
                   + " ?ont <http://www.w3.org/2000/01/rdf-schema#subClassOf>* <http://www.w3.org/2002/07/owl#Ontology> . \n"
                   + " ?uri a ?ont . \n"
                   + "} ";

            ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                    CollectionUtil.Collection.METADATA_SPARQL), queryString);

            while(resultsrw.hasNext()){
               QuerySolution soln = resultsrw.next();
               uris.add(soln.getResource("uri").getURI());
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return uris;
    }

	public void loadTriples(String address, boolean fromRemote) {
        try {
            Repository repo = new SPARQLRepository(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            repo.init();
            RepositoryConnection con = repo.getConnection();
            ValueFactory factory = repo.getValueFactory();

            System.out.println("Loading triples from " + address);
            if (fromRemote) {
                con.add(new URL(address), "", getRioFormat(getType()), (Resource)factory.createIRI(getName()));
            } else {
                con.add(new File(address), "", getRioFormat(getType()), (Resource)factory.createIRI(getName()));
            }
            System.out.println("Loaded triples from " + address + " \n");
        } catch (NotFoundException e) {
            System.out.println("NotFoundException: address " + address);
            System.out.println("NotFoundException: " + e.getMessage());
        } catch (RiotNotFoundException e) {
            System.out.println("RiotNotFoundException: address " + address);
            System.out.println("RiotNotFoundException: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception: address " + address);
            System.out.println("Exception: " + e.getMessage());
        }
    }

	public void deleteTriples() {
	    deleteTriplesByNamedGraph(getName());
	}

        public static void deleteTriplesByNamedGraph(String namedGraphUri) {
	    if (!namedGraphUri.isEmpty()) {
		String queryString = "";
		queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
		queryString += "WITH <" + namedGraphUri + "> ";
		queryString += "DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . } ";

		UpdateRequest req = UpdateFactory.create(queryString);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(req,
					    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
		try {
		    processor.execute();
		    DataAcquisitionSchema.resetCache();
            } catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	}

	public int save() {
        if (priority == -1) {
           System.out.println("Warning priority was never initalized through the Namespace file");
        }
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();

            int status = client.addBean(this).getStatus();
            client.commit();
            client.close();
            return status;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] Namespace.save() - e.Message: " + e.getMessage());
            return -1;
        }
    }

	public int delete() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();
            UpdateResponse response = client.deleteById(getAbbreviation());
            client.commit();
            client.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] NameSpace.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] NameSpace.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] NameSpace.delete() - Exception message: " + e.getMessage());
        }

        return -1;
    }

	public static int deleteAll() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();
            UpdateResponse response = client.deleteByQuery("*:*");
            client.commit();
            client.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] NameSpace.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] NameSpace.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] NameSpace.delete() - Exception message: " + e.getMessage());
        }

        return -1;
    }

    public static NameSpace convertFromSolr(SolrDocument doc) {
        NameSpace object = new NameSpace();
        object.setAbbreviation(doc.getFieldValue("abbreviation").toString());
        object.setName(doc.getFieldValue("name_str").toString());
        object.setType(doc.getFieldValue("mime_type_str").toString());
        object.setURL(doc.getFieldValue("url_str").toString());
        object.setNumberOfLoadedTriples(Integer.valueOf(doc.getFieldValue("number_of_loaded_triples_int").toString()).intValue());
        object.setPriority(Integer.valueOf(doc.getFieldValue("priority_int").toString()).intValue());
        return object;
    }

    public static List<NameSpace> findByQuery(SolrQuery query) {
        List<NameSpace> list = new ArrayList<NameSpace>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            Iterator<SolrDocument> i = results.iterator();
            while (i.hasNext()) {
                list.add(convertFromSolr(i.next()));
            }
        } catch (Exception e) {
            list.clear();
            System.out.println("[ERROR] OperationMode.findByQuery(SolrQuery) - Exception message: " + e.getMessage());
        }

        return list;
    }

    public static List<NameSpace> findAll() {
        SolrQuery query = new SolrQuery();
        query.set("q", "*:*");
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    public static NameSpace findByAbbreviation(String abbreviation) {
        SolrQuery query = new SolrQuery();
        query.set("q", "abbreviation:\"" + abbreviation + "\"");
        query.set("rows", "10000000");
        List<NameSpace> namespaces = findByQuery(query);
        if (namespaces.isEmpty()) {
            return null;
        }

        return namespaces.get(0);
    }

    public static RDFFormat getRioFormat(String contentType) {
        if (contentType.contains("turtle")) {
            return RDFFormat.TURTLE;
        } else if (contentType.contains("rdf+xml")) {
            return RDFFormat.RDFXML;
        } else {
            return RDFFormat.NTRIPLES;
        }
    }
}
