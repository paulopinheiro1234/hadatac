package org.hadatac.data.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.annotator.AnnotationLog;

public class GeneratorChain {

	private List<BasicGenerator> chain = new ArrayList<BasicGenerator>();

	public void addGenerator(BasicGenerator generator) {
		chain.add(generator);
	}

	public boolean generateTriples() {
		boolean bSucceed = true;
		for (BasicGenerator generator : chain) {
			try {
				List<Map<String, Object>> rows = null;
				if (generator.getRows().size() == 0) {
					rows = generator.createRows();
				} else {
					rows = generator.getRows();
				}
				
				if (generator.getCommitNeeded()) {
					bSucceed = generator.commitRowsToTripleStore(rows);
					bSucceed = generator.commitRowsToLabKey(rows);
				}
			} catch (Exception e) {
				System.out.println(generator.getErrorMsg(e));
				e.printStackTrace();
				AnnotationLog.printException(e, generator.getFileName());
				bSucceed = false;
			}
		}
		
		return bSucceed;
	}
	
	public void deleteTriples() {
		for (BasicGenerator generator : chain) {
			try {
				List<Map<String, Object>> rows = null;
				if (generator.getRows().size() == 0) {
					rows = generator.createRows();
				} else {
					rows = generator.getRows();
				}
				
				generator.deleteRowsFromTripleStore(rows);
			} catch (Exception e) {
				System.out.println(generator.getErrorMsg(e));
				e.printStackTrace();
				AnnotationLog.printException(e, generator.getFileName());
			}
		}
	}
}
