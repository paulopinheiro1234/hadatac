package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadOASTest extends StepTest{
	private static LoadOASTest test = new LoadOASTest();
	
	private LoadOASTest() {}
	
	public static LoadOASTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step8: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadSDD());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step8: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadOASTest:");
		
	}
	
	@Override
	public int step() {
		return 8;
	}

}
