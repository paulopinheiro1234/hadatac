package org.hadatac.metadata.api;

import java.net.URISyntaxException;
import java.util.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.*;
import org.eclipse.rdf4j.model.util.RDFCollections;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.GSPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetadataFactory {

	private static final Logger log = LoggerFactory.getLogger(MetadataFactory.class);

	public static Model createModel(List<Map<String, Object>> rows, String namedGraphUri) {

		ModelFactory modelFactory = new LinkedHashModelFactory();
        Model model = modelFactory.createEmptyModel();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI namedGraph = null;
        if (!namedGraphUri.isEmpty()) {
            namedGraph = factory.createIRI(namedGraphUri);
        }

        for (Map<String, Object> row : rows) {
        	if (row == null || row.get("hasURI") == null) {
        		System.out.println("[ERROR] MetadataFactory.createModel() failed because the 'hasURI' row is missing");
        	} else {
        		// debug
				if (row.get("hasURI").toString().contains("ZBFA")) {
					int x = 1;
				}
				// end of debug
	            IRI sub = factory.createIRI(URIUtils.replacePrefixEx((String)row.get("hasURI")));
	            for (String key : row.keySet()) {

	            	if ("hasURI".equals(key)) continue;

	                IRI pred = null;
	                if ("a".equals(key)) {
	                	pred = factory.createIRI(URIUtils.replacePrefixEx("rdf:type"));
	                } else {
	                	pred = factory.createIRI(URIUtils.replacePrefixEx(key));
	                }

	                if ( pred != null && pred.getLocalName().contains("hasAttribute") ) {
	                	addAttributeListToModel(model, key, row, sub, pred, namedGraph);
	                	continue;
					}

	                String cellValue = (String)row.get(key);
	                if (URIUtils.isValidURI(cellValue)) {
	                	IRI obj = factory.createIRI(URIUtils.replacePrefixEx(cellValue));
	                	if (namedGraph == null) {
	                		model.add(sub, pred, obj);
	                	} else {
	                		model.add(sub, pred, obj, (Resource)namedGraph);
	                	}
	                } else {
	                	if (cellValue == null) {
	                		cellValue = "NULL";
	                	}
	                	Literal obj = factory.createLiteral(
	                                cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
	                	if (namedGraph == null) {
	                		model.add(sub, pred, obj);
	                	} else {
	                		model.add(sub, pred, obj, (Resource)namedGraph);
	                	}
	                }

	            } // end of for-loop

        	}
        }

        return model;
    }

	private static void addAttributeListToModel(Model model, String key, Map<String, Object> row, IRI subj, IRI pred, IRI namedGraph) {

		/*
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m : http://hadatac.org/ont/hasco/hasAttribute : http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/1
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/1 : http://www.w3.org/1999/02/22-rdf-syntax-ns#type : http://www.w3.org/1999/02/22-rdf-syntax-ns#List
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/1 : http://www.w3.org/1999/02/22-rdf-syntax-ns#first : http://purl.obolibrary.org/obo/STATO_0000104
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/1 : http://www.w3.org/1999/02/22-rdf-syntax-ns#rest : http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/2
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/2 : http://www.w3.org/1999/02/22-rdf-syntax-ns#first : http://purl.obolibrary.org/obo/CMO_0000012
		http://hadatac.org/kb/hhear#DASA-2017-1740-WHZ-6m/2 : http://www.w3.org/1999/02/22-rdf-syntax-ns#rest : http://www.w3.org/1999/02/22-rdf-syntax-ns#nil
		*/

		// debug
		if ( subj.toString().contains("ZBFA")) {
			int x = 1;
		}
		// end of debug

		SimpleValueFactory simpleValueFactory = SimpleValueFactory.getInstance();
		BNode head = simpleValueFactory.createBNode();

		Object objValue = row.get(key);
		List<String> attributes = new ArrayList<>();

		if ( objValue instanceof String ) {
			attributes.add((String)objValue);
		} else if ( objValue instanceof ArrayList ) {
			attributes = (ArrayList<String>)objValue;
			attributes.remove(attributes.size()-1);
		}

		List<IRI> attributeIRIs = new ArrayList<>();
		for ( String attribute : attributes ) {
			attributeIRIs.add(simpleValueFactory.createIRI(URIUtils.replacePrefixEx(attribute)));
		}
		Collections.reverse(attributeIRIs);

		Model tmpModel = RDFCollections.asRDF(attributeIRIs, head, new LinkedHashModel());
		tmpModel.add(subj, pred, head);

		tmpModel.forEach(statement -> {
			model.add(statement.getSubject(), statement.getPredicate(), statement.getObject(), (Resource)namedGraph);
		});

	}

    public static int commitModelToTripleStore(Model model, String endpointUrl) {
		try {
			GSPClient gspClient = new GSPClient(endpointUrl);
			gspClient.postModel(model);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

        return model.size();
    }
}
