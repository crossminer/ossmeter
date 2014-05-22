package org.ossmeter.metricprovider.trans.threads.model;

import com.googlecode.pongo.runtime.Pongo;
import com.googlecode.pongo.runtime.querying.NumericalQueryProducer;
import com.googlecode.pongo.runtime.querying.StringQueryProducer;


public class NewsgroupData extends Pongo {
	
	
	
	public NewsgroupData() { 
		super();
		URL_NAME.setOwningType("org.ossmeter.metricprovider.trans.threads.model.NewsgroupData");
		THREADS.setOwningType("org.ossmeter.metricprovider.trans.threads.model.NewsgroupData");
		PREVIOUSTHREADS.setOwningType("org.ossmeter.metricprovider.trans.threads.model.NewsgroupData");
	}
	
	public static StringQueryProducer URL_NAME = new StringQueryProducer("url_name"); 
	public static NumericalQueryProducer THREADS = new NumericalQueryProducer("threads");
	public static NumericalQueryProducer PREVIOUSTHREADS = new NumericalQueryProducer("previousThreads");
	
	
	public String getUrl_name() {
		return parseString(dbObject.get("url_name")+"", "");
	}
	
	public NewsgroupData setUrl_name(String url_name) {
		dbObject.put("url_name", url_name);
		notifyChanged();
		return this;
	}
	public int getThreads() {
		return parseInteger(dbObject.get("threads")+"", 0);
	}
	
	public NewsgroupData setThreads(int threads) {
		dbObject.put("threads", threads);
		notifyChanged();
		return this;
	}
	public int getPreviousThreads() {
		return parseInteger(dbObject.get("previousThreads")+"", 0);
	}
	
	public NewsgroupData setPreviousThreads(int previousThreads) {
		dbObject.put("previousThreads", previousThreads);
		notifyChanged();
		return this;
	}
	
	
	
	
}