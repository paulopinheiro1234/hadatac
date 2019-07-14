package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class DownloadTest extends StepTest{
	private static DownloadTest test = new DownloadTest();
	
	private DownloadTest() {}
	
	public static DownloadTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step11: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyFacetedSearch());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step11: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing downloadTest:");
		
	}
	
	@Override
	public int step() {
		return 11;
	}

}
