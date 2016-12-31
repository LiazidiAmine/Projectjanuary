package thaw.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonObject;
import thaw.chatroom.Message;

public class Parser {

	public static Message strToMessage(String msg){
		ObjectMapper mapper = new ObjectMapper();
		Message obj = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String date = dateFormat.format(new Date());
		try {
			obj = mapper.readValue((msg), Message.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		obj.setDate(date);

		return obj;
	}

	public static Message parseBotMsg(JsonObject json) {
		Objects.requireNonNull(json);
		ObjectMapper mapper = new ObjectMapper();
		Message msg_bot = null;
		JsonFactory factory = new JsonFactory();
		JsonParser parser = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String date = dateFormat.format(new Date());
		try {	
			parser = factory.createParser(json.toString());
			msg_bot = mapper.readValue(parser, Message.class);
			msg_bot.setDate(date);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return msg_bot;
	}
}
