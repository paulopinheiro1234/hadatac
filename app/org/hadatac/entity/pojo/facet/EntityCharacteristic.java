package org.hadatac.entity.pojo.facet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.entity.pojo.Characteristic;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.utils.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;

import play.Play;

public class EntityCharacteristic {
	
	public List<Entity> entities;
	
	private EntityCharacteristic(String userUri) {
		entities = new ArrayList<Entity>();
		
		String dataAcquisitionQuery = "";
		List<String> dataAcquisitions = DataAcquisition.findAllAccessibleDataAcquisition(userUri);
		Iterator<String> i = dataAcquisitions.iterator();
		while (i.hasNext()) {
			String dataAcquisition = i.next();
			dataAcquisitionQuery += "acquisition_uri_str" + ":\"" + dataAcquisition + "\"";
			if (i.hasNext()) {
				dataAcquisitionQuery += " OR ";
			}
		}
		
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.set("q", dataAcquisitionQuery);
		solrQuery.setFacet(true);
		solrQuery.set("facet.pivot", "entity_uri_str,characteristic_uri_str");
		solrQuery.set("rows", "0");

		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		
		try {
			QueryResponse response = solr.query(solrQuery);
			solr.close();
			
			List<PivotField> pivotFields1 = response.getFacetPivot().get("entity_uri_str,characteristic_uri_str");
			Iterator<PivotField> i_pivotFields1 = pivotFields1.iterator();
			while (i_pivotFields1.hasNext()) {
				PivotField field1 = i_pivotFields1.next();
				Entity entity = Entity.find(field1.getValue().toString());
				entities.add(entity);
				System.out.println("PIVOT 1 " + entity.getLabel());
				List<PivotField> pivotFields2 = field1.getPivot();
				Iterator<PivotField> i_pivotFields2 = pivotFields2.iterator();
				while (i_pivotFields2.hasNext()) {
					PivotField field2 = i_pivotFields2.next();
					Characteristic characteristic = Characteristic.find(field2.getValue().toString());
					entity.characteristics.add(characteristic);
					System.out.println("PIVOT 2   " + characteristic.getLabel());
				}
			}
		} catch (Exception e) {
			System.out.println("[ERROR] EntityCharacteristic.find(String) - Exception message: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static EntityCharacteristic create(String userUri) {
		return new EntityCharacteristic(userUri);
	}
	
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String output =  mapper.writeValueAsString(this);
			System.out.println(output);
			return output;
		} catch (Exception e) {
			System.out.println("[ERROR] EntityCharacteristic.toJSON() - Exception message: " + e.getMessage());
		}
		return "";
	}
}