package thaw.chatroom;

import io.vertx.core.json.JsonObject;

public class Message {
	
	private String content;
	private String channel;
	private String date;
	private String username;
	
	public Message(String content, String channel, String timestamp, String username){
		this.content = content;
		this.channel = channel;
		this.date = timestamp;
		this.username = username;
	}
	
	public Message(){
		
	}

	public String getContent() {
		return content;
	}

	
	public String getChannel() {
		return channel;
	}
	
	
	
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public JsonObject toJson(){
		JsonObject json = new JsonObject()
				.put("username", username)
				.put("date", date)
				.put("content", content)
				.put("channel", channel);
		return json;
	}
	
  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer("Message{");
    sb.append(" date : '").append(date).append('\'');
    sb.append(", username : '").append(username).append('\'');
    sb.append(", content : '").append(content).append('\'');
    sb.append(", channel : ").append(channel);
    sb.append('}');
    return sb.toString();
  }

}
