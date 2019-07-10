package ingestDataTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.List;

import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.NameSpace;

public class LoadDPLTest extends StepTest{
	private static LoadDPLTest test = new LoadDPLTest();
	
	private LoadDPLTest() {}
	
	public static LoadDPLTest getTest()
	{
		return test;
	}

	@Override
	public void test() {
		//delete all existing processed csv
		List<DataFile> processed = DataFile.findByStatus(DataFile.PROCESSED);
		for (DataFile dataFile : processed)
		{
			//equivalent to part of AutoAnnotator.deleteDataFile("/", id)
			File file = new File(dataFile.getAbsolutePath());

	        if (dataFile.getPureFileName().startsWith("DA-")) {
	            Measurement.deleteFromSolr(dataFile.getDatasetUri());
	            NameSpace.deleteTriplesByNamedGraph(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
	        } else {
	            try {
	                AutoAnnotator.deleteAddedTriples(file, dataFile);
	            } catch (Exception e) {
	                System.out.print("Can not delete triples ingested by " + dataFile.getFileName() + " ..");
	                file.delete();
	                dataFile.delete();
	                
	                //return redirect(routes.AutoAnnotator.index(dir, "."));
	            }
	        }
	        file.delete();
	        dataFile.delete();
		}
		
		//TODO: delete all files in unprocessed_csv
		
		
		//put DPL into unprocessed_csv
		try {
			Files.copy(java.nio.file.Paths.get("test/src/DPL-CHEAR-v2.xlsx"), java.nio.file.Paths.get("unprocessed_csv/DPL-CHEAR-v2.xlsx"), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			fail("Fail to copy DPL from test/src to unprocessed_csv");
		}
		
		//process the DPL
		AnnotationWorker.scan();
		AnnotationWorker.autoAnnotate();
		
		//check absence of DPL in unprocessed_csv
		File dplUnprocessed = new File("unprocessed_csv/DPL-CHEAR-v2.xlsx");
		assertTrue("unprocessed_csv/DPL-CHEAR-v2.xlsx not deleted after loading DPL", !dplUnprocessed.exists());
		
		//check existence of DPL in processed_csv
		File dplProcessed = new File("processed_csv/DPL-CHEAR-v2.xlsx");
		assertTrue("Fail to detect processed_csv/DPL-CHEAR-v2.xlsx after loading DPL", dplProcessed.exists());
		
	}

	@Override
	public void preMsg() {
		System.out.println("[Step 4] Executing loadDPLTest:");
		
	}

	@Override
	public int step() {
		return 4;
	}
}
