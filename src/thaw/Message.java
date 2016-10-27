package thaw;

import io.vertx.core.json.JsonObject;

public class Message {
	
	private final int id;
	private final User user;
	private final String content;
	private final long timestamp;
	private final Channel channel;
	
	public Message(int id, User user, String content, long timestamp, Channel channel){
		this.id = id;
		this.user = user;
		this.content = content;
		this.timestamp = timestamp;
		this.channel = channel;
	}
	
	public Message(JsonObject json){
		this.id = json.getInteger("id");
		this.user = new User(json.getJsonObject("user"));
		this.content = json.getString("content");
		this.timestamp = json.getLong("timestamp");
		this.channel = new Channel(json.getJsonObject("channel"));
	}
	
	public int getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getContent() {
		return content;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject()
				.put("id", id)
				.put("user", user.toJson())
				.put("content", content)
				.put("timestamp", timestamp)
				.put("channel", channel.toJson());
		return json;
	}

}
