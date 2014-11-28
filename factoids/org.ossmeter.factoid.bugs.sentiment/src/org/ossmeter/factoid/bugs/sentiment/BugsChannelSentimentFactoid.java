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
package org.ossmeter.factoid.bugs.sentiment;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import org.ossmeter.metricprovider.historic.bugs.sentiment.SentimentHistoricMetricProvider;
import org.ossmeter.metricprovider.historic.bugs.sentiment.model.BugsSentimentHistoricMetric;
import org.ossmeter.platform.AbstractFactoidMetricProvider;
import org.ossmeter.platform.Date;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.delta.ProjectDelta;
import org.ossmeter.platform.factoids.Factoid;
import org.ossmeter.platform.factoids.StarRating;
import org.ossmeter.repository.model.Project;

import com.googlecode.pongo.runtime.Pongo;

public class BugsChannelSentimentFactoid extends AbstractFactoidMetricProvider{

	protected List<IMetricProvider> uses;
	
	@Override
	public String getShortIdentifier() {
		return "BugChannelSentiment";
	}

	@Override
	public String getFriendlyName() {
		return "Bug Channel Sentiment";
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
		return Arrays.asList(SentimentHistoricMetricProvider.IDENTIFIER);
	}

	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}

	@Override
	public void measureImpl(Project project, ProjectDelta delta, Factoid factoid) {
//		factoid.setCategory(FactoidCategory.BUGS);
		factoid.setName("Bug Channel Sentiment Factoid");

		SentimentHistoricMetricProvider sentimentProvider = null;

		for (IMetricProvider m : this.uses) {
			if (m instanceof SentimentHistoricMetricProvider) {
				sentimentProvider = (SentimentHistoricMetricProvider) m;
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
		List<Pongo> sentimentList = sentimentProvider.getHistoricalMeasurements(context, project, start, end);
		
		float averageSentiment = getAverageSentiment(sentimentList),
			  sentimentAtThreadBeggining = getSentimentAtThreadBeggining(sentimentList),
			  sentimentAtThreadEnd = getSentimentAtThreadEnd(sentimentList);

		if ( ( averageSentiment > 0.5 )
			   || ( ( sentimentAtThreadEnd - sentimentAtThreadBeggining > 0.25 ) 
					&& ( sentimentAtThreadBeggining > 0.15 ) )
		     ) {
			factoid.setStars(StarRating.FOUR);
		} else if ( ( averageSentiment > 0.25 )
					  || ( ( sentimentAtThreadEnd - sentimentAtThreadBeggining > 0.125 ) 
						 && ( sentimentAtThreadEnd > 0.0 ) )
				  ) {
			factoid.setStars(StarRating.THREE);
		} else if ( ( averageSentiment > 0 )
					  || ( sentimentAtThreadEnd - sentimentAtThreadBeggining > 0 ) ) {
			factoid.setStars(StarRating.TWO);
		} else {
			factoid.setStars(StarRating.ONE);
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		
		stringBuffer.append("The average sentimental polarity measured on all comments is ");
		stringBuffer.append(decimalFormat.format(averageSentiment));
		stringBuffer.append(".\nThe measurement lies in the range [-1,1], where -1 designates " +
							"entirely negative sentiment while 1 designates entirely positive " +
							"sentiment. The middle of the range, designates neutral sentiment.\n");

		stringBuffer.append("The average sentimental polarity in all bug tracking systems " +
							"associated with the project is ");
		if ( averageSentiment > 0.3 )
			stringBuffer.append("positive");
		else if ( averageSentiment > 0.15 )
			stringBuffer.append("weakly positive");
		else if ( averageSentiment > -0.15 )
			stringBuffer.append("neutral");
		if ( averageSentiment > -0.3 )
			stringBuffer.append("weakly negative");
		else 
			stringBuffer.append("negative");
		stringBuffer.append(".\n");
		
		stringBuffer.append("In the beginning of threads, the average sentiment score is ");
		stringBuffer.append(decimalFormat.format(sentimentAtThreadBeggining));
		stringBuffer.append(", while at the end of threads it is ");
		stringBuffer.append(decimalFormat.format(sentimentAtThreadEnd));
		stringBuffer.append(", showing that users ");
		if ( Math.abs( sentimentAtThreadBeggining - sentimentAtThreadEnd ) < 0.15 ) 
			stringBuffer.append("have similar feelings");
		else if ( sentimentAtThreadBeggining < sentimentAtThreadEnd )
			stringBuffer.append("are happier");
		else
			stringBuffer.append("are unhappier");
		stringBuffer.append("at the end of a discussion ");
		if ( Math.abs( sentimentAtThreadBeggining - sentimentAtThreadEnd ) < 0.15 )
			stringBuffer.append("as");
		else
			stringBuffer.append("than");
		stringBuffer.append(" in the beginning of it.\n");

		factoid.setFactoid(stringBuffer.toString());

	}
		
	private float getAverageSentiment(List<Pongo> sentimentList) {
		if ( sentimentList.size() > 0 ) {
			BugsSentimentHistoricMetric sentimentPongo = 
					(BugsSentimentHistoricMetric) sentimentList.get(sentimentList.size()-1);
			return sentimentPongo.getOverallAverageSentiment();
		}
		return 0;
	}

	private float getSentimentAtThreadBeggining(List<Pongo> sentimentList) {
		if ( sentimentList.size() > 0 ) {
			BugsSentimentHistoricMetric sentimentPongo = 
					(BugsSentimentHistoricMetric) sentimentList.get(sentimentList.size()-1);
			return sentimentPongo.getOverallSentimentAtThreadBeggining();
		}
		return 0;
	}
	
	private float getSentimentAtThreadEnd(List<Pongo> sentimentList) {
		if ( sentimentList.size() > 0 ) {
			BugsSentimentHistoricMetric sentimentPongo = 
					(BugsSentimentHistoricMetric) sentimentList.get(sentimentList.size()-1);
			return sentimentPongo.getOverallSentimentAtThreadEnd();
		}
		return 0;
	}

}
