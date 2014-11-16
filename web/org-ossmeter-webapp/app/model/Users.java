package model;

import com.googlecode.pongo.runtime.*;
import com.mongodb.*;

public class Users extends PongoDB {
	
	public Users() {}
	
	public Users(DB db) {
		setDb(db);
	}
	
	protected UserCollection users = null;
	protected TokenCollection tokens = null;
	protected ProjectCollection projects = null;
	protected StatisticsCollection statistics = null;
	protected TagCollection tags = null;
	
	
	
	public UserCollection getUsers() {
		return users;
	}
	
	public TokenCollection getTokens() {
		return tokens;
	}
	
	public ProjectCollection getProjects() {
		return projects;
	}
	
	public StatisticsCollection getStatistics() {
		return statistics;
	}
	
	public TagCollection getTags() {
		return tags;
	}
	
	
	@Override
	public void setDb(DB db) {
		super.setDb(db);
		users = new UserCollection(db.getCollection("users"));
		pongoCollections.add(users);
		tokens = new TokenCollection(db.getCollection("tokens"));
		pongoCollections.add(tokens);
		projects = new ProjectCollection(db.getCollection("projects"));
		pongoCollections.add(projects);
		statistics = new StatisticsCollection(db.getCollection("statistics"));
		pongoCollections.add(statistics);
		tags = new TagCollection(db.getCollection("tags"));
		pongoCollections.add(tags);
	}
}