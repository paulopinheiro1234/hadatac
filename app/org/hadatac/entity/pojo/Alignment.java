package org.hadatac.entity.pojo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.hadatac.data.model.AcquisitionQueryResult;

public class Alignment {
	private List<StudyObject> objects;
	private List<Attribute> attributes;
	
	public Alignment() {
		objects = new ArrayList<StudyObject>();
		attributes = new ArrayList<Attribute>();
	}

	public List<StudyObject> getObjects() {
		return objects;
	}

	public void setObjects(List<StudyObject> objects) {
		this.objects = objects;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(Attribute attribute) {
		this.attributes.add(attribute);
	}
	
	public void outputAsCSV(File file, DataFile dataFile, String facets, String user_uri) {
		try {
			int count = 0;
			int total = objects.size() * (attributes.size() + 1);
			
			// Create headers
			FileUtils.writeStringToFile(file, "\"" + count++ + "-object\"", "utf-8", true);
			
			Iterator<Attribute> i_attribute = attributes.iterator();
			while (i_attribute.hasNext()) {
				Attribute attribute = i_attribute.next();
				FileUtils.writeStringToFile(file, ",\"" + count++ + "-" + attribute.getLabel() + "\"", "utf-8", true);
			}
			FileUtils.writeStringToFile(file, "\n", "utf-8", true);
			
			count = 0;
			// Create rows
			Iterator<StudyObject> i_object = objects.iterator();
			while (i_object.hasNext()) {
				StudyObject object = i_object.next();
				FileUtils.writeStringToFile(file, "\"" + object.getOriginalId() + "\"", "utf-8", true);
				count++;
				
				i_attribute = attributes.iterator();
				while (i_attribute.hasNext()) {
					Attribute attribute = i_attribute.next();
					AcquisitionQueryResult result = Measurement.findByObjectAttribute(user_uri, object, attribute, facets);
					String value;
					if (result.getDocuments().size() == 0) {
						value = "";
					} else {
						value = result.getDocuments().get(0).getValue();
					}
					FileUtils.writeStringToFile(file, ",\"" + value + "\"", "utf-8", true);
					
					int prev_ratio = 0;
		            double ratio = (double)count / total * 100;
		            if (((int)ratio) != prev_ratio) {
		                prev_ratio = (int)ratio;
		                dataFile.setCompletionPercentage((int)ratio);
		                dataFile.save();
		            }
		            count++;
				}
				FileUtils.writeStringToFile(file, "\n", "utf-8", true);
			}
			
			dataFile.setCompletionPercentage(100);
            dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.setStatus(DataFile.CREATED);
            dataFile.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
