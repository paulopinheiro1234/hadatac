package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadDPLTest extends StepTest{
	private static LoadDPLTest test = new LoadDPLTest();
	
	private LoadDPLTest() {}
	
	public static LoadDPLTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step4: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyUploadOntology());
		System.out.println("Passed.");
		
	}

	@Override
	public void verifyPost() {
		System.out.print("Step4: Post-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadDPL());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step4: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadDPLTest:");
		
	}

	@Override
	public int step() {
		return 4;
	}
}
