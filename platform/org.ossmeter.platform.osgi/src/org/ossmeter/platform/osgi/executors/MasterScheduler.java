/*******************************************************************************
 * Copyright (c) 2014 OSSMETER Partners.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    James Williams - Implementation.
 *******************************************************************************/
package org.ossmeter.platform.osgi.executors;

import org.ossmeter.platform.osgi.old.IScheduler;

public class MasterScheduler implements IScheduler {

	@Override
	public void run() {

	}
	
	@Override
	public boolean finish() {
		return true;
	}
	
	@Override 
	public SchedulerStatus getStatus(){
		return null;
	}

}
