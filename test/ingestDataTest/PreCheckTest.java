package ingestDataTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hadatac.entity.pojo.OperationMode;
import com.typesafe.config.ConfigFactory;

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
	public void test() {
      String triplestore = ConfigFactory.load().getString("hadatac.solr.triplestore");
      String host = triplestore.split("//")[1].split(":")[0];
      // String host = "localhost";
      // int blazePort = 8080;
      int blazePort = Integer.parseInt(triplestore.split("//")[1].split(":")[1].split("/")[0]);

		String email = "userTest@test";
		int solrPort = 8983;
      System.out.println("Unable to detect blazegraph at " + host + ":" + blazePort);

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

		// //enter sandbox mode
      //   //List<OperationMode> modes = OperationMode.findAll();
      //   OperationMode m = OperationMode.findByEmail(email);
      //   if(m != null) {
      //   	System.out.println("[Step 1] Test Sandbox Mode has been found.");
      //   	assertTrue("Test Sandbox delete unsuccessfully", m.delete() != -1);
      //   }
      //
      //   OperationMode mode = new OperationMode();
      //   mode.setUserEmail(email);
      //   mode.setOperationMode(OperationMode.SANDBOX);
      //   mode.setLastEnterTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
      //   mode.save();
      //
      //   System.out.println("[Step 1] Test Sandbox Mode has been created.");
	}


	@Override
	public int step() {
		return 1;
	}


}
