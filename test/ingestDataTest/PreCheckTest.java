package ingestDataTest;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class PreCheckTest extends StepTest{
	private static final PreCheckTest test = new PreCheckTest();

	private PreCheckTest() {}
	
	public static PreCheckTest getTest() {
		return test;
	}
	
	
	@Override
	public void preMsg() {
		System.out.println("[Step 1] Executing PreCheckTest:");
		
	}
	
	
	@Override
	public void verifyPre() {
		
		
	}

	
	@Override
	public void test() {
		String host = "localhost";
		int solrPort = 8983;
		int blazePort = 8080;
		
		//detect solr on localhost:8983
		try {
			Socket solr = new Socket(host, solrPort);
			solr.close();
		} catch (UnknownHostException e) {
			fail("[Step 1] Connection to "+ host +" failed: Unknown host.");
		} catch (IOException e) {
			fail("[Step 1] Unable to detect solr at "+ host + ":" + solrPort);
		}
		System.out.println("[Step 1] Solr Check Pass.");
		
		//detect blazegraph on localhost:8080
		try {
			Socket blaze = new Socket(host, blazePort);
			blaze.close();
		} catch (UnknownHostException e) {
			fail("[Step 1] Connection to "+ host +" failed: Unknown host.");
		} catch (IOException e) {
			fail("[Step 1] Unable to detect blazegraph at "+ host + ":" + blazePort);
		}
		System.out.println("[Step 1] Blazegraph Check Pass.");
		
	}
	
	
	@Override
	public void verifyPost() {
		
		
	}

	@Override
	public int step() {
		return 1;
	}


}
