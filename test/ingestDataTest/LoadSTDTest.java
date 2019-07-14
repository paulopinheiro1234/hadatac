package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadSTDTest extends StepTest{
	private static LoadSTDTest test = new LoadSTDTest();
	
	private LoadSTDTest() {}
	
	public static LoadSTDTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step5: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadDPL());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step5: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadSTDTest:");
		
	}

	@Override
	public int step() {
		return 5;
	}
}
