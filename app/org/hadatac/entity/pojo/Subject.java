package org.hadatac.entity.pojo;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class Subject extends StudyObject {

	public static boolean isPlatform(String subject_uri) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?cohort WHERE {\n"
				+ "  <" + subject_uri + "> a hasco:SubjectPlatform . \n"
				+ "  <" + subject_uri + "> hasco:isSubjectOf ?cohort . \n"
				+ "}";

		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		if (resultsrw.size() >= 1) {
			return true;
		}

		return false;
	}

	public static Subject findSubject(String study_uri, String subject_id) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?uri WHERE {\n"
				+ "  ?uri hasco:originalID \"" + subject_id + "\" .\n"
				+ "  ?uri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";

		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		if (resultsrw.size() >= 1) {
			Subject subject = new Subject();
			QuerySolution soln = resultsrw.next();
			subject.setUri(soln.getResource("uri").getURI());
			return subject;
		}

		return null;
	}

	public static String findSampleUri(String study_uri, String sample_id) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT ?sampleUri WHERE {\n"
				+ "  ?sampleUri hasco:originalID \"" + sample_id + "\" .\n"
				+ "  ?sampleUri hasco:isSampleOf ?subjectUri .\n"
				+ "  ?subjectUri hasco:isSubjectOf ?cohort .\n"
				+ "  ?cohort hasco:isCohortOf " + study_uri + " .\n"
				+ "}";

		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

		if (resultsrw.size() >= 1) {
			QuerySolution soln = resultsrw.next();
			return soln.getResource("sampleUri").getURI();
		}

		return null;
	}
}


