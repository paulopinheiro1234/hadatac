package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.ObjectCollection;


public class SubjectGenerator extends BasicGenerator {

	static final long MAX_OBJECTS = 1000;
	static final long LENGTH_CODE = 6;

	final String kbPrefix = ConfigProp.getKbPrefix();

	StudyObject obj = null;
	private int counter = 1; //starting index number
	private String hasScopeUri = "";    
	private List<String> scopeUris = new ArrayList<String>();
	private List<String> spaceScopeUris = new ArrayList<String>();
	private List<String> timeScopeUris = new ArrayList<String>();
	private List<String> objectUris = new ArrayList<String>();

	public SubjectGenerator(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("subjectID", "patient_id");
		mapCol.put("pilotNum", "project_id");
	}

	private String getUri(CSVRecord rec) {
		return kbPrefix + "SBJ-" + getOriginalID(rec) + "-" + getPilotNum(rec);
	}

	private String getType() {
		return "http://semanticscience.org/resource/Human";
	}

	private String getLabel(CSVRecord rec) {
		return "Subject ID " + getOriginalID(rec) + " - " + getPilotNum(rec);
	}

	private String getOriginalID(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("subjectID"));
	}

	private String getPilotNum(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("pilotNum"));
	}

	private String getStudyUri(CSVRecord rec) {
		return kbPrefix + "STD-" + getPilotNum(rec);
	}

	private String getCohortUri(CSVRecord rec) {
		return kbPrefix + "CH-" + getPilotNum(rec);
	}

	private String getCohortLabel(CSVRecord rec) {
		return "Study Population of " + getPilotNum(rec);
	}

	public void createObj(CSVRecord record) throws Exception {
		// insert current state of the OBJ
		obj = new StudyObject(getUri(record), "sio:Human", 
				getOriginalID(record), getLabel(record), 
				getCohortUri(record), getLabel(record), scopeUris);

		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		obj.save();
		System.out.println("obj " + getUri(record).toString() + " saved!");

		// update/create new OBJ in LabKey
		Credential cred = Credential.find();
		if (null == cred) {
			System.out.println(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			return;
		}
		int nRowsAffected = obj.saveToLabKey(cred.getUserName(), cred.getPassword());
		if (nRowsAffected <= 0) {
			System.out.println("[ERROR] Failed to insert new OBJ to LabKey!");
		}

		objectUris.add(getUri(record));
		System.out.println(objectUris.size());
	}

	public boolean createOc(CSVRecord record) throws Exception {
		// insert current state of the OC
		ObjectCollection oc = new ObjectCollection(
				getCohortUri(record),
				"http://hadatac.org/ont/hasco/SubjectGroup",
				getCohortLabel(record),
				getCohortLabel(record),
				getStudyUri(record),
				hasScopeUri,
				spaceScopeUris,
				timeScopeUris);
		oc.setObjectUris(objectUris);
		oc.save();
		System.out.println("oc saved!");
		
		// update/create new OC in LabKey
		Credential cred = Credential.find();
		if (null == cred) {
			System.out.println(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			return false;
		}

		int nRowsAffected = oc.saveToLabKey(cred.getUserName(), cred.getPassword());
		if (nRowsAffected <= 0) {
			System.out.println("Failed to insert new OC to LabKey!\n");
			return false;
		}
		
		return true;
	}

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", getType());
		row.put("rdfs:label", getLabel(rec));
		row.put("hasco:originalID", getOriginalID(rec));
		row.put("hasco:isSubjectOf", getCohortUri(rec));
		counter++;

		return row;
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		boolean firstRow = true;
		for (CSVRecord record : records) {
			if (firstRow) {
				if (!createOc(record)) {
					break;
				}
				firstRow = false;
			}
			createObj(record);
		}
		
		return rows;
	}
}
