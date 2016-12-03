
package thaw.bots.gitbot;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;

import io.vertx.core.json.JsonObject;

public class Gitbot
{
	public static void main(String[] args) throws IOException{
		System.out.println(Parser.Parser.parse("github-bot LiazidiAmine Java-basic-tilemap"));
	}
	
	public static JsonObject getUsersRepos(String user) throws IOException{
		JsonObject json = new JsonObject();
		final String format = "{0}- created on {1}";
		
		int count = 1;
		RepositoryService service = new RepositoryService();
		for (Repository repo : service.getRepositories(user)){
			json.put(String.valueOf(count), MessageFormat.format(format,
					repo.getName(), repo.getCreatedAt()).toString());
			count++;
		}
		return json;
	}
	
	public static JsonObject commitsRepo(String user, String repository){
		JsonObject json = new JsonObject();
		final int size = 25;
		final RepositoryId repo = new RepositoryId(user, repository);
		final String message = "{0} by {1} on {2}";
		final CommitService service = new CommitService();
		int count = 1;
		for (Collection<RepositoryCommit> commits : service.pageCommits(repo,
				size)) {
			for (RepositoryCommit commit : commits) {
				count ++;
				String sha = commit.getSha().substring(0, 7);
				String author = commit.getCommit().getAuthor().getName();
				Date date = commit.getCommit().getAuthor().getDate();
				json.put(String.valueOf(count), MessageFormat.format(message,sha, author, date).toString());
			}
		}
		return json;
	}
}