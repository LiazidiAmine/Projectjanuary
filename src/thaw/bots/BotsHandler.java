package thaw.bots;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.syndication.io.FeedException;

import io.vertx.core.json.JsonObject;

public class BotsHandler {
	
	public static final String GIT_BOT = "git-bot";
	public static final String RSS_BOT = "rss-bot";
	//private static final String GITHUB_BOT = "GITHUB_BOT";
	private static Map<String,Function<String,JsonObject>> map = new HashMap<>();
	private static BotsHandler instance;
	
	private BotsHandler(){
		init();
	}
	
	private void init(){
		map.put(GIT_BOT, (line)->{
			Objects.requireNonNull(line);
			List<String> params = Arrays.asList(line.split(" ")).stream().skip(1).collect(Collectors.toList());
			requireArgsGitBot(params);
			return gitBot(params.get(0), params.get(1));
		});
		map.put(RSS_BOT, (line)->{
			Objects.requireNonNull(line);
			List<String> params = Arrays.asList(line.split(" ")).stream().skip(1).collect(Collectors.toList());
			requireArgsRssBot(params);
			return rssBot(params.get(0));
		});
	}
	
	private static JsonObject gitBot(String user, String repository) {	
		Objects.requireNonNull(user);
		Objects.requireNonNull(repository);
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future<JsonObject> result = es.submit(new Gitbot(user, repository));
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static JsonObject rssBot(String url) {
		Objects.requireNonNull(url);
		ExecutorService es = Executors.newSingleThreadExecutor();
		Future<JsonObject> result = null;
		try {
			result = es.submit(new RssBot(url));
		} catch (IllegalArgumentException | IOException | FeedException e1) {
			e1.printStackTrace();
		}
		try {
			return result.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static void requireArgsGitBot(List<String> list){
		if(list.size() != 2 ){
			throw new IllegalArgumentException("Invalid arguments");
		}
	}
	
	private static void requireArgsRssBot(List<String> list){
		if(list.size() != 1 ){
			throw new IllegalArgumentException("Invalid arguments");
		}
	}
	
	public boolean isBotCall(String line) {
		Objects.requireNonNull(line);
		String botName = Arrays.asList(line.split(" ")).get(0);
		if(map.containsKey(botName)){
			return true;
		}
		return false;
	}
	
	public static BotsHandler getInstance(){
		if(instance == null){
			instance = new BotsHandler();
		}
		return instance;
	}
	
	public JsonObject botCall(String line){
		String bot_name = Arrays.asList(line.split(" ")).get(0);
		return map.get(bot_name).apply(line);
	}
	
}
