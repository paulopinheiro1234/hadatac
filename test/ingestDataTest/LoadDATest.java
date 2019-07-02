package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadDATest extends StepTest{
	private static LoadDATest test = new LoadDATest();
	
	private LoadDATest() {}
	
	public static LoadDATest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step9: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadOAS());
		System.out.println("Passed.");
		
	}

	@Override
	public void verifyPost() {
		System.out.print("Step9: Post-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadDA());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step9: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadDATest:");
		
	}

	@Override
	public int step() {
		return 9;
	}
}
