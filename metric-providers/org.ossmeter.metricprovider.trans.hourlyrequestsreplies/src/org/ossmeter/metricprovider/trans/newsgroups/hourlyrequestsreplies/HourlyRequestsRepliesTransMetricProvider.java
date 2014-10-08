package org.ossmeter.metricprovider.trans.newsgroups.hourlyrequestsreplies;

import java.util.Arrays;
import java.util.List;

import org.ossmeter.metricprovider.trans.newsgroups.hourlyrequestsreplies.model.HourArticles;
import org.ossmeter.metricprovider.trans.newsgroups.hourlyrequestsreplies.model.NewsgroupsHourlyRequestsRepliesTransMetric;
import org.ossmeter.metricprovider.trans.requestreplyclassification.RequestReplyClassificationTransMetricProvider;
import org.ossmeter.metricprovider.trans.requestreplyclassification.model.NewsgroupArticlesData;
import org.ossmeter.metricprovider.trans.requestreplyclassification.model.RequestReplyClassificationTransMetric;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.ITransientMetricProvider;
import org.ossmeter.platform.MetricProviderContext;
import org.ossmeter.platform.delta.ProjectDelta;
import org.ossmeter.platform.delta.communicationchannel.CommunicationChannelArticle;
import org.ossmeter.platform.delta.communicationchannel.CommunicationChannelDelta;
import org.ossmeter.platform.delta.communicationchannel.CommunicationChannelProjectDelta;
import org.ossmeter.platform.delta.communicationchannel.PlatformCommunicationChannelManager;
import org.ossmeter.repository.model.CommunicationChannel;
import org.ossmeter.repository.model.Project;
import org.ossmeter.repository.model.cc.nntp.NntpNewsGroup;

import com.mongodb.DB;

public class HourlyRequestsRepliesTransMetricProvider implements ITransientMetricProvider<NewsgroupsHourlyRequestsRepliesTransMetric>{

	protected PlatformCommunicationChannelManager communicationChannelManager;

	protected MetricProviderContext context;
	
	protected List<IMetricProvider> uses;

	@Override
	public String getIdentifier() {
		return HourlyRequestsRepliesTransMetricProvider.class.getCanonicalName();
	}

	@Override
	public boolean appliesTo(Project project) {
		for (CommunicationChannel communicationChannel: project.getCommunicationChannels()) {
			if (communicationChannel instanceof NntpNewsGroup) return true;
		}
		return false;
	}

	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}

	@Override
	public List<String> getIdentifiersOfUses() {
		return Arrays.asList(RequestReplyClassificationTransMetricProvider.class.getCanonicalName());
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.context = context;
		this.communicationChannelManager = context.getPlatformCommunicationChannelManager();
	}

	@Override
	public NewsgroupsHourlyRequestsRepliesTransMetric adapt(DB db) {
		return new NewsgroupsHourlyRequestsRepliesTransMetric(db);
	}

	@Override
	public void measure(Project project, ProjectDelta projectDelta, NewsgroupsHourlyRequestsRepliesTransMetric db) {
		
		String[] hoursOfDay = new String[]{"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"};
		for (String hour : hoursOfDay) {
			HourArticles hourArticles = db.getHourArticles().findOneByHour(hour+":00");
			if (hourArticles == null) {
				hourArticles = new HourArticles();
				hourArticles.setHour(hour+":00");
				hourArticles.setNumberOfArticles(0);
				hourArticles.setNumberOfRequests(0);
				hourArticles.setNumberOfReplies(0);
				db.getHourArticles().add(hourArticles);
				db.sync();
			}
		}

		CommunicationChannelProjectDelta delta = projectDelta.getCommunicationChannelDelta();
		
		RequestReplyClassificationTransMetric usedClassifier = 
				((RequestReplyClassificationTransMetricProvider)uses.get(0)).adapt(context.getProjectDB(project));

		for ( CommunicationChannelDelta communicationChannelDelta: delta.getCommunicationChannelSystemDeltas()) {
			CommunicationChannel communicationChannel = communicationChannelDelta.getCommunicationChannel();
			if (!(communicationChannel instanceof NntpNewsGroup)) continue;
			NntpNewsGroup newsgroup = (NntpNewsGroup) communicationChannel;
			
			List<CommunicationChannelArticle> articles = communicationChannelDelta.getArticles();
			for (CommunicationChannelArticle article: articles) {
				@SuppressWarnings("deprecation")
				String hourNumber = String.format("%02d", article.getDate().getHours());
				
				HourArticles hourArticles = db.getHourArticles().findOneByHour(hourNumber + ":00");
				hourArticles.setNumberOfArticles(hourArticles.getNumberOfArticles()+1);
				String requestReplyClass = getRequestReplyClass(usedClassifier, newsgroup, article);
				if (requestReplyClass.equals("Request"))
					hourArticles.setNumberOfRequests(hourArticles.getNumberOfRequests()+1);
				else if (requestReplyClass.equals("Reply"))
					hourArticles.setNumberOfReplies(hourArticles.getNumberOfReplies()+1);
				db.sync();
			}
		}
	}

	private String getRequestReplyClass(RequestReplyClassificationTransMetric usedClassifier, 
			NntpNewsGroup newsgroup, CommunicationChannelArticle article) {
		Iterable<NewsgroupArticlesData> newsgroupArticlesDataIt = usedClassifier.getNewsgroupArticles().
				find(NewsgroupArticlesData.URL.eq(newsgroup.getUrl()), 
						NewsgroupArticlesData.ARTICLENUMBER.eq(article.getArticleNumber()));
		NewsgroupArticlesData newsgroupArticleData = null;
		for (NewsgroupArticlesData art:  newsgroupArticlesDataIt) {
			newsgroupArticleData = art;
		}
		if (newsgroupArticleData == null) {
			System.err.println("Active users metric -\t" + 
					"there is no classification for article: " + article.getArticleNumber() +
					"\t of newsgroup: " + newsgroup.getUrl());
			System.exit(-1);
		} else{
			return newsgroupArticleData.getClassificationResult();
		}
		return "";
	}

	@Override
	public String getShortIdentifier() {
		return "hourlyrequestsreplies";
	}

	@Override
	public String getFriendlyName() {
		return "Number of Articles, Requests and Replies per Hour of the Day";
	}

	@Override
	public String getSummaryInformation() {
		return "This metric stores the number of articles, " +
				"requests and replies for each hour of the day.";
	}

}