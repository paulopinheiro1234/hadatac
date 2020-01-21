package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class LoadSTRTest extends StepTest{
	private static LoadSTRTest test = new LoadSTRTest();
	
	private LoadSTRTest() {}
	
	public static LoadSTRTest getTest()
	{
		return test;
	}

	@Override
	public void test() {
		System.out.println("Step8: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing loadSTRTest:");
		
	}
	
	@Override
	public int step() {
		return 8;
	}

}
