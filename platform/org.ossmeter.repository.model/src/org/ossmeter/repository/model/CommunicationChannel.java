/*******************************************************************************
 * Copyright (c) 2014 OSSMETER Partners.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Davide Di Ruscio - Implementation.
 *******************************************************************************/
package org.ossmeter.repository.model;

import com.mongodb.*;
import java.util.*;
import com.googlecode.pongo.runtime.*;
import com.googlecode.pongo.runtime.querying.*;


public abstract class CommunicationChannel extends Pongo {
	
	protected List<Person> persons = null;
	
	
	public CommunicationChannel() { 
		super();
		dbObject.put("persons", new BasicDBList());
		URL.setOwningType("org.ossmeter.repository.model.CommunicationChannel");
		NONPROCESSABLE.setOwningType("org.ossmeter.repository.model.CommunicationChannel");
	}
	
	public static StringQueryProducer URL = new StringQueryProducer("url"); 
	public static StringQueryProducer NONPROCESSABLE = new StringQueryProducer("nonProcessable"); 
	
	
	public String getUrl() {
		return parseString(dbObject.get("url")+"", "");
	}
	
	public CommunicationChannel setUrl(String url) {
		dbObject.put("url", url);
		notifyChanged();
		return this;
	}
	public boolean getNonProcessable() {
		return parseBoolean(dbObject.get("nonProcessable")+"", false);
	}
	
	public CommunicationChannel setNonProcessable(boolean nonProcessable) {
		dbObject.put("nonProcessable", nonProcessable);
		notifyChanged();
		return this;
	}
	
	
	public List<Person> getPersons() {
		if (persons == null) {
			persons = new PongoList<Person>(this, "persons", false);
		}
		return persons;
	}
	
	
}