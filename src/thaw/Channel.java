package thaw;

import java.util.HashMap;

import io.vertx.core.json.JsonObject;

public class Channel {
	
	private final int id;
	private final String title;
	private final long timestamp;
	private final User owner;
	private HashMap<Integer, Message> messages;
	
	public Channel(int id, String title, long timestamp, User owner) {
		this.id = id;
		this.title = title;
		this.timestamp = timestamp;
		this.owner = owner;
		this.messages = new HashMap<>();
	}
	
	public Channel(JsonObject json){
		this.id = json.getInteger("channel_id");
		this.title = json.getString("title");
		this.timestamp = json.getLong("timestamp");
		this.owner = new User(json.getJsonObject("owner"));
		this.messages = new HashMap<>();
	}

	public int getId() {
		return id;
	}
	
	public String getTitle() {
		return title;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public User getOwner() {
		return owner;
	}
	
	public HashMap<Integer, Message> getMessages() {
		return messages;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject()
				.put("id", id)
				.put("title", title)
				.put("timestamp", timestamp)
				.put("owner", owner.toJson());
		return json;
	}
}
