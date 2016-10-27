package thaw;

import io.vertx.core.json.JsonObject;

public class User {
	
	private final int id;
	private final String username;
	private final String email;
	private final long timestamp;
	private boolean authenticate;
	
	public User(int id, String username, String email, long timestamp, boolean authenticate) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.timestamp = timestamp;
		this.authenticate = authenticate;
	}
	
	public User(JsonObject json){
		this.id = json.getInteger("id");
		this.username = json.getString("username");
		this.email = json.getString("email");
		this.timestamp = json.getLong("timestamp");
		this.authenticate = json.getBoolean("authenticate");
	}

	public int getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}
	
	public boolean isAuthenticate() {
		return authenticate;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject()
				.put("id", id)
				.put("username", username)
				.put("email", email)
				.put("timestamp", timestamp)
				.put("authenticate", authenticate);
		return json;
	}
	
	public void sendMessage(Channel channel, Message message) {
		channel.getMessages().put(message.getId(), message);
	}
	
	public void removeMessage(Channel channel, Message message) {
		channel.getMessages().remove(message.getId());
	}

}
