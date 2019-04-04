package org.hadatac.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;

import com.google.refine.clustering.binning.FingerprintKeyer;

public class TermClusteror {

   private final String ontologyTermQuery = "" +
	         "select distinct ?class ?label\n" +
	         "where{\n" +
	         "  ?class a owl:Class .\n" +
	         "  ?class rdfs:label ?label .\n" +
	         "}";

	public Map<String, List<OntologyTerm>> getTermClusters() {
		
		final Map<String, List<OntologyTerm>> clusters = new HashMap<>();
		final FingerprintKeyer fp = new FingerprintKeyer();
		
		try {
			final String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + ontologyTermQuery;
			
			ResultSetRewindable resultsrw = SPARQLUtils.select(
			        CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

			while (resultsrw.hasNext()) {
				// Get class term and generate cluster
				final QuerySolution result = resultsrw.next();
				final OntologyTerm term = new OntologyTerm(result.get("class").asResource(),
						result.get("label").asLiteral().getString());
				final String clusterLabel = fp.key(term.classTerm.split("<")[0]); // Removes sections of labels with <>,
																					// this mimics open refine
																					// functionality

				// Update cluster map
				if (!clusters.keySet().contains(clusterLabel)) { // this is a new cluster so initialize key
					clusters.put(clusterLabel, new LinkedList<OntologyTerm>());
				}

				// check to ensure this is not a duplicate IRI
				if (!clusters.get(clusterLabel).contains(term)) {
					clusters.get(clusterLabel).add(term);
				}
			}

		} catch (Exception e) {
			System.err.println("Ran into error when Term Clustering :" + e.getMessage());
		}

		return refineClusters(clusters);
	}

	private static Map<String, List<OntologyTerm>> refineClusters(Map<String, List<OntologyTerm>> clusters) {
		// Remove Singleton Clusters
		Map<String, List<OntologyTerm>> output = new HashMap<>(clusters);
		for (final String key : clusters.keySet()) {
			if (clusters.get(key).size() == 1) {
				output.remove(key);
			}
		}

		clusters = null;
		return output;
	}
}
