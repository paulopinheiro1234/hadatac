package ingestDataTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public abstract class StepTest {

	public final static String FILEPATH = "test/ingestDataTest/step.txt";
	public abstract int step();
	public abstract void test();
	public abstract void preMsg();
	
	
	public void checkStep()
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(FILEPATH));
			int s = Integer.parseInt(reader.readLine());
			assertTrue("Skip Step: Should be: " + String.valueOf(step()-1) + " But was: " + s, s == step()-1);
			reader.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void recordStep()
	{
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(FILEPATH));
			writer.write(String.valueOf(step()));
			writer.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void execute()
	{
		preMsg();
		checkStep();
		test();
		recordStep();
	}
}
