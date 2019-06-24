package ingestDataTest;

public class EmptyTest extends StepTest{
	private static EmptyTest test = new EmptyTest();
	
	private EmptyTest() {}
	
	public static EmptyTest getTest()
	{
		return test;
	}

	@Override
	public int step() {
		return 0;
	}

	@Override
	public void verifyPre() {
		return;
		
	}

	@Override
	public void verifyPost() {
		return;
	}

	@Override
	public void test() {
		return;
		
	}

	@Override
	public void preMsg() {
		return;
	}
	
	@Override
	public void checkStep() {
		return;
	}

}
