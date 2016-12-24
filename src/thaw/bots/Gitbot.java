
package thaw.bots;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.service.CommitService;

import io.vertx.core.json.JsonObject;

public class Gitbot implements Callable<JsonObject>{
	
	private final RepositoryId repoId;
	private final static int SIZE = 25;
	private final static String MSG_TEMPLATE = "{0} by {1} on {2}";
	private final static String NAME = "GitBot";
	private final Object lock = new Object();
	
	public Gitbot(String user, String repository){
		synchronized(lock){
			Objects.requireNonNull(user);
			Objects.requireNonNull(repository);
			repoId = new RepositoryId(user, repository);
		}
	}
	
	@Override
	public JsonObject call(){
		synchronized(lock){
			JsonObject json = new JsonObject();
			final CommitService service = new CommitService();
			StringBuilder str = new StringBuilder();
			
			for (Collection<RepositoryCommit> commits : service.pageCommits(repoId,SIZE)) {
				for (RepositoryCommit commit : commits) {
					String sha = commit.getSha().substring(0, 7);
					String author = commit.getCommit().getAuthor().getName();
					Date date = commit.getCommit().getAuthor().getDate();
					str.append(MessageFormat.format(MSG_TEMPLATE,sha, author, date).toString() + "\n");
				}
			}
			String content = Arrays.asList(str.toString().split("\n"))
					.stream()
					.map(x-> "<p>" + x + "</p> </br>")
					.collect(Collectors.joining(""));
			
			json.put("content", content);
			json.put("username", NAME);
			return json;
		}
	}

}