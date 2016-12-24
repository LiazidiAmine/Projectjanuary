package Parser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonObject;
import thaw.Message;
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
	
	public static Message strToMessage(String msg){
		ObjectMapper mapper = new ObjectMapper();
		Message obj = null;
		SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-mm-dd");
		String date = dt1.format(new Date());
		try {
			obj = mapper.readValue((msg), Message.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		obj.setDate(date);
		
		return obj;
	}
	
	//TODO bothandler
	public static Message parseBotMsg(Message msg){
		ObjectMapper mapper = new ObjectMapper();
		Message msg_bot = null;
		JsonObject json = null;
		if (isBot(msg.toJson().getString("content"))) {
			try {
				json = parse(msg.toJson().getString("content"));
				JsonFactory factory = new JsonFactory();
				JsonParser parser = null;
				parser = factory.createParser(json.toString());
				msg_bot = mapper.readValue(parser, Message.class);
				SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-mm-dd");
				String date = dt1.format(new Date());
				msg_bot.setDate(date);
				msg_bot.setChannel(msg.toJson().getValue("channel").toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}
		return msg_bot;
	}
}
