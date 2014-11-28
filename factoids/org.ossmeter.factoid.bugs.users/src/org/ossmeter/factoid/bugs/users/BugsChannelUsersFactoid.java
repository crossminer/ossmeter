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
package org.ossmeter.factoid.bugs.users;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.ossmeter.metricprovider.historic.bugs.bugs.BugsHistoricMetricProvider;
import org.ossmeter.metricprovider.historic.bugs.bugs.model.BugsBugsHistoricMetric;
import org.ossmeter.metricprovider.historic.bugs.users.UsersHistoricMetricProvider;
import org.ossmeter.metricprovider.historic.bugs.users.model.BugsUsersHistoricMetric;
import org.ossmeter.metricprovider.historic.bugs.users.model.DailyBugTrackingData;
import org.ossmeter.platform.AbstractFactoidMetricProvider;
import org.ossmeter.platform.Date;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.delta.ProjectDelta;
import org.ossmeter.platform.factoids.Factoid;
import org.ossmeter.platform.factoids.StarRating;
import org.ossmeter.repository.model.Project;

import com.googlecode.pongo.runtime.Pongo;

public class BugsChannelUsersFactoid extends AbstractFactoidMetricProvider{

	protected List<IMetricProvider> uses;
	
	@Override
	public String getShortIdentifier() {
		return "BugChannelUsers";
	}

	@Override
	public String getFriendlyName() {
		return "Bug Channel Users";
		// This method will NOT be removed in a later version.
	}

	@Override
	public String getSummaryInformation() {
		return "summaryblah"; // This method will NOT be removed in a later version.
	}

	@Override
	public boolean appliesTo(Project project) {
	    return !project.getBugTrackingSystems().isEmpty();	   
	}

	@Override
	public List<String> getIdentifiersOfUses() {
		return Arrays.asList(UsersHistoricMetricProvider.IDENTIFIER,
							 BugsHistoricMetricProvider.IDENTIFIER);
	}

	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}

	@Override
	public void measureImpl(Project project, ProjectDelta delta, Factoid factoid) {
//		factoid.setCategory(FactoidCategory.BUGS);
		factoid.setName("Bug Channel Users Factoid");

		UsersHistoricMetricProvider usersProvider = null;
		BugsHistoricMetricProvider bugsProvider = null;
		
		for (IMetricProvider m : this.uses) {
			if (m instanceof UsersHistoricMetricProvider) {
				usersProvider = (UsersHistoricMetricProvider) m;
				continue;
			}
			if (m instanceof BugsHistoricMetricProvider) {
				bugsProvider = (BugsHistoricMetricProvider) m;
				continue;
			}
		}

		Date end = new Date();
		Date start = new Date();
//		Date start=null, end=null;
//		try {
//			start = new Date("20050301");
//			end = new Date("20060301");
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		List<Pongo> bugList = bugsProvider.getHistoricalMeasurements(context, project, start, end);

		start = (new Date()).addDays(-30);
		List<Pongo> usersMonthList = usersProvider.getHistoricalMeasurements(context, project, start, end);
		
		start = (new Date()).addDays(-365);
		List<Pongo> usersYearList = usersProvider.getHistoricalMeasurements(context, project, start, end);
		
		StringBuffer stringBuffer = new StringBuffer();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		
		int currentUsers = getCurrentNumberOfUsers(usersMonthList),
			currentActiveUsers = getCurrentNumberOfActiveUsers(usersMonthList);
		float currentActivePercentage = ( (float) 100 * currentActiveUsers ) / currentUsers; 

		stringBuffer.append("The community of the project contains ");
		stringBuffer.append(currentUsers);
		stringBuffer.append(" users, of which currently ");
		stringBuffer.append(currentActiveUsers);
		stringBuffer.append(" are active (");
		stringBuffer.append(decimalFormat.format(currentActivePercentage));
		stringBuffer.append(" %).\n");
		stringBuffer.append("Each user has contributed approximately ");
		stringBuffer.append(getMessagesPerUser(bugList));
		stringBuffer.append(" messages, "); 
		stringBuffer.append(getMessagesPerRequests(bugList));
		stringBuffer.append(" requests and ");
		stringBuffer.append(getMessagesPerReplies(bugList));
		stringBuffer.append(" replies.\n");

		float dailyNumberOfNewUsersInTheLastMonth = getDailyNumberOfNewUsersInDuration(usersMonthList),
			  dailyNumberOfActiveUsersInTheLastMonth = getDailyNumberOfActiveUsersInDuration(usersMonthList),
			  dailyNumberOfNewUsersInTheLastYear = getDailyNumberOfNewUsersInDuration(usersYearList),
			  dailyNumberOfActiveUsersInTheLastYear = getDailyNumberOfActiveUsersInDuration(usersYearList);
		
		stringBuffer.append("There are approximately "); 
		stringBuffer.append(decimalFormat.format(dailyNumberOfNewUsersInTheLastMonth));
		stringBuffer.append(" new users per day in the last month, while approximately ");
		stringBuffer.append(decimalFormat.format(dailyNumberOfActiveUsersInTheLastMonth));
		stringBuffer.append(" users are active.\n");
		stringBuffer.append("In the last year, there are "); 
		stringBuffer.append(decimalFormat.format(dailyNumberOfNewUsersInTheLastYear));
		stringBuffer.append(" new users per day, ");
		stringBuffer.append(decimalFormat.format(dailyNumberOfActiveUsersInTheLastYear));
		stringBuffer.append(" of which are active.\n"); 
		
		float newUsersThreshold = 0.25f,
			  activeUsersThreshold = 2.5f;
		
		if ( ( dailyNumberOfNewUsersInTheLastMonth > 8 * newUsersThreshold ) 
		  || ( dailyNumberOfActiveUsersInTheLastMonth > 8 * activeUsersThreshold )
		  || ( dailyNumberOfNewUsersInTheLastYear > 4 * newUsersThreshold ) 
		  || ( dailyNumberOfActiveUsersInTheLastYear > 4 * activeUsersThreshold ) ) {
			factoid.setStars(StarRating.FOUR);
		} else if ( ( dailyNumberOfNewUsersInTheLastMonth > 4 * newUsersThreshold ) 
			   || ( dailyNumberOfActiveUsersInTheLastMonth > 4 * activeUsersThreshold )
			   || ( dailyNumberOfNewUsersInTheLastYear > 2 * newUsersThreshold ) 
			   || ( dailyNumberOfActiveUsersInTheLastYear > 2 * activeUsersThreshold ) ) {
			factoid.setStars(StarRating.THREE);
		} else if ( ( dailyNumberOfNewUsersInTheLastMonth > 2 * newUsersThreshold ) 
				 || ( dailyNumberOfActiveUsersInTheLastMonth > 2 * activeUsersThreshold )
				 || ( dailyNumberOfNewUsersInTheLastYear > newUsersThreshold ) 
				 || ( dailyNumberOfActiveUsersInTheLastYear > activeUsersThreshold ) ) {
			factoid.setStars(StarRating.TWO);
		} else {
			factoid.setStars(StarRating.ONE);
		}

		if ( usersMonthList.size() > 0 ) {
			BugsUsersHistoricMetric usersPongo = 
					(BugsUsersHistoricMetric) usersMonthList.get(usersMonthList.size()-1);
			int trackerNo = 1;
			for (DailyBugTrackingData dailyTrackerData: usersPongo.getBugTrackers()) {
				int numberOfUsers = dailyTrackerData.getNumberOfUsers(),
					numberOfActiveUsers = dailyTrackerData.getNumberOfActiveUsers();
				float percentageOfActiveUsers = ( (float) 100 * numberOfActiveUsers ) / numberOfUsers;
				stringBuffer.append("Tracker no. ");
				stringBuffer.append(trackerNo++);
				stringBuffer.append(" hosts ");
				stringBuffer.append(numberOfUsers);
				stringBuffer.append(" users, of which ");
				stringBuffer.append(numberOfActiveUsers);
				stringBuffer.append(" are currently active (");
				stringBuffer.append(decimalFormat.format(percentageOfActiveUsers));
				stringBuffer.append(" %).\n");
			}
		}
	
		factoid.setFactoid(stringBuffer.toString());

	}

	private int getCurrentNumberOfUsers(List<Pongo> usersList) {
		int numberOfUsers = 0;
		if ( usersList.size() > 0 ) {
			BugsUsersHistoricMetric usersPongo = 
					(BugsUsersHistoricMetric) usersList.get(usersList.size()-1);
			numberOfUsers = usersPongo.getNumberOfUsers();
		}
		return numberOfUsers;
	}

	private int getCurrentNumberOfActiveUsers(List<Pongo> usersList) {
		int numberOfActiveUsers = 0;
		if ( usersList.size() > 0 ) {
			BugsUsersHistoricMetric usersPongo = 
					(BugsUsersHistoricMetric) usersList.get(usersList.size()-1);
			numberOfActiveUsers = usersPongo.getNumberOfActiveUsers();
		}
		return numberOfActiveUsers;
	}

	private float getDailyNumberOfNewUsersInDuration(List<Pongo> usersList) {
		int numberOfNewUsers = 0;
		if ( usersList.size() > 0 ) {
			BugsUsersHistoricMetric firstUsersPongo = 
					(BugsUsersHistoricMetric) usersList.get(1);
			BugsUsersHistoricMetric lastUsersPongo = 
					(BugsUsersHistoricMetric) usersList.get(usersList.size()-1);
			int firstNumberOfUsers = firstUsersPongo.getNumberOfUsers();
			int lastNumberOfUsers = lastUsersPongo.getNumberOfUsers();
			return ( (float) (lastNumberOfUsers - firstNumberOfUsers) ) / usersList.size();
		}
		return numberOfNewUsers;
	}
	
	private float getDailyNumberOfActiveUsersInDuration(List<Pongo> usersList) {
		int numberOfActiveUsers = 0;
		if ( usersList.size() > 0 ) {
			for (Pongo pongo: usersList) {
				BugsUsersHistoricMetric usersPongo = (BugsUsersHistoricMetric) pongo;
				numberOfActiveUsers += usersPongo.getNumberOfActiveUsers();
			}
			return ( (float) numberOfActiveUsers ) / usersList.size();
		}
		return numberOfActiveUsers;
	}

	private float getMessagesPerUser(List<Pongo> threadList) {
		if ( threadList.size() > 0 ) {
			BugsBugsHistoricMetric threadPongo = 
					(BugsBugsHistoricMetric) threadList.get(threadList.size()-1);
			return threadPongo.getAverageCommentsPerUser();
		}
		return 0;
	}

	private float getMessagesPerRequests(List<Pongo> threadList) {
		if ( threadList.size() > 0 ) {
			BugsBugsHistoricMetric threadPongo = 
					(BugsBugsHistoricMetric) threadList.get(threadList.size()-1);
			return threadPongo.getAverageRequestsPerUser();
		}
		return 0;
	}
	
	private float getMessagesPerReplies(List<Pongo> threadList) {
		if ( threadList.size() > 0 ) {
			BugsBugsHistoricMetric threadPongo = 
					(BugsBugsHistoricMetric) threadList.get(threadList.size()-1);
			return threadPongo.getAverageRepliesPerUser();
		}
		return 0;
	}

}
