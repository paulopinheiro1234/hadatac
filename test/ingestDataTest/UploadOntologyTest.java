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
import org.hadatac.utils.NameSpaces;
import com.typesafe.config.ConfigFactory;

public class UploadOntologyTest extends StepTest{
	private static UploadOntologyTest test = new UploadOntologyTest();
	private int CHEARTRIPLE = 5339;
	
	private UploadOntologyTest() {}
	
	public static UploadOntologyTest getTest()
	{
		return test;
	}
	
	@Override
	public void verifyPre() {

		return;
	}

	@Override
	public void verifyPost() {

		return;
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
		
		//move copy-chear into cache file
		try {
			Files.copy(java.nio.file.Paths.get("test/src/copy-chear"), java.nio.file.Paths.get(NameSpaces.CACHE_PATH+"copy-chear"), StandardCopyOption.REPLACE_EXISTING);
		}catch(IOException e) {
			fail("Fail to copy \"copy-chear\" from src to cache file");
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
        long tripleNum = metadata.totalTriples();
        assertTrue(String.format("Triples after loading \"chear\" Ontology should be %d, but was %d", CHEARTRIPLE, tripleNum), tripleNum == CHEARTRIPLE);
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

}
