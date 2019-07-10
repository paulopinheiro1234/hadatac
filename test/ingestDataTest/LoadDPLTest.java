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
