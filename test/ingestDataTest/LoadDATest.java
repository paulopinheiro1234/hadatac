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
