package org.ossmeter.metricprovider.threads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ossmeter.platform.communicationchannel.nntp.Article;
import org.apache.commons.net.nntp.Threader;
import org.ossmeter.metricprovider.threads.model.ArticleData;
import org.ossmeter.metricprovider.threads.model.Threads;
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

public class ThreadsMetricProvider implements ITransientMetricProvider<Threads>{

	protected final int STEP = 15;
	
	protected PlatformCommunicationChannelManager communicationChannelManager;

	@Override
	public String getIdentifier() {
		return ThreadsMetricProvider.class.getCanonicalName();
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
		// DO NOTHING -- we don't use anything
	}

	@Override
	public List<String> getIdentifiersOfUses() {
		return Collections.emptyList();
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.communicationChannelManager = context.getPlatformCommunicationChannelManager();
	}

	@Override
	public Threads adapt(DB db) {
		return new Threads(db);
	}

	@Override
	public void measure(Project project, ProjectDelta projectDelta, Threads db) {
		CommunicationChannelProjectDelta delta = projectDelta.getCommunicationChannelDelta();
		for ( CommunicationChannelDelta communicationChannelDelta: delta.getCommunicationChannelSystemDeltas()) {
			CommunicationChannel communicationChannel = communicationChannelDelta.getCommunicationChannel();
			
			if (communicationChannelDelta.getArticles().size() > 0) {
				List<Article> articles = new ArrayList<Article>();
				for (ArticleData articleData: db.getArticles()) {
					Article article = new Article();
					article.setArticleId(articleData.getArticleId());
					article.setArticleNumber(articleData.getArticleNumber());
					article.setDate(articleData.getDate());
					article.setFrom(articleData.getFrom());
					article.setSubject(articleData.getSubject());
					for (String reference: articleData.getReferences().split(" "))
						article.addReference(reference);
//			System.out.println("|1| " + 
//								articleData.getArticleId() + "\t" +
//								articleData.getArticleNumber() + "\t" +
//								articleData.getDate() + "\t" +
//								articleData.getFrom() + "\t" +
//								articleData.getSubject() + "\t|" +
//								articleData.getReferences() + "|");
					articles.add(article);
				}
				
				
				if (!(communicationChannel instanceof NntpNewsGroup)) continue;
				NntpNewsGroup newsgroup = (NntpNewsGroup) communicationChannel;
				for (CommunicationChannelArticle deltaArticle :communicationChannelDelta.getArticles()) {
					Iterable<ArticleData> articleDataIt = db.getArticles().
							find(ArticleData.URL_NAME.eq(newsgroup.getUrl()), 
									ArticleData.ARTICLENUMBER.eq(deltaArticle.getArticleNumber()));
					ArticleData articleData = null;
					for (ArticleData art:  articleDataIt) {
						articleData = art;
					}
					if (articleData == null) {
						Article article = new Article();
						article.setArticleId(deltaArticle.getArticleId());
						article.setArticleNumber(deltaArticle.getArticleNumber());
						article.setDate(deltaArticle.getDate().toString());
						article.setFrom(deltaArticle.getUser());
						article.setSubject(deltaArticle.getSubject());
						for (String reference: deltaArticle.getReferences())
							article.addReference(reference);
						articles.add(article);
						
						articleData = new ArticleData();
						articleData.setUrl_name(newsgroup.getUrl());
						articleData.setArticleId(deltaArticle.getArticleId());
						articleData.setArticleNumber(deltaArticle.getArticleNumber());
						articleData.setDate(deltaArticle.getDate().toString());
						articleData.setFrom(deltaArticle.getUser());
						articleData.setSubject(deltaArticle.getSubject());
						String references = "";
						for (String reference: deltaArticle.getReferences())
							references += " " + reference;
						articleData.setReferences(references.trim());
						
//					System.out.println("|2| " + 
//							articleData.getArticleId() + "\t" +
//							articleData.getArticleNumber() + "\t" +
//							articleData.getDate() + "\t" +
//							articleData.getFrom() + "\t" +
//							articleData.getSubject() + "\t|" +
//							articleData.getReferences() + "|");
						db.getArticles().add(articleData);
					} 
				}
				db.sync();

				System.out.println("Building message thread tree... (" + articles.size() + ")");
				Threader threader = new Threader();
				Article root = (Article)threader.thread(articles);
				List<List<Article>> articleList = zeroLevelCall(root);
//				for (List<Article>list: articleList) {
//					System.out.print(" [ ");
//					for (Article art: list)
//						System.out.print(art.getArticleNumber() + " ");
//					System.out.print("] ");
//				}
//				System.out.println();
//				System.out.println("-=-=-=-=-=-=-=-");
//				Article.printThread(root, 0);
//				System.out.println("-=-=-=-=-=-=-=-");
				
				int index = 0;
				for (List<Article>list: articleList) {
					index++;
					for (Article article: list) {
						Iterable<ArticleData> articleDataIt = db.getArticles().
								find(ArticleData.ARTICLEID.eq(article.getArticleId()), 
										ArticleData.ARTICLENUMBER.eq(article.getArticleNumber()));
						ArticleData articleData = null;
						for (ArticleData art:  articleDataIt)
							articleData = art;
						articleData.setThreadId(index);
					}
				}
				db.sync();
			}
		}
	}

	@Override
	public String getShortIdentifier() {
		return "threads";
	}

	@Override
	public String getFriendlyName() {
		return "Threads";
	}

	@Override
	public String getSummaryInformation() {
		return "This metric holds information for assigning newsgroup articles to threads. " +
				"The threading algorithm is executed from scratch everytime.";
	}
	
	public static List<List<Article>> zeroLevelCall(Article article) {
		List<List<Article>> threadList = new ArrayList<List<Article>>();
		while (article!=null) {
			List<Article> articleNumbers = new ArrayList<Article>();
			if (article.getArticleNumber()>0)
				articleNumbers.add(article);
			if (article.kid != null)
				articleNumbers.addAll(higherLevelCall(article.kid));
			Collections.sort(articleNumbers);
			if (threadList.size()==0)
				threadList.add(articleNumbers);
			else {
				int index=0;
				while ((index<threadList.size()) && 
						(articleNumbers.get(0).getArticleNumber() > 
							threadList.get(index).get(0).getArticleNumber()))
					index++;
				threadList.add(index, articleNumbers);
			}
				
			article = article.next;
		}
		return threadList;
	}

	public static List<Article> higherLevelCall(Article article) {
		List<Article> articleNumbers = new ArrayList<Article>();
		if (article.getArticleNumber()>0)
			articleNumbers.add(article);
		if (article.kid != null)
			articleNumbers.addAll(higherLevelCall(article.kid));
		if (article.next != null)
			articleNumbers.addAll(higherLevelCall(article.next));
		return articleNumbers;
	}
	
}