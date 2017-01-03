import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.*;
import org.hadatac.console.controllers.metadata.*;
import org.hadatac.console.controllers.metadataacquisition.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.mvc.Http.Status.OK;

import java.util.ArrayList;
import java.util.HashMap;

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

public class ControllersTest {
	public static FakeApplication app;

	@BeforeClass
	public static void startApp() {
	    app = Helpers.fakeApplication(Helpers.inMemoryDatabase());
	    Helpers.start(app);
	}

	@AfterClass
	public static void stopApp() {
	    Helpers.stop(app);
	}
	
	@Test
	public void testDynamicStudyBrowserGeneration(){
		Map<String, List<String>> studyResult = DynamicGeneration.generateStudy();
		Map<String, List<String>> subjectResult = DynamicGeneration.findSubject();
		Map<String, Map<String, String>> indicatorResults = new HashMap<String, Map<String,String>>();
		for (Map.Entry<String, List<String>> study: studyResult.entrySet()){
			Map<String, String> indicatorResult = ViewStudy.findStudyIndicators(study.getKey());
			indicatorResults.put(study.getKey(), indicatorResult);
		}
	}
	
	@Test
	public void testDynamicMetadataBrowserGeneration(){
		Map<String,String> indicatorMap = DynamicMetadataGeneration.getIndicatorTypes();
		DynamicMetadataGeneration.renderSPARQLPage();
		DynamicMetadataGeneration.renderNavigationHTML(indicatorMap);
		DynamicMetadataGeneration.renderMetadataHTML(indicatorMap);
		DynamicMetadataGeneration.renderMetadataEntryHTML();
		DynamicMetadataGeneration.renderMetadataBrowserHTML(indicatorMap);
	}
	
	@Test
	public void testViewSample(){
		Map<String, String> indicatorValues = ViewSample.findSampleIndicators("chear-kb:fakeSample");
    	Map<String, List<String>> sampleResult = ViewSample.findBasic("chear-kb:fakeSample");
	}
	
	@Test
	public void testViewSubject(){
		Map<String, String> indicatorValues = ViewSubject.findSubjectIndicators("chear-kb:fakeSubject");
		Map<String, List<String>> subjectResult = ViewSubject.findBasic("chear-kb:fakeSubject");
		Map<String, List<String>> sampleResult = ViewSubject.findSampleMap("chear-kb:fakeSubject");
	}
	
	@Test
	public void testViewStudy(){
		Map<String, String> indicatorValues = ViewStudy.findStudyIndicators("chear-kb:fakeStudy");
		Map<String, List<String>> poResult = ViewStudy.findBasic("chear-kb:fakeStudy");
		Map<String, List<String>> subjectResult = ViewStudy.findSubject("chear-kb:fakeStudy");
	}

}