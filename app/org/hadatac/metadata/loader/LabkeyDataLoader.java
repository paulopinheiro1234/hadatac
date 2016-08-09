package org.hadatac.metadata.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.labkey.remoteapi.CommandException;
import org.labkey.remoteapi.Connection;
import org.labkey.remoteapi.query.GetQueriesCommand;
import org.labkey.remoteapi.query.GetQueriesResponse;
import org.labkey.remoteapi.query.SelectRowsCommand;
import org.labkey.remoteapi.query.SelectRowsResponse;
import org.labkey.remoteapi.security.GetContainersCommand;
import org.labkey.remoteapi.security.GetContainersResponse;
import org.labkey.remoteapi.security.EnsureLoginCommand;

public class LabkeyDataLoader {
	public Map< String, Map< String, List<PlainTriple> > > mapQueryNameToTriples = 
			new HashMap< String, Map< String, List<PlainTriple> > >();
	private Connection cn = null;
	private String folder_path = "";
	
	public static class PlainTriple {
		public String sub;
		public String pred;
		public String obj;
	}
	
	public LabkeyDataLoader(String labkey_site, String user_name, String password, String path){
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
					Object obj_value = ((JSONObject)row.get(tri.pred)).get("value");
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

	public List<String> getMetadataQueryNames() throws CommandException {
		GetQueriesCommand cmd = new GetQueriesCommand("lists");
		cmd.setRequiredVersion(9.1);
		GetQueriesResponse response;
		try {
			response = cmd.execute(cn, folder_path);
			List<String> results = new LinkedList<String>();
			for(String query : response.getQueryNames()){
				List<String> cols = getColumnNames(query, false);
				if(containsMetaData(cols)){
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
	
	private void checkAuthentication() throws CommandException {
		List<String> listFolders = new LinkedList<String>();
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
	
	public void insertRowData(){
//		InsertRowsCommand cmd = new InsertRowsCommand("lists", "Instruments");
//
//		Map<String,Object> row = new HashMap<String,Object>();
//		row.put("FirstName", "Insert");
//		row.put("LastName", "Test");
//		cmd.addRow(row); //can add multiple rows to insert many at once
//		try {
//			SaveRowsResponse response = cmd.execute(cn, "/Test");
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (CommandException e) {
//			e.printStackTrace();
//		}
//
//		get the newly-assigned primary key value from the first return row
//		int newKey = resp.getRows().get(0).get("Key");
	}
}