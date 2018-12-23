package org.hadatac.utils;

import org.apache.commons.configuration2.INIConfiguration;

public class Templates {
	
    public static String TEMPLATE_FILE = ConfigProp.getTemplateFileName();
    public static INIConfiguration iniConfig = new MyINIConfiguration(TEMPLATE_FILE);

    // STD Template (Study)                                                                     
    public static String STUDYID = iniConfig.getSection("STD").getString("studyID"); // also in ACQ, PID and SID
    public static String STUDYTITLE = iniConfig.getSection("STD").getString("studyTitle");
    public static String STUDYAIMS = iniConfig.getSection("STD").getString("studyAims");
    public static String STUDYSIGNIFICANCE = iniConfig.getSection("STD").getString("studySignificance");
    public static String NUMSUBJECTS = iniConfig.getSection("STD").getString("numSubjects");
    public static String NUMSAMPLES = iniConfig.getSection("STD").getString("numSamples");
    public static String INSTITUTION = iniConfig.getSection("STD").getString("institution");
    public static String PI = iniConfig.getSection("STD").getString("PI");
    public static String PIADDRESS = iniConfig.getSection("STD").getString("PIAddress");
    public static String PICITY = iniConfig.getSection("STD").getString("PICity");
    public static String PISTATE = iniConfig.getSection("STD").getString("PIState");
    public static String PIZIPCODE = iniConfig.getSection("STD").getString("PIZipCode");
    public static String PIEMAIL = iniConfig.getSection("STD").getString("PIEmail");
    public static String PIPHONE = iniConfig.getSection("STD").getString("PIPhone");
    public static String CPI1FNAME = iniConfig.getSection("STD").getString("CPI1FName");
    public static String CPI1LNAME = iniConfig.getSection("STD").getString("CPI1LName");
    public static String CPI1EMAIL = iniConfig.getSection("STD").getString("CPI1Email");
    public static String CPI2FNAME = iniConfig.getSection("STD").getString("CPI2FName");
    public static String CPI2LNAME = iniConfig.getSection("STD").getString("CPI2LName");
    public static String CPI2EMAIL = iniConfig.getSection("STD").getString("CPI2Email");
    public static String CONTACTFNAME = iniConfig.getSection("STD").getString("contactFName");
    public static String CONTACTLNAME = iniConfig.getSection("STD").getString("contactLName");
    public static String CONTACTEMAIL = iniConfig.getSection("STD").getString("contactEmail");
    public static String CREATEDDATE = iniConfig.getSection("STD").getString("createdDate");
    public static String UPDATEDDATE = iniConfig.getSection("STD").getString("updatedDate");
    public static String DCACCESSBOOL = iniConfig.getSection("STD").getString("DCAccessBool");
    public static String EXTSRC = iniConfig.getSection("STD").getString("externalSource");
    
    // ACQ Template
    public static String ACQ_DATAACQUISITIONNAME = iniConfig.getSection("ACQ").getString("DataAcquisitionName");
    public static String ACQ_METHOD = iniConfig.getSection("ACQ").getString("Method");
    public static String ACQ_DASTUDYID = iniConfig.getSection("ACQ").getString("DAStudyName");
    public static String ACQ_DATADICTIONARYNAME = iniConfig.getSection("ACQ").getString("DataDictionaryName");
    public static String ACQ_EPILAB = iniConfig.getSection("ACQ").getString("Epi/Lab");
    public static String ACQ_OWNEREMAIL = iniConfig.getSection("ACQ").getString("OwnerEmail");
    public static String ACQ_PERMISSIONURI = iniConfig.getSection("ACQ").getString("PermissionURI");
    
    // OAS Template
    public static String DATAACQUISITIONNAME = iniConfig.getSection("OAS").getString("DataAcquisitionName");
    public static String METHOD = iniConfig.getSection("OAS").getString("Method");
    public static String DATADICTIONARYNAME = iniConfig.getSection("OAS").getString("DataDictionaryName");
    public static String DASTUDYID = iniConfig.getSection("OAS").getString("DAStudyName");
    public static String EPILAB = iniConfig.getSection("OAS").getString("Epi/Lab");
    public static String OWNEREMAIL = iniConfig.getSection("OAS").getString("OwnerEmail");
    public static String PERMISSIONURI = iniConfig.getSection("OAS").getString("PermissionURI");
    public static String DEPLOYMENTURI = iniConfig.getSection("OAS").getString("DeploymentUri");
    public static String ROWSCOPE = iniConfig.getSection("OAS").getString("RowScope");
    public static String CELLSCOPE = iniConfig.getSection("OAS").getString("CellScope");

    // DASA, DASE, DASO Template (Part of SDD)
    public static String LABEL = iniConfig.getSection("DASA").getString("Label");     // also in PV                                                                                   
    public static String ATTRIBUTETYPE = iniConfig.getSection("DASA").getString("AttributeType");
    public static String ATTTRIBUTEOF = iniConfig.getSection("DASA").getString("AttributeOf");
    public static String UNIT = iniConfig.getSection("DASA").getString("Unit");
    public static String TIME = iniConfig.getSection("DASA").getString("Time");
    public static String ENTITY = iniConfig.getSection("DASA").getString("Entity");
    public static String ROLE = iniConfig.getSection("DASA").getString("Role");
    public static String RELATION = iniConfig.getSection("DASA").getString("Relation");
    public static String INRELATIONTO = iniConfig.getSection("DASA").getString("InRelationTo");
    public static String WASDERIVEDFROM = iniConfig.getSection("DASA").getString("WasDerivedFrom");
    public static String WASGENERATEDBY = iniConfig.getSection("DASA").getString("WasGeneratedBy");
    
    // PV Template (Part of SDD)                                                                                     
    public static String CODE = iniConfig.getSection("PV").getString("Code");
    public static String CODEVALUE = iniConfig.getSection("PV").getString("CodeValue");
    public static String CLASS = iniConfig.getSection("PV").getString("Class");
    
    // SID Template                                                                                                    
    public static String SAMPLEID = iniConfig.getSection("SID").getString("sampleID");                      
    public static String SAMPLESTUDYID = iniConfig.getSection("SID").getString("sampleStudyID");
    public static String SAMPLESUFFIX = iniConfig.getSection("SID").getString("sampleSuffix");
    public static String SUBJECTID = iniConfig.getSection("SID").getString("subjectID");  // also in PID                                                                             
    public static String SAMPLETYPE = iniConfig.getSection("SID").getString("sampleType");
    public static String SAMPLINGMETHOD = iniConfig.getSection("SID").getString("samplingMethod");
    public static String SAMPLINGVOL = iniConfig.getSection("SID").getString("samplingVol");
    public static String SAMPLINGVOLUNIT = iniConfig.getSection("SID").getString("samplingVolUnit");
    public static String STORAGETEMP = iniConfig.getSection("SID").getString("storageTemp");
    public static String FTCOUNT = iniConfig.getSection("SID").getString("FTcount");
    
    // MAP Template                                                                                                  
    public static String ORIGINALPID = iniConfig.getSection("MAP").getString("originalPID");
    public static String ORIGINALSID = iniConfig.getSection("MAP").getString("originalSID");
    public static String OBJECTTYPE = iniConfig.getSection("MAP").getString("objecttype");
    public static String MAPSTUDYID = iniConfig.getSection("MAP").getString("studyId");
    public static String TIMESCOPEID = iniConfig.getSection("MAP").getString("timeScope");
}
