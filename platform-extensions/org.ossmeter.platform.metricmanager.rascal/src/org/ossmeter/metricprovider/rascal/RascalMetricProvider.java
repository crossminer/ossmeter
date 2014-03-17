package org.ossmeter.metricprovider.rascal;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.imp.pdb.facts.IInteger;
import org.eclipse.imp.pdb.facts.IMap;
import org.eclipse.imp.pdb.facts.IString;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.type.Type;
import org.ossmeter.metricprovider.rascal.RascalManager.ProjectRascalManager;
import org.ossmeter.metricprovider.rascal.trans.model.IntegerMeasurement;
import org.ossmeter.metricprovider.rascal.trans.model.RascalMetrics;
import org.ossmeter.platform.IMetricProvider;
import org.ossmeter.platform.ITransientMetricProvider;
import org.ossmeter.platform.MetricProviderContext;
import org.ossmeter.platform.delta.ProjectDelta;
import org.ossmeter.platform.delta.vcs.VcsChangeType;
import org.ossmeter.platform.delta.vcs.VcsCommit;
import org.ossmeter.platform.delta.vcs.VcsCommitItem;
import org.ossmeter.platform.delta.vcs.VcsProjectDelta;
import org.ossmeter.platform.delta.vcs.VcsRepositoryDelta;
import org.ossmeter.repository.model.Project;
import org.ossmeter.repository.model.VcsRepository;
import org.rascalmpl.interpreter.result.ICallableValue;

import com.mongodb.DB;

public class RascalMetricProvider implements ITransientMetricProvider<org.ossmeter.metricprovider.rascal.trans.model.RascalMetrics> {

  private final String description;
  private final String friendlyName;
  private final String shortMetricId;
  private final String metricId;
  private final ICallableValue function;
  private MetricProviderContext context;

  public RascalMetricProvider(String metricId, String shortMetricId, String friendlyName, String description, ICallableValue function) {
    this.metricId = metricId;
    this.shortMetricId =  shortMetricId;
    this.friendlyName = friendlyName;
    this.description = description;
    this.function = function;
  }
  
  @Override
  public String toString() {
    return getIdentifier();
  }
  
	@Override
	public String getIdentifier() {
		return metricId;
	}

	@Override
	public String getShortIdentifier() {
		return shortMetricId;
	}

	@Override
	public String getFriendlyName() {
		return friendlyName;
	}

	@Override
	public String getSummaryInformation() {
		return description;
	}

	@Override
	public boolean appliesTo(Project project) {
		return project.getVcsRepositories().size() > 0;
	}

	@Override
	public void setUses(List<IMetricProvider> uses) {
		
	}

	@Override
	public List<String> getIdentifiersOfUses() {
		return null;
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.context = context;
	}

	@Override
	public RascalMetrics adapt(DB db) {
		org.ossmeter.metricprovider.rascal.trans.model.RascalMetrics rm = new RascalMetrics(db, this.metricId);
		return rm;
	}

	@Override
	public void measure(Project project, ProjectDelta delta, RascalMetrics db) {
		Map<String, Map<String, IValue>> revisions = getM3s(project, delta);

		for (String rev: revisions.keySet()) {
			Map<String, IValue> fileM3s = revisions.get(rev);
		
			for (String fileURL: fileM3s.keySet()) {
				IValue m3 = fileM3s.get(fileURL);
				
				// TODO: generalize to any return type
				// TODO: handle exceptions gracefully
				// TODO: route warnings to the platform as well
        IMap result = (IMap) function.call(new Type[] { m3.getType() }, new IValue[] {m3}, null).getValue();
        
				for (Iterator<Entry<IValue, IValue>> it = result.entryIterator(); it.hasNext(); ) {
					Entry<IValue, IValue> currentEntry = (Entry<IValue, IValue>)it.next();
					
					// TODO: change to source locations
					String key = ((IString)currentEntry.getKey()).getValue();
					
					if (!key.isEmpty()) {
						IntegerMeasurement noaData = null;
						// for cc need to delete all methods for this file
						
						// TODO: dispatch on return type
						noaData = new IntegerMeasurement();
						noaData.setUri(key);
						noaData.setValue(((IInteger)currentEntry.getValue()).longValue());
						db.getMeasurements().add(noaData);
					}
				}
			}
			db.sync();
		}
	}
	
	private Map<String, Map<String, IValue>> getM3s(Project project, ProjectDelta delta) {
//  assert manager != null = throw new RuntimeException("manager isn't initializer");
  Map<String, Map<String, IValue>> m3sPerRevision = new TreeMap<>();
  Map<String, IValue> fileM3s = new HashMap<>();
  File localStorage = new File(project.getStorage().getPath());
  VcsProjectDelta vcsDelta = delta.getVcsDelta();
  ProjectRascalManager manager = RascalManager.getInstance().getInstance(project.getName());

  for (VcsRepositoryDelta vcsRepositoryDelta : vcsDelta.getRepoDeltas()) {
    VcsRepository vcsRepository = vcsRepositoryDelta.getRepository();
    List<VcsCommit> commits = vcsRepositoryDelta.getCommits();
    for (VcsCommit commit : commits) {
      manager.checkOutRevision(delta.getDate(), commit.getRevision(), vcsRepository.getUrl(), localStorage.getAbsolutePath());
      for (VcsCommitItem item : commit.getItems()) {
        if (item.getChangeType() == VcsChangeType.DELETED || item.getChangeType() == VcsChangeType.UNKNOWN) {
          // not handling deleted files or unknown
          continue;
        }
        IValue fileM3 = null;
        String repo = vcsRepository.getUrl();
        // FIXME: This should only be a temporary resolution.
        String path = RascalManager.makeRelative(repo, item.getPath());
        String localFile = localStorage.getAbsolutePath() + "/checkout/" + path;
        String fileURL = repo + (repo.endsWith("/") ? "" : "/") + path;
        try {
          fileM3 = manager.getModel(commit.getRevision(), fileURL, localFile);
        } catch (Exception e) {
          System.out.println(e.getMessage());
          System.err.println("Model could not be created for file " + localFile);
          System.err.println("Continuing with other files...");
          continue;
        }
        //fileM3 = manager.isValidModel(fileM3) ? fileM3 : manager.makeLocation(localFile); //everything is valid now
        fileM3s.put(fileURL, fileM3);
      }
      m3sPerRevision.put(commit.getRevision(), fileM3s);
    }
  }
  
  return m3sPerRevision;
}
	
	 private String getLastSegment(String repo) {
	    String[] segments = repo.split("/");
	    int last = segments.length - 1;
	    while(segments[last].isEmpty()) {
	      --last;
	    }
	    return segments[last];
	  }
}