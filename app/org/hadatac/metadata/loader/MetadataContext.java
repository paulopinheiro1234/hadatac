package org.hadatac.metadata.loader;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;


public class MetadataContext implements RDFContext {

    String username = null;
    String password = null;
    String kbURL = null;
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";

    public MetadataContext(String un, String pwd, String kb, boolean ver) {
        username = un;
        password = pwd;
        kbURL = kb;
        verbose = ver;
    }

    public static Long playTotalTriples() {
        MetadataContext metadata = new MetadataContext(
                "user", "password",
                ConfigFactory.load().getString("hadatac.solr.triplestore"),
                false);
        return metadata.totalTriples();
    }

    public Long totalTriples() {
        try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                    "SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";

            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

            QuerySolution soln = resultsrw.next();

            return Long.valueOf(soln.getLiteral("tot").getValue().toString()).longValue();
        } catch (Exception e) {
            return (long) -1;
        }
    }

    public String clean(int mode) {
        String message = "";
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        message += Feedback.println(mode, " ");

        String queryString = "";
        queryString += NameSpaces.getInstance().printSparqlNameSpaceList();
        queryString += "DELETE WHERE { ?s ?p ?o . } ";
        UpdateRequest req = UpdateFactory.create(queryString);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(req,
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        try {
            processor.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        message += Feedback.println(mode, " ");
        message += Feedback.println(mode, " ");
        message += Feedback.print(mode, "   Triples after [clean]: " + totalTriples());

        return message;
    }

    public String cleanStudy(int mode, String study) {
        String message = "";
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        NameSpace.deleteTriplesByNamedGraph(study);
        message += Feedback.print(mode, "   Triples after [clean]: " + totalTriples());

        return message;
    }

    public String getLang(String contentType) {
        if (contentType.contains("turtle")) {
            return "TTL";
        } else if (contentType.contains("rdf+xml")) {
            return "RDF/XML";
        } else {
            return "";
        }
    }

    /*
     *   contentType correspond to the mime type required for curl to process the data provided. For example, application/rdf+xml is
     *   used to process rdf/xml content.
     */
    public Long loadLocalFile(int mode, String filePath, String contentType, String graphUri) {
        Long total = totalTriples();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Repository repo = new SPARQLRepository(
                        kbURL + CollectionUtil.getCollectionName(CollectionUtil.Collection.METADATA_GRAPH.get()));
                repo.init();
                RepositoryConnection con = repo.getConnection();
                ValueFactory factory = repo.getValueFactory();
                if (graphUri.isEmpty()) {
                	con.add(file, "", NameSpace.getRioFormat(contentType), (Resource)factory.createBNode());
                } else {
                	con.add(file, "", NameSpace.getRioFormat(contentType), (Resource)factory.createIRI(graphUri));
                }
            }
        } catch (NotFoundException e) {
            System.out.println("NotFoundException: file " + filePath);
            System.out.println("NotFoundException: " + e.getMessage());
            e.printStackTrace();
        } catch (RiotNotFoundException e) {
            System.out.println("RiotNotFoundException: file " + filePath);
            System.out.println("RiotNotFoundException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Exception: graphUri [" + graphUri + "]");
            System.out.println("Exception: file " + filePath);
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
        }

        Long newTotal = totalTriples();
        return (newTotal - total);
    }

    /*
     *  oper: "confirmed" cache ontologies from the web and load
     *        "confrmedCache" load from cached ontologies
     *        "cache" cache ontologies from the web
     */
    public String loadOntologies(int mode, String oper) {
        String message = "";
        if ("cache".equals(oper)) {
            message += NameSpaces.getInstance().copyNameSpacesLocally(mode);
        } else {
            Long total = totalTriples();
            message += Feedback.println(mode, "   Triples before [loadOntologies]: " + total);
            message += Feedback.println(mode," ");

            ConcurrentHashMap<String, NameSpace> namespaces = NameSpaces.getInstance().getNamespaces();
            for (String abbrev : namespaces.keySet()) {
                NameSpace ns = namespaces.get(abbrev);
                String nsURL = ns.getURL();
                if (abbrev != null && nsURL != null && !nsURL.equals("") && ns.getType() != null) {
                    String path = "";
                    if ("confirmed".equals(oper)) {
                        ns.loadTriples(nsURL, true);
                        path = nsURL;
                    } else if ("confirmedCache".equals(oper)) {
                        String filePath = NameSpaces.CACHE_PATH + "copy" + "-" + abbrev.replace(":", "");
                        message += Feedback.print(mode, "   Uploading " + filePath);
                        for (int i = filePath.length(); i < 50; i++) {
                            message += Feedback.print(mode, ".");
                        }
                        loadLocalFile(mode, filePath, ns.getType(), ns.getName());
                        path = filePath;
                    }

                    Long newTotal = totalTriples();
                    message += Feedback.println(mode, "   Added " + (newTotal - total) + " triples from " + path + " .");
                    total = newTotal;
                }
            }
            message += Feedback.println(mode," ");
            message += Feedback.println(mode, "   Triples after [loadOntologies]: " + totalTriples());
        }
        NameSpaces.getInstance().reload();

        return message;
    }
}
