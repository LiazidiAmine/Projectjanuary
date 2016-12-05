
package thaw.bots.gitbot;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;

import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import io.vertx.core.json.JsonObject;

public class Gitbot {
	
	public static JsonObject commitsRepo(String user, String repository){
		JsonObject json = new JsonObject();
		final int size = 25;
		final RepositoryId repo = new RepositoryId(user, repository);
		final String message = "{0} by {1} on {2}";
		final CommitService service = new CommitService();
		StringBuilder str = new StringBuilder();
		for (Collection<RepositoryCommit> commits : service.pageCommits(repo,size)) {
			for (RepositoryCommit commit : commits) {
				String sha = commit.getSha().substring(0, 7);
				String author = commit.getCommit().getAuthor().getName();
				Date date = commit.getCommit().getAuthor().getDate();
				System.out.println(MessageFormat.format(message,sha, author, date).toString());
				str.append(MessageFormat.format(message,sha, author, date).toString() + "\n");
			}
		}
		System.out.println(str.toString());
		json.put("content", str.toString());
		json.put("username", "Gitbot");
		return json;
		
	}
}