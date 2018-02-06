import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.*;
import org.hadatac.console.controllers.metadata.*;
import org.hadatac.console.controllers.metadataacquisition.*;
import org.hadatac.console.models.SysUser;

import static play.mvc.Http.Status.OK;

import java.util.ArrayList;
import java.util.HashMap;

import com.feth.play.module.pa.PlayAuthenticate;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.*;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.Helpers.*;
import play.test.WithApplication;
import play.twirl.api.Content;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import play.test.*;
import static play.test.Helpers.*;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.controllers.annotator.Annotator;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.loader.AnnotationWorker;
import org.hadatac.data.loader.StudyGenerator;
import org.hadatac.data.loader.util.Arguments;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.SDD;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.State;
import org.apache.commons.io.*;

public class ControllersTest {
	public static Application fakeApp = fakeApplication();
	public static Application fakeAppWithMemoryDb = fakeApplication(inMemoryDatabase("test"));
	public static String study_id = "default-study";

	@BeforeClass
	public static void startApp() {
	    Helpers.start(fakeApp);
	    Helpers.start(fakeAppWithMemoryDb);
	}

	@AfterClass
	public static void stopApp() {
	    Helpers.stop(fakeApp);
	    Helpers.stop(fakeAppWithMemoryDb);
	}

	@Test
	public void testRowsCreation() {
		List<Map<String, Object>> testl;
		
	    try{
			URL url = new URL("https://raw.githubusercontent.com/paulopinheiro1234/hadatac/master/test/controllers/STD-Test.csv");
			File file = new File("STD-Test.csv");
			FileUtils.copyURLToFile(url, file);
			StudyGenerator studyGenerator = new StudyGenerator(file);
			testl = studyGenerator.createRows();
			assertNotNull(testl);
	    } catch(Exception e){
	    	
	    }
	}
	
	@Test
	public void testStudyCreation() {
		List<Map<String, Object>> testl;
		try{
			URL url = new URL("https://raw.githubusercontent.com/paulopinheiro1234/hadatac/master/test/controllers/STD-Test.csv");
			File file = new File("STD-Test.csv");
			FileUtils.copyURLToFile(url, file);
			boolean bSucceed = false;
			bSucceed = AnnotationWorker.annotateStudyIdFile(file);
			assertTrue(bSucceed);
		}	catch(Exception e){
	    	
	    }
	}
	
	
//	@Test
//	public void testDynamicStudyBrowserGeneration(){
//		GeneratedStrings generatedStringsObject = new DynamicGeneration.GeneratedStrings();
//		Map<String, List<String>> studyResult = DynamicGeneration.generateStudy(generatedStringsObject);
//		Map<String, List<String>> subjectResult = DynamicGeneration.findSubject();
//		Map<String, Map<String, String>> indicatorResults = new HashMap<String, Map<String,String>>();
//		for (Map.Entry<String, List<String>> study: studyResult.entrySet()){
//			Map<String, String> indicatorResult = ViewStudy.findStudyIndicators(study.getKey());
//			indicatorResults.put(study.getKey(), indicatorResult);
//		}
//		assertNotNull(studyResult);
//		assertNotNull(subjectResult);
//		assertNotNull(indicatorResults);
//	}
//	
//	@Ignore @Test
//	public void testDynamicMetadataBrowserGeneration(){
//		Map<String,String> indicatorMap = DynamicMetadataGeneration.getIndicatorTypes();
//		DynamicMetadataGeneration.renderSPARQLPage();
//		DynamicMetadataGeneration.renderNavigationHTML(indicatorMap);
//		DynamicMetadataGeneration.renderMetadataHTML(indicatorMap);
//		DynamicMetadataGeneration.renderMetadataEntryHTML();
//		DynamicMetadataGeneration.renderMetadataBrowserHTML(indicatorMap);
//	}
//	
//	@Test
//	public void testViewSample(){
//		Map<String, String> indicatorValues = ViewSample.findSampleIndicators("chear-kb:fakeSample");
//    	Map<String, List<String>> sampleResult = ViewSample.findBasic("chear-kb:fakeSample");
//		assertNotNull(indicatorValues);
//		assertNotNull(sampleResult);
//	}
//	
//	@Test
//	public void testViewSubject(){
//		Map<String, String> indicatorValues = ViewSubject.findSubjectIndicators("chear-kb:fakeSubject");
//		Map<String, List<String>> subjectResult = ViewSubject.findBasic("chear-kb:fakeSubject");
//		Map<String, List<String>> sampleResult = ViewSubject.findSampleMap("chear-kb:fakeSubject");
//		assertNotNull(indicatorValues);
//		assertNotNull(subjectResult);
//		assertNotNull(sampleResult);
//	}
//	
//	@Test
//	public void testViewStudy(){
//		Map<String, String> indicatorValues = ViewStudy.findStudyIndicators("chear-kb:fakeStudy");
//		Map<String, List<String>> poResult = ViewStudy.findBasic("chear-kb:fakeStudy");
//		Map<String, List<String>> subjectResult = ViewStudy.findSubject("chear-kb:fakeStudy");
//		assertNotNull(indicatorValues);
//		assertNotNull(poResult);
//		assertNotNull(subjectResult);
//	}
//
}