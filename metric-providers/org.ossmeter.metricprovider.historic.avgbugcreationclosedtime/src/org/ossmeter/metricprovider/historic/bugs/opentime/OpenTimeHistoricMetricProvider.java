package org.ossmeter.metricprovider.historic.bugs.opentime;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.ossmeter.metricprovider.historic.bugs.opentime.model.BugsOpenTimeHistoricMetric;
import org.ossmeter.metricprovider.trans.bugs.bugmetadata.BugMetadataTransMetricProvider;
import org.ossmeter.metricprovider.trans.bugs.bugmetadata.model.BugTrackerData;
import org.ossmeter.metricprovider.trans.bugs.bugmetadata.model.BugsBugMetadataTransMetric;
import org.ossmeter.platform.AbstractHistoricalMetricProvider;
import org.ossmeter.platform.Date;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.MetricProviderContext;
import org.ossmeter.platform.communicationchannel.nntp.NntpUtil;
import org.ossmeter.repository.model.Project;

import com.googlecode.pongo.runtime.Pongo;

public class OpenTimeHistoricMetricProvider extends AbstractHistoricalMetricProvider {

	public final static String IDENTIFIER = "org.ossmeter.metricprovider.historic.bugs.opentime";

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
	    return !project.getBugTrackingSystems().isEmpty();	   
	}

	private static final long SECONDS_DAY = 24 * 60 * 60;

	@Override
	public Pongo measure(Project project) {
		BugsOpenTimeHistoricMetric avgBugOpenTime = new BugsOpenTimeHistoricMetric();
		if (uses.size() == 1) {
			BugsBugMetadataTransMetric usedBhm = ((BugMetadataTransMetricProvider)uses.get(0)).adapt(context.getProjectDB(project));
			long seconds = 0;
			int durations = 0;
			for (BugTrackerData bugTrackerData: usedBhm.getBugTrackerData()) {
				if (!bugTrackerData.getLastClosedTime().equals("null")) {
					java.util.Date javaOpenTime = NntpUtil.parseDate(bugTrackerData.getCreationTime());
					java.util.Date javaCloseTime = NntpUtil.parseDate(bugTrackerData.getLastClosedTime());
					seconds += ( Date.duration(javaOpenTime, javaCloseTime) / 1000);
					durations++;
				}
			}
			if (durations>0) {
				long avgDuration = seconds/durations;
				int days = (int) (avgDuration / SECONDS_DAY);
				long lessThanDay = (avgDuration % SECONDS_DAY);
				String formatted = DurationFormatUtils.formatDuration(lessThanDay*1000, "HH:mm:ss:SS");
				avgBugOpenTime.setAvgBugOpenTime(days+":"+formatted);
//				System.out.println(days + ":" + formatted);
				avgBugOpenTime.setBugsConsidered(durations);
			}

		}
		return avgBugOpenTime;
	}
			
	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}
	
	@Override
	public List<String> getIdentifiersOfUses() {
		return Arrays.asList(BugMetadataTransMetricProvider.class.getCanonicalName());
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.context = context;
	}

	@Override
	public String getShortIdentifier() {
		return "bugopentime";
	}

	@Override
	public String getFriendlyName() {
		return "Average Bug Duration";
	}

	@Override
	public String getSummaryInformation() {
		return "This metric computes the average time between creating and closing bugs. " +
				"Format: dd:HH:mm:ss:SS, where dd=days, HH:hours, mm=minutes, ss:seconds, SS=milliseconds.";
	}
}