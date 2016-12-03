package Parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import thaw.bots.gitbot.Gitbot;

public class Parser {
	private static boolean isGitBot(String s){
		return s.matches("git-bot");
	}
	private static boolean isGitHubBot(String s){
		return s.matches("github-bot");
	}
	
	public static JsonObject parse(String line) throws IOException {
		List<String> arguments = Arrays.asList(line.split(" "));
		for (int i=0; i<arguments.size(); i++){
			if(isGitBot(arguments.get(i))){
				List<String> params = Arrays.asList(line.split(" ")).stream().skip(1).collect(Collectors.toList());
				params.stream().forEach(System.out::println);
				requireArgumentsGit(params);
				return Gitbot.getUsersRepos(params.get(0));
			} else if(isGitHubBot(arguments.get(i))){
				List<String> params = Arrays.asList(line.split(" ")).stream().skip(1).collect(Collectors.toList());
				requireArgumentsGitHub(params);
				return Gitbot.commitsRepo(params.get(0), params.get(1));
			}
		}
		throw new IllegalArgumentException("Not a bot call");
	}
	
	private static void requireArgumentsGit(List<String> list){
		if(list.size() != 1 ){
			throw new IllegalArgumentException("Invalid arguments");
		}
	}
	
	private static void requireArgumentsGitHub(List<String> list){
		if(list.size() != 2 ){
			throw new IllegalArgumentException("Invalid arguments");
		}
	}
}
