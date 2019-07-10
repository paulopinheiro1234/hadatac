package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadSDDTest extends StepTest{
	private static LoadSDDTest test = new LoadSDDTest();
	
	private LoadSDDTest() {}
	
	public static LoadSDDTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step7: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadSSD());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step7: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadSDDTest:");
		
	}

	@Override
	public int step() {
		return 7;
	}
	
}
