package thaw.utils;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import thaw.chatroom.Message;

public class Parser {
	/*
	 * we use a date format to parse messages
	 */
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	/*
	 * we use Jackson ObjectMapper and JsonFactory to parse Json objects
	 */
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final JsonFactory factory = new JsonFactory();

	/*
	 * parse Message object from String to Message
	 * using Jackson ObjectMapper
	 * 
	 * @param String message
	 * @return Message object
	 */
	public static Message strToMessage(String msg) throws JsonParseException, JsonMappingException, IOException{
		Message obj = null;
		String date = dateFormat.format(new Date());
		obj = mapper.readValue((msg), Message.class);
		obj.setDate(date);
		return obj;
	}

	/*
	 * parse bot message from json to Message
	 * using Jackson ObjectMapper
	 * 
	 * @param JsonObject
	 * @return Message object
	 */
	public static Message parseBotMsg(JsonObject json) throws JsonParseException, IOException {
		Objects.requireNonNull(json);
		String date = dateFormat.format(new Date());
		JsonParser parser = factory.createParser(json.toString());
		Message msg_bot =  mapper.readValue(parser, Message.class);
		msg_bot.setDate(date);
		return msg_bot;
	}
	
	/*
	 * parse database ResultSet to jsonarray object
	 * 
	 * @param ResultSet object
	 * @return JsonArray
	 */
	public static JsonArray resultSetToJson(ResultSet rs) throws SQLException{
		JsonArray jsonArray = new JsonArray();
		while (rs.next()) {
			int rows = rs.getMetaData().getColumnCount();
			JsonObject obj = new JsonObject();
			for (int i = 0; i < rows; i++) {
				obj.put(rs.getMetaData().getColumnLabel(i + 1).toLowerCase(), rs.getObject(i + 1));
			}
			jsonArray.add(obj);
		}
		return jsonArray;
	}
}
