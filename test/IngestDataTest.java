
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;

import org.junit.Test;

import ingestDataTest.CleanDataTest;
import ingestDataTest.DownloadTest;
import ingestDataTest.EmptyTest;
import ingestDataTest.FacetedSearchTest;
import ingestDataTest.LoadDATest;
import ingestDataTest.LoadDPLTest;
import ingestDataTest.LoadOASTest;
import ingestDataTest.LoadSDDTest;
import ingestDataTest.LoadSSDTest;
import ingestDataTest.LoadSTDTest;
import ingestDataTest.PreCheckTest;
import ingestDataTest.StepTest;
import ingestDataTest.UploadOntologyTest;

public class IngestDataTest {
	private final int TESTNUM = 11;
	private ArrayList<StepTest> tests= new ArrayList<StepTest>(TESTNUM);
	public void loadTest()
	{
		tests.add(0, EmptyTest.getTest());
		tests.add(1, PreCheckTest.getTest());
		tests.add(2, CleanDataTest.getTest());
		tests.add(3, UploadOntologyTest.getTest());
		tests.add(4, LoadDPLTest.getTest());
		tests.add(5, LoadSTDTest.getTest());
		tests.add(6, LoadSSDTest.getTest());
		tests.add(7, LoadSDDTest.getTest());
		tests.add(8, LoadOASTest.getTest());
		tests.add(9, LoadDATest.getTest());
		tests.add(10, FacetedSearchTest.getTest());
		tests.add(11, DownloadTest.getTest());
	}
	
	@Test
	public void test() throws IOException
	{
		loadTest();
		int step;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(StepTest.FILEPATH));
			step = Integer.parseInt(reader.readLine());
			reader.close();
		} catch (Exception e) {
			fail(e.getMessage());
		}

		//UI not implemented yet
		//due to unavailability of System.in stream under sbt mode
		
		/*System.out.println("You have currently arrived Test Step: " + step);
		System.out.println("0: Reinitialize the step.");
		System.out.println("1: SupportOntologyTest.");
		System.out.println("2: CleanDataTest.");
		System.out.println("3: UploadOntologyTest.");
		System.out.println("4: LoadDPLTest.");
		System.out.println("5: LoadSTDTest.");
		System.out.println("6: LoadSSDTest.");
		System.out.println("7: LoadSDDTest.");
		System.out.println("8: LoadOASTest.");
		System.out.println("9: LoadDATest.");
		System.out.println("10: FacetedSearchTest.");
		System.out.println("11: DownloadTest.");
		System.out.println("all: Go through all steps.");
		System.out.println("q: exit.");*/
		Scanner scan = new Scanner(System.in);
		
		while(true)
		{
			System.out.print("Choose a test option:");
			System.out.println("\nUI implementation not completed. Choose \"all\" automatically.");
			/*String cmd = scan.nextLine();*/
			String cmd = "all";
			if(cmd.equals("q"))
			{
				scan.close();
				return;
			}
			if(cmd.equals("all"))
			{
				testAll();
				scan.close();
				return;
			}
			
			int index = 0;
			try
			{
				index = Integer.parseInt(cmd);
			}
			catch(Exception e)
			{
				continue;
			}
			
			if(index < 0 || index > TESTNUM)
			{
				continue;
			}
			
			tests.get(index).execute();
			scan.close();
			break;
		}
	}
	
	public void testAll()
	{
		for(StepTest test : tests)
		{
			test.execute();
		}
		//tests.get(4).execute();
		System.out.println("\nCongratulation: IngestDataTest Pass.");
	}
}