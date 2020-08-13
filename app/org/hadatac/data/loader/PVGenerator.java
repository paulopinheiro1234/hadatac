package org.hadatac.data.loader;

import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.PossibleValue;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;

import java.lang.String;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigInteger;  
import java.nio.charset.StandardCharsets; 
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  

public class PVGenerator extends BaseGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";
	Map<String, String> codeMap;
	Map<String, Map<String, String>> pvMap = new HashMap<String, Map<String, String>>();
	Map<String, String> mapAttrObj;
	Map<String, String> codeMappings;

	public PVGenerator(DataFile dataFile, String SDDName,  
			Map<String, String> mapAttrObj, Map<String, String> codeMappings) {
		super(dataFile);
		this.SDDName = SDDName;
		this.mapAttrObj = mapAttrObj;
		this.codeMappings = codeMappings;
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

	  
	public static byte[] getSHA(String input) throws NoSuchAlgorithmException {  
        // Static getInstance method is called with hashing SHA  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        // digest() method called  
        // to calculate message digest of an input  
        // and return array of byte 
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
	} 
	    
    public static String toHexString(byte[] hash) { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32) {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
	} 
	  	
    private String generateDCTerms(String variable, String code) {  
    	return variable + "||||" + code;
    }
    
    /*
    <http://purl.org/twc/HHEAR_00525> a owl:Class ;
        dcterms:identifier "Pvit_2to2||||1" ;
        rdfs:subClassOf <http://purl.obolibrary.org/obo/MAXO_0001018> .

    <http://purl.org/twc/ctxid/cb10292fc67baf58fe96bf5b3d8ae2249d4c978705991c46f1879018c62313729d4> a owl:Class ;
        rdfs:label "No Gestational Diabetes" ;
        dcterms:identifier "GDM||||0" ;
        rdfs:subClassOf sio:SIO_010056 ;
        skos:notation 376209662259445586110671426195360743491331431208160433392413756876228049585514964 .
    */    
        
    private void generateOtherOther(String superUri, PossibleValue pv) {
    	if (pv.getHasClass() == null || pv.getHasClass().isEmpty()) {
    		return;
    	}
    	Attribute attr = Attribute.find(pv.getHasClass());
    	if (attr != null) {
    		attr.setHasDCTerms(generateDCTerms(pv.getHasVariable(), pv.getHasCode()));
    		attr.setSuperUri(superUri);
    		attr.updateAttribute();
    	}
    }

    private void generateOther(String uri, String harmonizedCode, PossibleValue pv) {
    	if (pv.getHasClass() == null || pv.getHasClass().isEmpty()) {
    		return;
    	}
    	Attribute attr = new Attribute();
    	attr.setUri(uri);
    	attr.setLabel(pv.getHasCodeLabel());
    	attr.setSuperUri(pv.getHasOtherFor());
    	attr.setHasDCTerms(generateDCTerms(pv.getHasVariable(), pv.getHasCode()));
    	attr.setHasSkosNotation(harmonizedCode);
    	attr.saveAttribute();
    	
    }
    
	@Override
    public void postprocess() throws Exception {
		System.out.println("Processing additional knowledge");
		List<PossibleValue> codes = PossibleValue.findBySchema(URIUtils.replacePrefixEx(kbPrefix + "DAS-" + SDDName));
		for (PossibleValue code : codes) {
			if (code.getHasOtherFor() != null && !code.getHasOtherFor().isEmpty()) {
				String shaString = "Super="; 
				String superDCTerm = generateDCTerms(code.getHasVariable(), code.getHasCode());
				System.out.println("SuperClass: [" + code.getHasOtherFor() + "]   Variable: [" + code.getHasDASAUri() + "]");
				shaString = shaString + code.getHasOtherFor() + ":Sub=";
				List<PossibleValue> variableCodes = PossibleValue.findByVariable(code.getHasDASAUri());
				for (PossibleValue vc : variableCodes) {
					if (vc.getHasClass() != null && !vc.getHasClass().isEmpty()) {
						System.out.println("      Variable: [" + code.getHasVariable() + "]    Class: [" + vc.getHasClass() + "]");
						shaString = shaString + vc.getHasClass();
						
						// update the class inside vc as a subclass of super
						generateOtherOther(code.getHasOtherFor(),vc);
						System.out.println("        - added " + vc.getHasClass() + " as a subclass of " + code.getHasOtherFor());
					}
				}
		        try { 
		        	BigInteger intSha = new BigInteger(1, getSHA(shaString));
		        	String shaHash = intSha.toString();
					String harmonizedCode = shaHash.substring(0,7);
					String newUri = URIUtils.replacePrefixEx(kbPrefix + toHexString(getSHA(shaHash)));
		            System.out.println("      [" + shaString + "] : Code: [" + shaHash + "]");  
		            System.out.println("      [" + shaString + "] : harmonizedCode [" + harmonizedCode + "]");  
		            System.out.println("      [" + shaString + "] : new uri [" + newUri + "]");  
		            
		            // generate the 'other' class
		            generateOther(newUri, harmonizedCode, code);
					System.out.println("        - created 'other' class " + newUri + " as a subclass of " + code.getHasOtherFor());
		            
		            // associate the new 'other' class to the codebook element for the class
		            code.setHasClass(newUri);
		            code.saveHasClass();
		        } 
		        // For specifying wrong message digest algorithms  
		        catch (NoSuchAlgorithmException e) {  
		            System.out.println("Exception thrown for incorrect algorithm: " + e);  
		        }  
			}
		}
		System.out.println("Additional knowledge derived from code book");
	}
 
}
