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
