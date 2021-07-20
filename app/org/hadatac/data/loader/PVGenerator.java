package org.hadatac.data.loader;

import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.PossibleValue;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.utils.NameSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.String;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.Base64;
import java.math.BigInteger;  
import java.nio.charset.StandardCharsets; 
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  

public class PVGenerator extends BaseGenerator {

	private static final Logger log = LoggerFactory.getLogger(PVGenerator.class);

	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, Map<String, String>> pvMap = new HashMap<String, Map<String, String>>();
	Map<String, String> mapAttrObj;
	Map<String, String> codeMappings;
    protected AnnotationLogger logger = null;

	public PVGenerator(DataFile dataFile, String SDDName,  
			Map<String, String> mapAttrObj, Map<String, String> codeMappings) {
		super(dataFile);
		this.SDDName = SDDName;
		this.mapAttrObj = mapAttrObj;
		this.codeMappings = codeMappings;
		this.logger = dataFile.getLogger();
	}
	
	//Column	Code	Label	Class	Resource
	@Override
	public void initMapping() {
		mapCol.clear();
		mapCol.put("Label", "Column");
		mapCol.put("Code", "Code");
		mapCol.put("CodeLabel", "Label");
		mapCol.put("Class", "Class");
		mapCol.put("Resource", "Resource");
		mapCol.put("OtherFor", "Other For");
	}

    private String getLabel(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Label"));
	}

	private String getCode(Record rec) {
		String ss = Normalizer.normalize(rec.getValueByColumnName(mapCol.get("Code")), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").trim();
		int iend = ss.indexOf(".");
		if (iend != -1){
			ss = ss.substring(0 , iend);
		}
		return ss.trim();
	}

	private String getCodeLabel(Record rec) {
		return rec.getValueByColumnName(mapCol.get("CodeLabel"));
	}

	private String getClass(Record rec) {
		String cls = rec.getValueByColumnName(mapCol.get("Class"));
		if (cls.length() > 0) {
			if (URIUtils.isValidURI(cls)) {
				return cls;
			}
		} else {
			if (codeMappings.containsKey(getCode(rec))) {
				return codeMappings.get(getCode(rec));
			}
		}

		return "";
	}

	private String getResource(Record rec) {
		return rec.getValueByColumnName(mapCol.get("Resource"));
	}

	private Boolean checkVirtual(Record rec) {
		if (getLabel(rec).contains("??")){
			return true;
		} else {
			return false;
		}
	}

	private String getOtherFor(Record rec) {
		return rec.getValueByColumnName(mapCol.get("OtherFor"));
	}

	private String getPVvalue(Record rec) {
		if ((getLabel(rec)).length() > 0) {
			String colNameInSDD = getLabel(rec).replace(" ", "");
			if (mapAttrObj.containsKey(colNameInSDD) && mapAttrObj.get(colNameInSDD).length() > 0) {
				return kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			} else {
				return kbPrefix + "DASO-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", "");
			}
		} else {
			return "";
		}
	}
	
	public List<String> createUris() throws Exception {
		int rowNumber = 0;
		List<String> result = new ArrayList<String>();
		for (Record record : records) {
			result.add((kbPrefix + "PV-" + getLabel(record).replace("_","-").replace("??", "") + ("-" + SDDName + "-" + getCode(record)).replaceAll("--", "-")).replace(" ","") + "-" + rowNumber);
			++rowNumber;
		}
		return result;
	}

	@Override
	public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {	
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", (kbPrefix + "PV-" + getLabel(rec).replaceAll("[^a-zA-Z0-9:-]", "-") + ("-" + SDDName + "-" + getCode(rec)).replaceAll("--", "-")).replace(" ","").replaceAll("[^A-Za-z0-9:-]", "") + "-" + rowNumber);
		row.put("a", "hasco:PossibleValue");
		row.put("hasco:hasVariable", getLabel(rec).replaceAll("[^a-zA-Z0-9:-]", "-"));
		row.put("hasco:hasCode", getCode(rec));
		row.put("hasco:hasCodeLabel", getCodeLabel(rec));
		row.put("hasco:hasClass", getClass(rec));
		row.put("hasco:isPossibleValueOf", getPVvalue(rec));
		row.put("hasco:otherFor", getOtherFor(rec));
		
		return row;
	}

	@Override
	public String getTableName() {
		return "PossibleValue";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in PVGenerator: " + e.getMessage();
	}
 	 
    public static String hashWith256(String textToHash) {
    	String encoded = null;
    	try {
    		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
    		byte hashBytes[] = messageDigest.digest(textToHash.getBytes(StandardCharsets.UTF_8));
    		BigInteger noHash = new BigInteger(1, hashBytes);
    		encoded = noHash.toString(16);
    	} catch (Exception e) {
    	}
        return encoded;
    }
    
    private static String generateDCTerms(String variable, String code) {  
    	return variable + "||||" + code;
    }
    
    private static void generateOtherOther(String superUri, PossibleValue pv, String graphName) {
    	if (pv.getHasClass() == null || pv.getHasClass().isEmpty()) {
    		return;
    	}
    	Attribute attr = Attribute.find(pv.getHasClass());
    	if (attr != null) {
    		attr.setHasDCTerms(generateDCTerms(pv.getHasVariable(), pv.getHasCode()));
    		attr.setSuperUri(superUri);
			attr.setNamedGraph(graphName);
    		attr.updateAttribute();
    	}
    }

    private static void generateOther(String uri, String harmonizedCode, PossibleValue pv, String graphName) {
    	if (pv.getHasOtherFor() == null || pv.getHasOtherFor().isEmpty()) {
    		return;
    	}
    	Attribute attr = new Attribute();
    	attr.setUri(uri);
    	attr.setLabel(pv.getHasCodeLabel());
    	attr.setSuperUri(pv.getHasOtherFor());
    	attr.setHasDCTerms(generateDCTerms(pv.getHasVariable(), pv.getHasCode()));
    	attr.setHasSkosNotation(harmonizedCode);
    	attr.setNamedGraph(graphName);
    	attr.saveAttribute();
    	
    }
    
    public static void generateOthers(DataFile dataFile, String sddUri, String kbPrefix) {
		AnnotationLogger logger = dataFile.getLogger();
		logger.println("PVPostGenerator: Processing additional knowledge for <" + sddUri + ">");
		List<PossibleValue> codes = PossibleValue.findBySchema(sddUri);
		List<String> subs = new ArrayList<String>();
		logger.println("PVPostGenerator: Retrieved codes [" + codes.size() + "]");
		for (PossibleValue code : codes) {
			if (code.getHasOtherFor() != null && !code.getHasOtherFor().isEmpty()) {
				String superDCTerm = generateDCTerms(code.getHasVariable(), code.getHasCode());
				subs.clear();
				logger.println("SuperClass: [" + code.getHasOtherFor() + "]   Variable: [" + code.getHasDASAUri() + "]");
				List<PossibleValue> variableCodes = PossibleValue.findByVariable(code.getHasDASAUri());
				for (PossibleValue vc : variableCodes) {
					if (vc.getHasClass() != null && !vc.getHasClass().isEmpty() && (vc.getHasOtherFor() == null || vc.getHasOtherFor().isEmpty() )) {
						//System.out.println("      Variable: [" + code.getHasVariable() + "]    Class: [" + vc.getHasClass() + "]");
						if (!subs.contains(vc.getHasClass())) {
							subs.add(vc.getHasClass());
						}
						
						// update the class inside vc as a subclass of super
						generateOtherOther(code.getHasOtherFor(),vc,sddUri);
						logger.println("        - added " + vc.getHasClass() + " as a subclass of " + code.getHasOtherFor());
					}
				}
		        try { 
		        	Collections.sort(subs);
					String shaString = "Super=" + code.getHasOtherFor() + "|Sub=";
		        	for (String sub : subs) {
		        		shaString = shaString + sub;
		        	}
		        	String shaHash = hashWith256(shaString);
					String harmonizedCodeHex = shaHash.substring(0,5);
					String harmonizedCode = String.valueOf(Integer.parseInt(harmonizedCodeHex,16)); 
					String newUri = URIUtils.replacePrefixEx(kbPrefix + shaHash);
		            //System.out.println("      key:           [" + shaString + "]");  
		            //System.out.println("      harmonizedCode [" + harmonizedCode + "]");  
		            //System.out.println("      new uri        [" + newUri + "]");  
		            
		            // generate the 'other' class
		            generateOther(newUri, harmonizedCode, code, sddUri);
					logger.println("        - created 'other' class " + newUri + " as a subclass of " + code.getHasOtherFor());
		            
		            // associate the new 'other' class to the codebook element for the class
		            code.setHasClass(newUri);
		            code.setNamedGraph(sddUri);
		            code.saveHasClass();
		        } 
		        // For specifying wrong message digest algorithms  
		        catch (Exception e) {  
		            System.out.println("[ERROR] Generating sha-256: " + e);  
		        }  
			}
		}
		//System.out.println("PVPostGenerator: Additional knowledge derived from code book");
	}
	
}
