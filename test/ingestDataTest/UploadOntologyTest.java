package ingestDataTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.hadatac.console.controllers.triplestore.LoadOnt;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;
import com.typesafe.config.ConfigFactory;

public class UploadOntologyTest extends StepTest{
	private static UploadOntologyTest test = new UploadOntologyTest();
	private int CHEARTRIPLE = 5339;
	private int COGATTRIPLE = 22620;
	private int DOIDTRIPLE = 222793;
	private int HASCOTRIPLE = 511;
	private int OWLTRIPLE = 450;
	private int PATOTRIPLE = 33947;
	private int PROVTRIPLE = 1617;
	private int RDFTRIPLE = 102;
	private int RDFSTRIPLE = 87;
	private int SIOTRIPLE = 12184;
	private int UOTRIPLE = 5674;
	private int VSTOITRIPLE = 497;
	private int TOTALTRIPLE = 305821;

	private UploadOntologyTest() {}

	public static UploadOntologyTest getTest()
	{
		return test;
	}

	@Override
	public void test() {

      NameSpace[] ontologyList = new NameSpace[] {
         new NameSpace ("chear", "copy-chear", "Ontology", "<http://hadatac.org/ont/chear#>"),
         // new NameSpace ("cogat", "copy-cogat", "Ontology", "<http://www.cognitiveatlas.org/ontology/cogat.owl#CAO_>"),
         // new NameSpace ("doid", "copy-doid", "Ontology", "<http://purl.obolibrary.org/obo/DOID_>"),
         new NameSpace ("hasco", "copy-hasco", "Ontology", "<http://hadatac.org/ont/hasco/>"),
         new NameSpace ("owl", "copy-owl", "Ontology", "owl:"),
         // new NameSpace ("pato", "copy-pato", "Ontology", "<http://purl.obolibrary.org/obo/PATO_>"),
         // new NameSpace ("prov", "copy-prov", "Ontology", "<http://www.w3.org/ns/prov#>"),
         // new NameSpace ("rdf", "copy-rdf", "Ontology", "rdf:"),
         // new NameSpace ("rdfs", "copy-rdfs", "Ontology", "rdfs:"),
         new NameSpace ("sio", "copy-sio", "Ontology", "<http://semanticscience.org/resource/>"),
         // new NameSpace ("uo", "copy-uo", "Ontology", "<http://purl.obolibrary.org/obo/UO_>"),
         new NameSpace ("vstoi", "copy-vstoi", "Ontology", "<http://hadatac.org/ont/vstoi#>")
      };


      for(NameSpace ns : ontologyList){
         cleanCache();

         try {
            Files.copy(java.nio.file.Paths.get("test/src/" + ns.getName()), java.nio.file.Paths.get(NameSpaces.CACHE_PATH + ns.getName()), StandardCopyOption.REPLACE_EXISTING);
         } catch(IOException e) {
            fail("Fail to copy ontologies from test/src to tmp/cache");
         }

         NameSpaces.getInstance();
         MetadataContext metadata = new MetadataContext(
            "user", "password",
            ConfigFactory.load().getString("hadatac.solr.triplestore"),
            false);

         System.out.println(metadata.loadOntologies(0, "confirmedCache"));

         //check result after upload ontology from cache
         String ab = ns.getAbbreviation();
         int num = NameSpaces.getInstance().getNamespaces().get(ab).getNumberOfLoadedTriples();

         try{
            switch (ab) {
               case "chear":
                  assertTrue(String.format("Triples after loading \"chear\" ontologies should be %d, but was %d", CHEARTRIPLE, num), num == CHEARTRIPLE);
                  break;

               case "cogat":
                  assertTrue(String.format("Triples after loading \"cogat\" ontologies should be %d, but was %d", COGATTRIPLE, num), num == COGATTRIPLE);
                  break;

               case "doid":
                  assertTrue(String.format("Triples after loading \"doid\" ontologies should be %d, but was %d", DOIDTRIPLE, num), num == DOIDTRIPLE);
                  break;

               case "hasco":
                  assertTrue(String.format("Triples after loading \"hasco\" ontologies should be %d, but was %d", HASCOTRIPLE, num), num == HASCOTRIPLE);
                  break;

               case "owl":
                  assertTrue(String.format("Triples after loading \"owl\" ontologies should be %d, but was %d", OWLTRIPLE, num), num == OWLTRIPLE);
                  break;

               case "pato":
                  assertTrue(String.format("Triples after loading \"pato\" ontologies should be %d, but was %d", PATOTRIPLE, num), num == PATOTRIPLE);
                  break;

               case "prov":
                  assertTrue(String.format("Triples after loading \"prov\" ontologies should be %d, but was %d", PROVTRIPLE, num), num == PROVTRIPLE);
                  break;

               case "rdf":
                  assertTrue(String.format("Triples after loading \"rdf\" ontologies should be %d, but was %d", RDFTRIPLE, num), num == RDFTRIPLE);
                  break;

               case "rdfs":
                  assertTrue(String.format("Triples after loading \"rdfs\" ontologies should be %d, but was %d", RDFSTRIPLE, num), num == RDFSTRIPLE);
                  break;

               case "sio":
                  assertTrue(String.format("Triples after loading \"sio\" ontologies should be %d, but was %d", SIOTRIPLE, num), num == SIOTRIPLE);
                  break;

               case "uo":
                  assertTrue(String.format("Triples after loading \"uo\" ontologies should be %d, but was %d", UOTRIPLE, num), num == UOTRIPLE);
                  break;

               case "vstoi":
                  assertTrue(String.format("Triples after loading \"vstoi\" ontologies should be %d, but was %d", VSTOITRIPLE, num), num == VSTOITRIPLE);
                  break;
               default:
            }
            deleteNameSpace(ns);
         }
         catch(IOException e){
            System.err.println(e);
            fail("Couldn't clear ns = " + ns);
         }
      }
		System.out.println("[Step 3] Upload Ontology Test Pass.");
	}

	@Override
	public void preMsg() {
		System.out.println("[Step 3] Executing uploadOntologyTest:");

	}

	@Override
	public int step() {
		return 3;
	}

   private void cleanCache(){
      File folder = new File(NameSpaces.CACHE_PATH);
      folder.listFiles();
      String name = "";

      for (final File fileEntry : folder.listFiles()) {
          if (!fileEntry.isDirectory()) {
             name = fileEntry.getName();
             if (name.startsWith(NameSpaces.CACHE_PREFIX)) {
                  fileEntry.delete();
             }
          }
      }

      //check result of clean cache
    assertTrue("Clean Cache failed.", folder.listFiles().length == 0);
    System.out.println("[Step 3] Clean Cache Pass.");
   }

   private void deleteNameSpace(NameSpace ns) throws IOException {
      String url = "http://localhost:8080/blazegraph/namespace/store/sparql";
      System.out.println(ns.getURL());
		url += "?c=" + URLEncoder.encode("<" + ns.getURL() + ">", StandardCharsets.UTF_8.toString());

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("DELETE");
		con.setRequestProperty("Accept", "application/xml");


		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'Delete' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		//print result
		System.out.println(response.toString());
   }
}
