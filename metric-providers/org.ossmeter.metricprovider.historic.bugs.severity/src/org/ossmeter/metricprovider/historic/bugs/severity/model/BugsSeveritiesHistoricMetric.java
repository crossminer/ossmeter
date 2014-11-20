package org.ossmeter.metricprovider.historic.bugs.severity.model;

import java.util.List;

import com.googlecode.pongo.runtime.Pongo;
import com.googlecode.pongo.runtime.PongoList;
import com.mongodb.BasicDBList;


public class BugsSeveritiesHistoricMetric extends Pongo {
	
	protected List<BugData> bugData = null;
	protected List<SeverityLevel> severityLevels = null;
	
	
	public BugsSeveritiesHistoricMetric() { 
		super();
		dbObject.put("bugData", new BasicDBList());
		dbObject.put("severityLevels", new BasicDBList());
	}
	
	
	
	
	
	public List<BugData> getBugData() {
		if (bugData == null) {
			bugData = new PongoList<BugData>(this, "bugData", true);
		}
		return bugData;
	}
	public List<SeverityLevel> getSeverityLevels() {
		if (severityLevels == null) {
			severityLevels = new PongoList<SeverityLevel>(this, "severityLevels", true);
		}
		return severityLevels;
	}
	
	
}