package Parser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import thaw.bots.gitbot.Gitbot;

public class Parser {
	private static boolean isGitHubBot(String s){
		return s.matches("gitbot");
	}
	
	public static JsonObject parse(String line) throws IOException {
		List<String> arguments = Arrays.asList(line.split(" "));
		if(isGitHubBot(arguments.get(0))){
			List<String> params = Arrays.asList(line.split(" ")).stream().skip(1).collect(Collectors.toList());
			requireArgumentsGitHub(params);
			return Gitbot.commitsRepo(params.get(0), params.get(1));
		}
		throw new IllegalArgumentException("Not a bot call");
	}
	
	public static boolean isBot(String line) {
		List<String> arguments = Arrays.asList(line.split(" "));
		return isGitHubBot(arguments.get(0));
	}
	
	private static void requireArgumentsGitHub(List<String> list){
		if(list.size() != 2 ){
			throw new IllegalArgumentException("Invalid arguments");
		}
	}
}
