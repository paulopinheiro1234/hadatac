package ingestDataTest;

import static org.junit.Assert.assertTrue;
import org.hadatac.console.controllers.triplestore.LoadOnt;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import com.typesafe.config.ConfigFactory;

public class UploadOntologyTest extends StepTest{
	private static UploadOntologyTest test = new UploadOntologyTest();
	
	private UploadOntologyTest() {}
	
	public static UploadOntologyTest getTest()
	{
		return test;
	}
	
	@Override
	public void verifyPre() {
		System.out.print("Step3: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyCleanData());
		System.out.println("Passed.");
		
	}

	@Override
	public void verifyPost() {
		System.out.print("Step3: Post-step Verification......");
		assertTrue("Failed.", Verify.verifyUploadOntology());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		NameSpaces.getInstance();
        MetadataContext metadata = new 
                MetadataContext("user", 
                        "password", 
                        ConfigFactory.load().getString("hadatac.solr.triplestore"), 
                        false);
        metadata.loadOntologies(0, "confirmedCache");
        assertTrue(metadata.totalTriples() == 5339);
		System.out.println("[Step 3] Upload Ontology Test Pass.");
	}

	@Override
	public void preMsg() {
		System.out.println("[Step3] Executing uploadOntologyTest:");
		
	}

	@Override
	public int step() {
		return 3;
	}

}
