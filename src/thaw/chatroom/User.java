package thaw.chatroom;

import io.vertx.core.json.JsonObject;

public class User {
	
	private final String username;
	private final String password;
	public static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS Users " + "( _id INTEGER PRIMARY KEY, "
			+ "Username TEXT NOT NULL, " + "Password TEXT NOT NULL);";
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	public User(JsonObject json){
		this.username = json.getString("username");
		this.password = json.getString("password");
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public JsonObject toJson(){
		JsonObject json = new JsonObject()
				.put("username", username)
				.put("password", password);
		return json;
	}
	
	/*public void sendMessage(Channel channel, Message message) {
		channel.getMessages().put(message.getId(), message);
	}
	
	public void removeMessage(Channel channel, Message message) {
		channel.getMessages().remove(message.getId());
	}*/

}
