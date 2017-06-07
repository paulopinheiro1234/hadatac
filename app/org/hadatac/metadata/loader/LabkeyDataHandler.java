package org.hadatac.metadata.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.plaf.synth.SynthStyleFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.function.library.print;
import org.hadatac.console.views.html.deployments.newDeployment;
import org.apache.jena.rdf.model.Literal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.DeleteRowsCommand;
import org.labkey.remoteapi.query.ExecuteSqlCommand;
import org.labkey.remoteapi.query.GetQueriesCommand;
import org.labkey.remoteapi.query.GetQueriesResponse;
import org.labkey.remoteapi.query.InsertRowsCommand;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.remoteapi.query.UpdateRowsCommand;
import org.labkey.remoteapi.query.SaveRowsResponse;
import org.labkey.remoteapi.security.GetContainersCommand;
import org.labkey.remoteapi.security.GetContainersResponse;

import org.labkey.remoteapi.security.EnsureLoginCommand;

public class LabkeyDataHandler {
	public Map< String, Map< String, List<PlainTriple> > > mapQueryNameToTriples = 
			new HashMap< String, Map< String, List<PlainTriple> > >();
	private Connection cn = null;
	private String folder_path = "";
	private List<String> tableNames = null;
	
	public static class PlainTriple {
		public String sub;
		public String pred;
		public String obj;
	}
	
	public LabkeyDataHandler(String labkey_site, String user_name, 
			String password, String path) {
		cn = new Connection(labkey_site, user_name, password);
		setFolderPath(path);
	}
	
	public void setFolderPath(String path){
		this.folder_path = path;
	}
	
	public String getFolderPath(){
		return folder_path;
	}
	
	private String replaceIrregularCharacters(String str){
		String ret = str;
		String invalid_char = " .;{}()/\\";
		for (int i = 0, n = invalid_char.length(); i < n; i++) {
		    char c = invalid_char.charAt(i);
		    ret = ret.replace(String.valueOf(c), "-");
		}
		return ret;
	}
	
	public Map< String, List<PlainTriple> > selectRows(String queryName, List<String> cols) throws CommandException {
		Map< String, List<PlainTriple> > mapRow = new HashMap< String, List<PlainTriple> >();
		SelectRowsCommand cmd = new SelectRowsCommand("lists", queryName);
		cmd.setRequiredVersion(9.1);
		cmd.setColumns(cols);
		int nTriples = 0;
		try {
			SelectRowsResponse response = cmd.execute(cn, folder_path);
			for (Map<String, Object> row : response.getRows()){
				String pri_key = "";
				for(String the_key : row.keySet()){
					if(the_key.toLowerCase().contains("uri")){
						pri_key = the_key;
					}
				}
				String sub = ((JSONObject)row.get(pri_key)).get("value").toString();
				List<PlainTriple> triples = new LinkedList<PlainTriple>();
				for(Object pred : row.keySet()){
					if(((String)pred).equals(pri_key)){
						continue;
					}
					PlainTriple tri = new PlainTriple();
					tri.sub = replaceIrregularCharacters(sub);
					tri.pred = replaceIrregularCharacters(pred.toString());
					Object obj_value = ((JSONObject)row.get(pred.toString())).get("value");
					if(obj_value == null){
						continue;
					}
					else{
						tri.obj = obj_value.toString();
					}
			        triples.add(tri);
			        nTriples++;
				}
				mapRow.put(sub, triples);
			}
			
			System.out.println(String.format("Read %d row(s) with %d triple(s) from Table \"%s\"", 
					response.getRowCount(), nTriples, queryName));
			return mapRow;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		
		return mapRow;
	}
	
	public int updateRows(String queryName, List< Map<String, Object> > rows) throws CommandException {
		UpdateRowsCommand cmdUpd = new UpdateRowsCommand("lists", queryName);
		for (Map<String, Object> row : rows) {
			cmdUpd.addRow(row);
		}

		try {
			SaveRowsResponse response = cmdUpd.execute(cn, folder_path);
			return response.getRowsAffected().intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public int insertRows(String queryName, List< Map<String, Object> > rows) throws CommandException {
		InsertRowsCommand cmd = new InsertRowsCommand("lists", queryName);
		for (Map<String, Object> row : rows) {
			cmd.addRow(row);
//			System.out.println(row.get("hasURI"));
		}

		try {
			SaveRowsResponse response = cmd.execute(cn, folder_path);
			return response.getRowsAffected().intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public int deleteRows(String queryName, List< Map<String, Object> > rows) throws CommandException {
		DeleteRowsCommand cmd = new DeleteRowsCommand("lists", queryName);
		for (Map<String, Object> row : rows) {
			cmd.addRow(row);
		}

		try {
			SaveRowsResponse response = cmd.execute(cn, folder_path);
			return response.getRowsAffected().intValue();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public void selectInfoFromTables(String uri, Model model) throws CommandException {
		if (null == tableNames) {
			tableNames = getAllQueryNames();
			System.out.println("tableNames: " + tableNames);
		}
		for (String table : tableNames) {
			if (table.equals("StudyReport")) {
				continue;
			}
			selectInfo(uri, model);
		}
	}
	
	public void selectInfo(String uri, Model model) throws CommandException {
		ExecuteSqlCommand cmd = new ExecuteSqlCommand("lists");
		
		List<String> colNames = new ArrayList<String>();
		if (null == tableNames) {
			tableNames = getAllQueryNames();
			System.out.println("tableNames: " + tableNames);
		}
		String query = String.format("SELECT * FROM ");
		String prevTable = "";
		for (String table : tableNames) {
			colNames.addAll(getColumnNames(table, false));
			if (table.equals("StudyReport")) {
				continue;
			}
			if (!prevTable.equals("")) {
				query += String.format(" JOIN %s %s on %s.hasURI=%s.hasURI", 
						table, table.toLowerCase(), prevTable.toLowerCase(), table.toLowerCase());
				prevTable = table;
			}
			else {
				query += table + " " + table.toLowerCase();
				prevTable = table;
			}
		}
		query += " WHERE hasURI = \'" + ValueCellProcessing.replaceNameSpaceEx(uri) + "\'";
		System.out.println("\nquery: " + query);
		cmd.setSql(query);
		cmd.setTimeout(0);
		try {
			SelectRowsResponse response = cmd.execute(cn, folder_path);
			System.out.println("response.getRows(): " + response.getRows());
			for (Map<String, Object> row : response.getRows()){
				String pri_key = "";
				for (String the_key : colNames) {
					if(the_key.toLowerCase().contains("uri")){
						pri_key = the_key;
					}
				}
				String sub = row.get(pri_key).toString();
				if (!replaceIrregularCharacters(sub).equals(ValueCellProcessing.replaceNameSpaceEx(uri))) {
					continue;
				}
				
				Resource subject = model.createResource(uri);
				for (String pred : colNames) {
					if (((String)pred).equals(pri_key)) {
						continue;
					}
					Property predicate = model.createProperty(ValueCellProcessing.replacePrefixEx(
							replaceIrregularCharacters(pred.toString())));
					
					if (null == row.get(pred)) {
						continue;
					}
					String cellValue = row.get(pred).toString();
					System.out.println("cellValue: " + cellValue);
					if (ValueCellProcessing.isObjectSet(cellValue)) {
						System.out.println("cellValue is Object Set");
						StringTokenizer st;
						if (cellValue.contains("&")) {
							st = new StringTokenizer(cellValue, "&");
						}
						else {
							st = new StringTokenizer(cellValue, ",");
						}
						while (st.hasMoreTokens()) {
							Resource object = model.createResource(ValueCellProcessing.replacePrefixEx(
									replaceIrregularCharacters(st.nextToken().trim())));
							model.add(subject, predicate, object);
							selectInfoFromTables(object.getURI(), model);
						}
					}
					else if (ValueCellProcessing.isAbbreviatedURI(cellValue)) {
						System.out.println("cellValue is Resource");
						Resource object = model.createResource(ValueCellProcessing.replacePrefixEx(cellValue));
						model.add(subject, predicate, object);
						selectInfoFromTables(object.getURI(), model);
					}
					else {
						System.out.println("cellValue is Literal");
						Literal object = model.createLiteral(
								cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
						model.add(subject, predicate, object);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if (e.getMessage().equals("Unauthorized")) {
				throw e;
			}
			else {
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getAllQueryNames() throws CommandException {
		GetQueriesCommand cmd = new GetQueriesCommand("lists");
		cmd.setRequiredVersion(9.1);
		GetQueriesResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			return response.getQueryNames();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<String> getMetadataQueryNames(boolean bLocalTypesOnly) throws CommandException {
		GetQueriesCommand cmd = new GetQueriesCommand("lists");
		cmd.setRequiredVersion(9.1);
		GetQueriesResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			List<String> results = new LinkedList<String>();
			for(String query : response.getQueryNames()){
				List<String> cols = getColumnNames(query, false);
				if(containsMetaData(cols)){
					if(bLocalTypesOnly){
						if(query.startsWith("Local") && query.endsWith("Type")){
							results.add(query);
						}
					}
					else{
						results.add(query);
					}
				}
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public List<String> getInstanceDataQueryNames() throws CommandException {
		GetQueriesCommand cmd = new GetQueriesCommand("lists");
		cmd.setRequiredVersion(9.1);
		GetQueriesResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			List<String> results = new LinkedList<String>();
			for(String query : response.getQueryNames()){
				List<String> cols = getColumnNames(query, false);
				if(containsInstanceData(cols)){
					results.add(query);
				}
			}
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void checkAuthentication() throws CommandException {
		EnsureLoginCommand cmd = new EnsureLoginCommand();
		cmd.setRequiredVersion(9.1);
		try {
			cmd.execute(cn, "");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
	}
	
	public List<String> getSubfolders() throws CommandException {
		checkAuthentication();
		List<String> listFolders = new LinkedList<String>();
		GetContainersCommand cmd = new GetContainersCommand();
		cmd.setRequiredVersion(9.1);
		GetContainersResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			Map<String, Object> retMap = response.getParsedData();
			JSONArray subfolders = (JSONArray)((JSONObject)retMap).get("children");
			for(Object proj : subfolders){
				String proj_name = ((JSONObject)proj).get("title").toString();
				if(proj_name.equals("home") || proj_name.equals("Shared")){
					continue;
				}
				listFolders.add(proj_name);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		
		return listFolders;
	}
	
	public boolean isDefaultColumn(String col){
		if(col.equals("Created") 
				|| col.equals("lastIndexed") 
				|| col.equals("container") 
				|| col.equals("CreatedBy") 
				|| col.equals("Modified")
				|| col.equals("ModifiedBy")
				|| col.equals("EntityId")){
			return true;
		}
		return false;
	}
	
	public boolean containsInstanceData(List<String> columns){
		for(String col : columns){
			if(col.equals("a")){
				return true;
			}
		}
		return false;
	}
	
	public boolean containsMetaData(List<String> columns){
		for(String col : columns){
			if(col.contains("subClassOf")){
				return true;
			}
		}
		return false;
	}
	
	public List<String> getColumnNames(String query_name, boolean bRetDefault) throws CommandException {
		GetQueriesCommand cmd = new GetQueriesCommand("lists");
		cmd.setRequiredVersion(9.1);
		GetQueriesResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			List<String> cols = response.getColumnNames(query_name);
			if(!bRetDefault){
				Iterator<String> iter = cols.iterator();
				while(iter.hasNext()){
					String col = iter.next();
					if(isDefaultColumn(col)){
						iter.remove();
					}
				}
				return cols;
			}
			else{
				return cols;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				e.printStackTrace();
			}
		}
		
		return null;
	}
}