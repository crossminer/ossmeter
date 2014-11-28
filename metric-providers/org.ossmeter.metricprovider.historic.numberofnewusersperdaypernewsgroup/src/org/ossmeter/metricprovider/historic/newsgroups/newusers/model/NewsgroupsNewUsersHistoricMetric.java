/*******************************************************************************
 * Copyright (c) 2014 OSSMETER Partners.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yannis Korkontzelos - Implementation.
 *******************************************************************************/
package org.ossmeter.metricprovider.historic.newsgroups.newusers.model;

import com.mongodb.*;
import java.util.*;
import com.googlecode.pongo.runtime.*;
import com.googlecode.pongo.runtime.querying.*;


public class NewsgroupsNewUsersHistoricMetric extends Pongo {
	
	protected List<DailyNewsgroupData> newsgroups = null;
	
	
	public NewsgroupsNewUsersHistoricMetric() { 
		super();
		dbObject.put("newsgroups", new BasicDBList());
		NUMBEROFNEWUSERS.setOwningType("org.ossmeter.metricprovider.historic.newsgroups.newusers.model.NewsgroupsNewUsersHistoricMetric");
		CUMULATIVENUMBEROFNEWUSERS.setOwningType("org.ossmeter.metricprovider.historic.newsgroups.newusers.model.NewsgroupsNewUsersHistoricMetric");
	}
	
	public static NumericalQueryProducer NUMBEROFNEWUSERS = new NumericalQueryProducer("numberOfNewUsers");
	public static NumericalQueryProducer CUMULATIVENUMBEROFNEWUSERS = new NumericalQueryProducer("cumulativeNumberOfNewUsers");
	
	
	public int getNumberOfNewUsers() {
		return parseInteger(dbObject.get("numberOfNewUsers")+"", 0);
	}
	
	public NewsgroupsNewUsersHistoricMetric setNumberOfNewUsers(int numberOfNewUsers) {
		dbObject.put("numberOfNewUsers", numberOfNewUsers);
		notifyChanged();
		return this;
	}
	public int getCumulativeNumberOfNewUsers() {
		return parseInteger(dbObject.get("cumulativeNumberOfNewUsers")+"", 0);
	}
	
	public NewsgroupsNewUsersHistoricMetric setCumulativeNumberOfNewUsers(int cumulativeNumberOfNewUsers) {
		dbObject.put("cumulativeNumberOfNewUsers", cumulativeNumberOfNewUsers);
		notifyChanged();
		return this;
	}
	
	
	public List<DailyNewsgroupData> getNewsgroups() {
		if (newsgroups == null) {
			newsgroups = new PongoList<DailyNewsgroupData>(this, "newsgroups", true);
		}
		return newsgroups;
	}
	
	
}