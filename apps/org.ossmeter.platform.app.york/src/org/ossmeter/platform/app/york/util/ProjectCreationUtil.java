package org.ossmeter.platform.app.york.util;

import org.ossmeter.platform.Platform;
import org.ossmeter.repository.model.Bugzilla;
import org.ossmeter.repository.model.GitRepository;
import org.ossmeter.repository.model.NntpNewsGroup;
import org.ossmeter.repository.model.Project;
import org.ossmeter.repository.model.SvnRepository;
import org.ossmeter.repository.model.github.GitHubProject;
import org.ossmeter.repository.model.github.GitHubRepository;
import org.ossmeter.repository.model.github.GitHubUser;
import org.ossmeter.repository.model.sourceforge.SourceForgeProject;

/**
 * This class is purely for illustration purposes and is not intended for release.
 * @author jimmy
 *
 */
public class ProjectCreationUtil {

	public static Project createProjectWithNewsGroup(String name, String url, String newsGroupName, 
			Boolean authenticationRequired, String username, String password, int port, int interval){
		Project project = new Project();
		project.setName(name);
		
		NntpNewsGroup newsGroup = new NntpNewsGroup();
		newsGroup.setUrl(url+'/' + newsGroupName);
		newsGroup.setAuthenticationRequired(authenticationRequired);
		newsGroup.setUsername(username);
		newsGroup.setPassword(password);
		newsGroup.setPort(port);
		newsGroup.setInterval(interval);
		newsGroup.setLastArticleChecked("-1");
		project.getCommunicationChannels().add(newsGroup);
		
		return project;
	}
	
	public static Project createGitProject(String name, String url) {
		Project project = new Project();
		project.setName(name);
		
		GitRepository repo = new GitRepository();
		repo.setUrl(url);
		
		project.getVcsRepositories().add(repo);
		return project;
	}
	
	public static Project createProjectWithBugTrackingSystem(String name, String url, String product, String component){
		Project project = new Project();
		project.setName(name);
		
		Bugzilla bugzilla = new Bugzilla();
		bugzilla.setUrl(url);
		bugzilla.setProduct(product);
		if (component!=null) bugzilla.setComponent(component);
		project.getBugTrackingSystems().add(bugzilla);
		
		return project;
	}
	
	public static Project createSvnProject(String name, String url) {
		Project project = new Project();
		project.setName(name);
		SvnRepository svnRepository = new SvnRepository();
		svnRepository.setUrl(url);
		project.getVcsRepositories().add(svnRepository); 
		return project;
	}
	
	
	public static Project createGitHubProject(String login, String repository) {
		GitHubProject project = new GitHubProject();
		project.setName(login + "-" + repository);
		
		GitHubRepository gitHubRepository = new GitHubRepository();
		gitHubRepository.setName(repository);
		
		GitHubUser owner = new GitHubUser();
		owner.setLogin(login);
		gitHubRepository.setOwner(owner);
		
		project.getVcsRepositories().add(gitHubRepository);
		return project;
	}
	
	
	public static Project createSourceForgeProject(String name) {
		SourceForgeProject project = new SourceForgeProject();
		project.setName(name);
		/* TODO: Why was this code commented out?
		SvnRepository svnRepository = new SvnRepository();
		svnRepository.setUrl("http://" + name + ".svn.sourceforge.net/svnroot/" + name);
		project.getVcsRepositories().add(svnRepository); */
		return project;
	}
}
