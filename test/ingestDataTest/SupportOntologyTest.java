package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class SupportOntologyTest extends StepTest{
	private static final SupportOntologyTest test = new SupportOntologyTest();

	private SupportOntologyTest() {}
	
	public static SupportOntologyTest getTest() {
		return test;
	}
	
	
	@Override
	public void preMsg() {
		System.out.println("[test] Executing supportOntologyTest:");
		
	}
	
	
	@Override
	public void verifyPre() {
		System.out.print("Step1: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifySetUp());
		System.out.println("Passed.");
		
	}

	
	@Override
	public void test() {
		System.out.println("Step1: Not yet implemented");
		
	}
	
	
	@Override
	public void verifyPost() {
		System.out.print("Step1: Post-step Verification......");
		assertTrue("Failed.", Verify.verifySupportOntology());
		System.out.println("Passed.");
		
	}

	@Override
	public int step() {
		return 1;
	}


}
