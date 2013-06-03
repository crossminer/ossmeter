package org.ossmeter.metricprovider.generic.overalldailynumberofnewbugzillabugs;

import java.util.Arrays;
import java.util.List;

import org.ossmeter.metricprovider.generic.overalldailynumberofnewbugzillabugs.model.DailyBugzillaData;
import org.ossmeter.metricprovider.generic.overalldailynumberofnewbugzillabugs.model.DailyNonbb;
import org.ossmeter.metricprovider.numberofnewbugzillabugs.NonbbMetricProvider;
import org.ossmeter.metricprovider.numberofnewbugzillabugs.model.BugzillaData;
import org.ossmeter.metricprovider.numberofnewbugzillabugs.model.Nonbb;
import org.ossmeter.platform.IHistoricalMetricProvider;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.MetricProviderContext;
import org.ossmeter.repository.model.Project;

import com.googlecode.pongo.runtime.Pongo;

public class OverallDailyNumberOfNewBugzillaBugsProvider implements IHistoricalMetricProvider{

	public final static String IDENTIFIER = 
			"org.ossmeter.metricprovider.generic.overalldailynumberofnewbugzillabugs";

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
		return true; // FIXME: This should really check whether there are any providers
					 // for this MP. Otherwise it'll create an empty DB for every project.
					 // This is not possible in the current implementation because the 'uses'
					 // property is set AFTER this method is called.
	}

	@Override
	public Pongo measure(Project project) {
		DailyNonbb dailyNonbb = new DailyNonbb();
		for (IMetricProvider used : uses) {
			 Nonbb usedNonbb = ((NonbbMetricProvider)used).adapt(context.getProjectDB(project));
			 int numberOfNewBugzillaBugs = 0;
			 for (BugzillaData bugzilla: usedNonbb.getBugzillas()) {
				 numberOfNewBugzillaBugs += bugzilla.getNumberOfBugs();
			 }
			 DailyBugzillaData dailyBugzillaData = new DailyBugzillaData();
			 dailyBugzillaData.setNumberOfBugs(numberOfNewBugzillaBugs);
			 dailyNonbb.getBugzillas().add(dailyBugzillaData);
		}
		return dailyNonbb;
	}
			
	@Override
	public void setUses(List<IMetricProvider> uses) {
		this.uses = uses;
	}
	
	@Override
	public List<String> getIdentifiersOfUses() {
		return Arrays.asList(NonbbMetricProvider.class.getSimpleName());
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.context = context;
	}
}