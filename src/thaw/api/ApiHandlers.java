package thaw.api;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import thaw.chatroom.Message;
import thaw.chatroom.User;
import thaw.utils.Hash;

public class ApiHandlers {
	
	public static final String PATH_SYSTEM_DIRECTORY = System.getProperty("user.dir");
	private final DataBase db;
	
	/**
	 * ApiHandlers constructor
	 * @param DataBase
	 */
	public ApiHandlers(DataBase db){
		this.db = db;
	}
	
	/**
	 * retrieve user informations from login form and check validity
	 * if valid the user is redirect to the home page
	 * else unathorized message is displayed
	 * if the user doesnt exist in database, new user is created
	 * 
	 * @param RoutingContext
	 */
	public void login(RoutingContext ctx) {
		final String username = Objects.requireNonNull(ctx.request().getParam("uname"));
		final String psw = Objects.requireNonNull(ctx.request().getParam("psw"));
		isRegistred(username, psw);
		if(validUser(new User(username, psw))){
			ctx.addCookie(Cookie.cookie("user", username));
			ctx.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
		}else{
			ctx.response().putHeader("Content-Type", "text/plain").setStatusCode(401).end("Unauthorized");
		}
	}

	/**
	 * user registration function
	 * username and hashed password are inserted in the database
	 * 
	 * @param String username
	 * @param String password
	 */
	private void register(String username, String psw) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(psw);
		final String hash = Hash.toSHA256(psw);
		final String insert = "INSERT INTO Users (Username, Password) VALUES ('" + username + "', '"+ hash + "');";
		db.setQueryUpdate(User.CREATE_TABLE_USERS);
		db.setQueryUpdate(insert);
	}

	/**
	 * check if user is already registred
	 * or if his username already used
	 * 
	 * @param String username
	 * @param String psw
	 * @return JsonArray resulting from database
	 */
	private void isRegistred(String username, String psw) {
		Objects.requireNonNull(username);
		final String query = "SELECT _id FROM Users WHERE Username = '" + username + "';";
		JsonArray result = db.execQuery(query);
		if(result.isEmpty()){
			register(username, psw);
		}
	}
	
	/**
	 * check if user is registred
	 * and return true or false
	 * 
	 * @param User user
	 * @return boolean
	 */
	private boolean validUser(User user){
		final String hash = Hash.toSHA256(user.getPassword());
		final String query = "SELECT * FROM Users WHERE Username = '" + user.getUsername() +"';";
		final JsonObject result = db.execQuery(query).getJsonObject(0);
		return result.getString("username").equals(user.getUsername()) && result.getString("password").equals(hash);
	}

	/**
	 * retrieve channel title and delete the channel from database
	 * @param RoutingContext 
	 */
	public void deleteChannel(RoutingContext routingContext) {
		final String title = "Chan_"+requireNonNull(routingContext.request().getParam("title"));
		final String query = "DROP TABLE IF EXISTS " + title + ";";
		db.setQueryUpdate(query);
		routingContext.response().putHeader("content-type", "application/json").end();
			
	}

	/**
	 * retrieve channel title and insert it to the database
	 * @param RoutingContext
	 */
	public void addChannel(RoutingContext routingContext) {
		final String title = requireNonNull(routingContext.request().getParam("ch-title"));
		final String query = "CREATE TABLE IF NOT EXISTS Chan_" + title + "(" + "_id INTEGER PRIMARY KEY, "
				+ "Content VARCHAR(20)," + "Username VARCHAR(20), " + "Time Date);";
		if (!title.isEmpty()) {
			db.setQueryUpdate(query);
			routingContext.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
			return;
		}
		routingContext.response().end();
	}

	/**
	 * insert user message in database
	 * 
	 * @param Message object
	 */
	public void postMsg(Message msg) {
		Objects.requireNonNull(msg);
		if (!msg.getContent().isEmpty()) {
			final String query_insert = "INSERT INTO " + "Chan_"+ msg.getChannel() + "(" + "Content, Username, Time) VALUES (" + "'"
					+ msg.getContent() + "'" + ", " + "'" + msg.getUsername() + "'" + ", " + "'" + msg.getDate() + "');";
			db.setQueryUpdate(query_insert);
			return;
		}
		throw new IllegalArgumentException("Invalid message "+msg.toString());
	}

	/**
	 * check if channel exist then
	 * retrieve channel title then 
	 * retrieve his 20 last messages from database
	 * else the request is finished
	 * 
	 * @param RoutingContext
	 */
	public void getMessages(RoutingContext ctx) {
		final String channel = "Chan_" + ctx.request().getParam("channel");
		final String query = "SELECT Content, Time, Username FROM " + channel + " LIMIT 20;";
		if(isChannelExist(channel)){
			final String response = db.execQuery(query).toString();
			ctx.response().putHeader("content-type", "application/json; charset=utf-8").end(response);
		}else{
			ctx.response().end();
		}
	}

	/**
	 * retrieve all channels from database
	 * then send it with the response
	 * 
	 * @param RoutingContext
	 */
	public void getChannels(RoutingContext routingContext) {
		final String query = "SELECT NAME FROM sqlite_master WHERE type=\"table\" AND name LIKE \"Chan%\";";
		final String response = db.execQuery(query).toString();
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(response);
	}
	
	/**
	 * logout the user
	 * if user is registred, his cookie is cleared
	 * then the user is redirected to login page
	 * 
	 * @param RoutingContext
	 */
	public void logout(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		if(ck != null){
			ck.setMaxAge(0);
		}
		ctx.response().putHeader("location", "http://localhost:9997/login.html").end();
	}
	
	/**
	 * retrieve user name from his cookie and put it in the response
	 * 
	 * @param RoutingContext
	 */
	public void getUser(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		String username = "";
		if(ck != null){
			username = ck.getValue();
		}
		ctx.response().putHeader("content-type", "application/json; charset=utf-8").end(Json.encodePrettily(username));
	}
	
	/**
	 * check if channel exist
	 * 
	 * @param String channel title
	 * @return boolean
	 */
	private boolean isChannelExist(String title){
		String query = "SELECT NAME FROM sqlite_master WHERE type=\"table\" AND name ='"+title+"';";
		return db.execQuery(query).size() > 0;
	}

}
