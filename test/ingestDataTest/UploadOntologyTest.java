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
		//clean cache
		//equivalent to org.hadatac.console.controllers.triplestore.LoadOnt.erasecache()
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
		
		//move all ontologies into cache file
		try {
			Files.copy(java.nio.file.Paths.get("test/src/copy-chear"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-chear"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-cogat"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-cogat"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-doid"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-doid"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-hasco"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-hasco"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-owl"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-owl"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-pato"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-pato"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-prov"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-prov"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-rdf"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-rdf"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-rdfs"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-rdfs"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-sio"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-sio"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-uo"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-uo"), StandardCopyOption.REPLACE_EXISTING);
			Files.copy(java.nio.file.Paths.get("test/src/copy-vstoi"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-vstoi"), StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e) {
			fail("Fail to copy ontologies from test/src to tmp/cache");
		}
		
		
		//upload ontology from cache
		//equivalent to org.hadatac.console.controllers.triplestore.LoadOnt.playLoadOntologies("conformedCache")
		NameSpaces.getInstance();
        MetadataContext metadata = new 
                MetadataContext("user", 
                        "password", 
                        ConfigFactory.load().getString("hadatac.solr.triplestore"), 
                        false);
        metadata.loadOntologies(0, "confirmedCache");
        
        //check result after upload ontology from cache
        for(NameSpace ns : NameSpaces.getInstance().getOrderedNamespacesAsList())
        {
        	String ab = ns.getAbbreviation();
        	int num = ns.getNumberOfLoadedTriples();
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
				break;
			}
        }
        
        long tripleNum = metadata.totalTriples();
        assertTrue(String.format("Triples after loading all ontologies should be %d, but was %d", TOTALTRIPLE, tripleNum), tripleNum == TOTALTRIPLE);
		System.out.println("[Step 3] Upload Ontology Test Pass. " + tripleNum);
	}

	@Override
	public void preMsg() {
		System.out.println("[Step 3] Executing uploadOntologyTest:");
		
	}

	@Override
	public int step() {
		return 3;
	}

}
