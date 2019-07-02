package ingestDataTest;

import static org.junit.Assert.assertTrue;

public class FacetedSearchTest extends StepTest{
	private static FacetedSearchTest test = new FacetedSearchTest();
	
	private FacetedSearchTest() {}
	
	public static FacetedSearchTest getTest()
	{
		return test;
	}

	@Override
	public void verifyPre() {
		System.out.print("Step10: Pre-step Verification......");
		assertTrue("Failed.", Verify.verifyLoadDA());
		System.out.println("Passed.");
		
	}

	@Override
	public void verifyPost() {
		System.out.print("Step10: Post-step Verification......");
		assertTrue("Failed.", Verify.verifyFacetedSearch());
		System.out.println("Passed.");
		
	}

	@Override
	public void test() {
		System.out.println("Step10: Not yet implemented");
		
	}

	@Override
	public void preMsg() {
		System.out.println("[test] Executing facetedSearchTest:");
		
	}
	
	@Override
	public int step() {
		return 10;
	}

}
