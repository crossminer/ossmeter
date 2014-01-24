package org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread.model.AverageArticlesPerThreadData;
import org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread.model.AverageRRThread;
import org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread.model.AverageRepliesPerThreadData;
import org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread.model.AverageRequestsPerThreadData;
import org.ossmeter.metricprovider.requestreplyclassification.RequestReplyClassificationMetricProvider;
import org.ossmeter.metricprovider.requestreplyclassification.model.NewsgroupArticlesData;
import org.ossmeter.metricprovider.requestreplyclassification.model.Rrc;
import org.ossmeter.metricprovider.threads.ThreadsMetricProvider;
import org.ossmeter.metricprovider.threads.model.ArticleData;
import org.ossmeter.metricprovider.threads.model.Threads;
import org.ossmeter.platform.IHistoricalMetricProvider;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.MetricProviderContext;
import org.ossmeter.repository.model.CommunicationChannel;
import org.ossmeter.repository.model.Project;
import org.ossmeter.repository.model.cc.nntp.NntpNewsGroup;

import com.googlecode.pongo.runtime.Pongo;

public class NumberOfRequestsRepliesPerThreadProvider implements IHistoricalMetricProvider{
	public final static String IDENTIFIER = 
			"org.ossmeter.metricprovider.generic.numberofrequestsrepliesperthread";

	protected MetricProviderContext context;
	
	/**
	 * List of MPs that are used by this MP. These are MPs who have specified that 
	 * they 'provide' data for this MP.
	 */
	protected List<IMetricProvider> uses;
	
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}
	
	@Override
	public boolean appliesTo(Project project) {
		for (CommunicationChannel communicationChannel: project.getCommunicationChannels()) {
			if (communicationChannel instanceof NntpNewsGroup) return true;
		}
		return false;
	}

	@Override
	public Pongo measure(Project project) {

		if (uses.size()!=2) {
			System.err.println("Metric: numberofrequestsrepliesperthread failed to retrieve " + 
								"the two transient metrics it needs!");
			System.exit(-1);
		}

		Threads usedThreads = 
				((ThreadsMetricProvider)uses.get(0)).adapt(context.getProjectDB(project));
		
		HashSet<Integer> threadIdSet = new HashSet<Integer>();
		for (ArticleData article: usedThreads.getArticles())
			threadIdSet.add(article.getThreadId());

		Rrc usedClassifier = 
				((RequestReplyClassificationMetricProvider)uses.get(1)).adapt(context.getProjectDB(project));

		int numberOrRequests = 0,
				numberOrReplies = 0;
		for (NewsgroupArticlesData articleData: usedClassifier.getNewsgroupArticles()) {
			if (articleData.getClassificationResult().equals("Reply"))
				numberOrReplies++;
			else if (articleData.getClassificationResult().equals("Request"))
					numberOrRequests++;
		}
		
		AverageArticlesPerThreadData avgArticles = new AverageArticlesPerThreadData();
		avgArticles.setAverageArticlesPerThread( 
				((float) usedThreads.getArticles().size()) / threadIdSet.size() );
			
		AverageRepliesPerThreadData avgReplies = new AverageRepliesPerThreadData();
		avgReplies.setAverageRepliesPerThread(
				((float) numberOrReplies) / threadIdSet.size());

		AverageRequestsPerThreadData avgRequests = new AverageRequestsPerThreadData();
		avgRequests.setAverageRequestsPerThread( 
				((float) numberOrRequests) / threadIdSet.size() );

		AverageRRThread avgRRThread = new AverageRRThread();
		avgRRThread.getArticles().add(avgArticles);
		avgRRThread.getReplies().add(avgReplies);
		avgRRThread.getRequests().add(avgRequests);
		
		return avgRRThread;
	}
			
	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}
	
	@Override
	public List<String> getIdentifiersOfUses() {
		return Arrays.asList(ThreadsMetricProvider.class.getCanonicalName(),
				 RequestReplyClassificationMetricProvider.class.getCanonicalName());
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.context = context;
	}

	@Override
	public String getShortIdentifier() {
		return "articlesrequestsrepliesperthread";
	}

	@Override
	public String getFriendlyName() {
		return "Average Number of Articles, Requests and Replies Per Thread";
	}

	@Override
	public String getSummaryInformation() {
		return "This class computes the average number of articles, " +
				"request and reply newsgroup articles per thread.";
	}

}
