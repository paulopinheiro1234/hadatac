package org.hadatac.utils;

public class Templates {
	
    private static String TEMPLATE_FILE = ConfigProp.getTemplateFileName();

    // STD Template (Study and SampleCollection)                                                                     
    public static String STUDYID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "studyID"); // also in ACQ, PID and SID
    public static String STUDYTITLE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "studyTitle");
    public static String STUDYAIMS = ConfigProp.getPropertyValue(TEMPLATE_FILE, "studyAims");
    public static String STUDYSIGNIFICANCE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "studySignificance");
    public static String NUMSUBJECTS = ConfigProp.getPropertyValue(TEMPLATE_FILE, "numSubjects");
    public static String NUMSAMPLES = ConfigProp.getPropertyValue(TEMPLATE_FILE, "numSamples");
    public static String INSTITUTION = ConfigProp.getPropertyValue(TEMPLATE_FILE, "institution");
    public static String PI = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PI");
    public static String PIADDRESS = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PIAddress");
    public static String PICITY = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PICity");
    public static String PISTATE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PIState");
    public static String PIZIPCODE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PIZipCode");
    public static String PIEMAIL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PIEmail");
    public static String PIPHONE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "PIPhone");
    public static String CPI1FNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI1FName");
    public static String CPI1LNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI1LName");
    public static String CPI1EMAIL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI1Email");
    public static String CPI2FNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI2FName");
    public static String CPI2LNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI2LName");
    public static String CPI2EMAIL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CPI2Email");
    public static String CONTACTFNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "contactFName");
    public static String CONTACTLNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "contactLName");
    public static String CONTACTEMAIL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "contactEmail");
    public static String CREATEDDATE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "createdDate");
    public static String UPDATEDDATE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "updatedDate");
    public static String DCACCESSBOOL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "DCAccessBool");
    public static String EXTSRC = ConfigProp.getPropertyValue(TEMPLATE_FILE, "externalSource");
	    
    // ACQ (DA and DEPLOYMENT) Template                                                                              
    public static String DATAACQUISITIONNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "DataAcquisitionName");
    public static String METHOD = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Method");
    public static String DATADICTIONARYNAME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "DataDictionaryName");
    public static String DASTUDYID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "DAStudyName");
    public static String EPILAB = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Epi/Lab");
    public static String OWNEREMAIL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "OwnerEmail");

    // DASA, DASE, DASO Template (Part of SDD)                                                                       
    public static String LABEL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Label");     // also in PV                                                                                   
    public static String ATTRIBUTETYPE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "AttributeType");
    public static String ATTTRIBUTEOF = ConfigProp.getPropertyValue(TEMPLATE_FILE, "AttributeOf");
    public static String UNIT = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Unit");
    public static String TIME = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Time");
    public static String ENTITY = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Entity");
    public static String ROLE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Role");
    public static String RELATION = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Relation");
    public static String INRELATIONTO = ConfigProp.getPropertyValue(TEMPLATE_FILE, "InRelationTo");
    public static String WASDERIVEDFROM = ConfigProp.getPropertyValue(TEMPLATE_FILE, "WasDerivedFrom");
    public static String WASGENERATEDBY = ConfigProp.getPropertyValue(TEMPLATE_FILE, "WasGeneratedBy");
    public static String HASPOSITION = ConfigProp.getPropertyValue(TEMPLATE_FILE, "HasPosition");
    
    // PV Template (Part of SDD)                                                                                     
    public static String CODE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Code");
    public static String CODEVALUE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "CodeValue");
    public static String CLASS = ConfigProp.getPropertyValue(TEMPLATE_FILE, "Class");
    
    // SID Template                                                                                                    
    public static String SAMPLEID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "sampleID");                      
    public static String SAMPLESTUDYID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "sampleStudyID");
    public static String SAMPLESUFFIX = ConfigProp.getPropertyValue(TEMPLATE_FILE, "sampleSuffix");
    public static String SUBJECTID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "subjectID");  // also in PID                                                                             
    public static String SAMPLETYPE = ConfigProp.getPropertyValue(TEMPLATE_FILE, "sampleType");
    public static String SAMPLINGMETHOD = ConfigProp.getPropertyValue(TEMPLATE_FILE, "samplingMethod");
    public static String SAMPLINGVOL = ConfigProp.getPropertyValue(TEMPLATE_FILE, "samplingVol");
    public static String SAMPLINGVOLUNIT = ConfigProp.getPropertyValue(TEMPLATE_FILE, "samplingVolUnit");
    public static String STORAGETEMP = ConfigProp.getPropertyValue(TEMPLATE_FILE, "storageTemp");
    public static String FTCOUNT = ConfigProp.getPropertyValue(TEMPLATE_FILE, "FTcount");
    
    // MAP Template                                                                                                  
    public static String ORIGINALPID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "originalPID");
    public static String ORIGINALSID = ConfigProp.getPropertyValue(TEMPLATE_FILE, "originalSID");
}
