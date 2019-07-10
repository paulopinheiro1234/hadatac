package ingestDataTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.hadatac.console.controllers.triplestore.Clean;
import org.hadatac.console.http.SPARQLUtils;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import com.typesafe.config.ConfigFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.CollectionUtil.Collection;

public class CleanDataTest extends StepTest{
	private static final CleanDataTest test = new CleanDataTest();

	private CleanDataTest() {}
	
	public static CleanDataTest getTest() {
		return test;
	}
	
	
	@Override
	public void preMsg() {
		System.out.println("[Step 2] Executing cleanDataTest:");
		
	}
	
	
	@Override
	public void verifyPre() {
		return;
		
	}
	
	@Override
	public void test() {
		//Clean Data Button
		Clean.playClean("acquisitions");
		checkDataClean("Clean Data", Collection.DATA_ACQUISITION.get());
		
		//Clean Data Acquisitions Button
		Clean.playClean("collections");
		checkDataClean("Clean Data Acquisitions", Collection.DATA_COLLECTION.get());
		
		//Clean MetaData
		Clean.playClean("metadata");
		//check if org.hadatac.metadata.loader.MetadataContext.totalTriples() returns 0
		try {
            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                    "SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";

            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

            QuerySolution soln = resultsrw.next();

            assertTrue("Triples after [Clean MetaData] not being 0", Long.valueOf(soln.getLiteral("tot").getValue().toString()).longValue() == 0);
        } catch (Exception e) {
        	e.printStackTrace();
			fail();
        }
		System.out.println("[Step 2] Clean MetaData Pass.");
	}

	@Override
	public int step() {
		return 2;
	}

	//check if org.hadatac.data.loader.DataContext.totalDocuments() returns 0
	private void checkDataClean(String buttonName, String collection)
	{
		//assert number of triples in DATA_ACQUISITION being 0
		SolrClient solr = new HttpSolrClient.Builder(ConfigFactory.load().getString("hadatac.solr.data") + collection).build();
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		try
		{
			QueryResponse response = solr.query(parameters);
			solr.close();
			assertTrue("Triples after [" + buttonName + "] not being 0", response.getResults().getNumFound() == 0);
		}
		catch (SolrServerException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		System.out.println("[Step 2] "+buttonName+" Pass.");
	}

}
