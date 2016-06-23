package org.hadatac.metadata.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.hadatac.metadata.loader.LabkeyDataLoader.PlainTriple;
import org.hadatac.metadata.model.SpreadsheetParsingResult;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;

public class TripleProcessing {
	
	public static final String KB_FORMAT = "text/turtle";
	
	public static final String TTL_DIR = "tmp/ttl/";
	
	public static String printFileWithLineNumber(int mode, String filename) {
		String str = "";
		int lineNumber = 1;

        LineNumberReader reader = null;
        String line = null;

        try {
            reader = new LineNumberReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                str += Feedback.println(mode, lineNumber++ + " " + line);
            }
            reader.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return str;
	}
	
	public static SpreadsheetParsingResult generateTTL(int mode, 
			Map< String, List<PlainTriple> > sheet, List<String> predicates) {
		String shttl = "";
		String message = "";

		ValueCellProcessing cellProc = new ValueCellProcessing();
		
		// Prints all identified predicates as a turtle comment 
		shttl = shttl + "# properties: ";
		for (String pred : predicates) {
			cellProc.validateNameSpace(pred);
			shttl = shttl + "[" + pred + "] ";
		}
		shttl = shttl + "\n";
		
		int processedRows = 0;
		int processedTriples = 0;
		for (String uri : sheet.keySet()) {
			List<PlainTriple> row = sheet.get(uri);
			shttl = shttl + processTriplesOfRow(row, predicates);
			processedRows++;
			processedTriples += row.size();
		}
		
		System.out.println(String.format("%d rows processed!", processedRows));
		message += Feedback.println(mode, "processed " + processedRows + " row(s) " + "( " + processedTriples + " Triples ).");
		SpreadsheetParsingResult result = new SpreadsheetParsingResult(message, shttl);
		return result;
	}
	
	public static String processTriplesOfRow(List<PlainTriple> triples, List<String> predicates) {
		String clttl = "";
		
		boolean bListSubject = false;
		ValueCellProcessing cellProc = new ValueCellProcessing();
		Iterator<PlainTriple> iterTriple = triples.iterator();
		while(iterTriple.hasNext()){
			PlainTriple triple = iterTriple.next();
			String cellValue = triple.obj.trim();
			String predicate = triple.pred.trim();
			
			if(!bListSubject){
				clttl = clttl + cellProc.processSubjectValue(triple.sub.trim());
				bListSubject = true;
			}
			
			// cell has object value
			clttl = clttl + "   " + predicate + " ";
			if (cellProc.isObjectSet(cellValue)) {
				StringTokenizer st = new StringTokenizer(cellValue,",");
				while (st.hasMoreTokens()) {
					clttl = clttl + cellProc.processObjectValue(st.nextToken().trim());
					if (st.hasMoreTokens()){
						clttl = clttl + ", ";
					}
				}
			}
			else{
				clttl = clttl + cellProc.processObjectValue(cellValue);
			}
			if(iterTriple.hasNext()){
				clttl = clttl + " ; \n";
			}
			else{
				clttl = clttl + " . \n\n";
			}
		}
				
		return clttl;
	}

    public static String generateTTL(int mode, String oper, RDFContext rdf, 
    		String labkey_site, String user_name, String password, String path) throws CommandException {

		String message = "";
		if (oper.equals("load")) {
		   message += Feedback.println(mode, "   Triples before loading from LABKEY: " + rdf.totalTriples());
		   message += Feedback.println(mode, " ");
		}
		
		LabkeyDataLoader loader = new LabkeyDataLoader(labkey_site, user_name, password, path);
		Map< String, Map< String, List<PlainTriple> > > mapSheets = 
				new HashMap< String, Map< String, List<PlainTriple> > >();
		Map< String, List<String> > mapPreds = 
				new HashMap< String, List<String> >();
		
		try {
			List<String> queryNames = loader.getAllQueryNames();
			for(String query : queryNames){
				List<String> cols = loader.getColumnNames(query, false);
				if(loader.containsInstanceData(cols)){
					mapSheets.put(query, loader.selectRows(query, cols));
				}
				mapPreds.put(query, cols);
			}
			System.out.println("Data extraction finished...");
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				message += e.getMessage();
			}
			return message;
		}
		
		message += Feedback.println(mode, "   Parsing triples from LABKEY " );
		message += Feedback.println(mode, " ");
		
		String ttl = "";
		
		// print the registered list of name spaces 
		NameSpaces ns = NameSpaces.getInstance();
		ttl = ttl + ns.printNameSpaceList();
		
		for(String queryName : mapSheets.keySet()){
			Map< String, List<PlainTriple> > sheet = mapSheets.get(queryName);
			message += Feedback.print(mode, "   Processing sheet " + queryName + "     ");
			for (int i = queryName.length(); i < 25; i++) {
				message += Feedback.print(mode, ".");
			}
			SpreadsheetParsingResult result = generateTTL(mode, sheet, mapPreds.get(queryName));
			ttl = ttl + "\n# concept: " + queryName + result.getTurtle() + "\n";
			message += result.getMessage();
		}
			
		String fileName = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			fileName = TTL_DIR + "HASNetO-" + timeStamp + ".ttl";
			System.out.println(fileName);
			FileUtils.writeStringToFile(new File(fileName), ttl);
			System.out.println("Turtle file created!");
		} catch (IOException e) {
			message += e.getMessage();
			return message;
		}
		
		String listing = "";
		try {
			listing = URLEncoder.encode(SpreadsheetProcessing.printFileWithLineNumber(mode, fileName), "UTF-8");
			//System.out.println(listing);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		};

		System.out.println("");
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + fileName + " and stored locally.");
		try {
			Model model = RDFDataMgr.loadModel(fileName);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM LABKEY ====");
			message += listing;
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM LABKEY ====");
			message += listing;
			return message;
		}

		if (oper.equals("load")) {
		    message += Feedback.print(mode, "   Uploading generated file.");
		    rdf.loadLocalFile(mode, fileName, KB_FORMAT);
		    message += Feedback.println(mode, "");
		    message += Feedback.println(mode, " ");
		    message += Feedback.println(mode, "   Triples after [loading from LABKEY]: " + rdf.totalTriples());
		}

	    return message;
	}
}