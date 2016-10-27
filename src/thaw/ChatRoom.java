package thaw;

import java.util.HashMap;

public class ChatRoom {
	
	private HashMap<Integer, Channel> channelList;
	private HashMap<Integer, User> userList;
	
	public ChatRoom(){
		this.channelList = new HashMap<>();
		this.userList = new HashMap<>();
	}
	
	public void addUser(User user){
		userList.put(user.getId(), user);
	}
	
	public void addChannel(Channel channel){
		channelList.put(channel.getId(), channel);
	}
	
	public void removeUser(User user){
		userList.remove(user.getId());
	}
	
	public void removeChannel(Channel channel){
		channelList.remove(channel.getId());
	}

}
