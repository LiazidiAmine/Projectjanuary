package thaw.chatroom;

import io.vertx.core.json.JsonObject;

public class User {
	
	private final String username;
	private final String password;
	
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


}
