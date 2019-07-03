package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadSSDTest extends StepTest{
	private static LoadSSDTest test = new LoadSSDTest();
	
	private LoadSSDTest() {}
	
	public static LoadSSDTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step6: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadSTD());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step6: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadSSDTest:");
		
	}

	@Override
	public int step() {
		return 6;
	}
	
}
