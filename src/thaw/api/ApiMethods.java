package thaw.api;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;
import thaw.chatroom.Message;
import thaw.chatroom.User;

public class ApiMethods {
	
	private static ApiMethods api;
	public static final String PATH_SYSTEM_DIRECTORY = System.getProperty("user.dir");
	private final DataBase db;
	
	private ApiMethods(DataBase db){
		this.db = db;
	}
	
	public static ApiMethods getInstance(DataBase db){
		if(api == null){
			api = new ApiMethods(db);
		}
		return api;
	}
	
	public void login(RoutingContext routingContext) {
		final String username = Objects.requireNonNull(routingContext.request().getParam("uname"));
		final String psw = Objects.requireNonNull(routingContext.request().getParam("psw"));
		if(isRegistred(username).isEmpty()){
			register(username, psw);
		}
		//check password validity
		routingContext.addCookie(Cookie.cookie("user", username));
		routingContext.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
	}

	private void register(String username, String psw) {
		Objects.requireNonNull(username);
		Objects.requireNonNull(psw);//TODO HASH PASSWORD
		final String insert = "INSERT INTO Users (Username, Password, Time) VALUES ('" + username + "', '"
				+ psw + "');";
		db.setQueryUpdate(User.CREATE_TABLE_USERS);
		db.setQueryUpdate(insert);
	}

	private JsonArray isRegistred(String username) {
		Objects.requireNonNull(username);
		final String query = "SELECT _id FROM Users WHERE Username = '" + username + "';";
		return db.execQuery(query);
	}
	/*
	private boolean validUser(User user){
		String query = "SELECT * FROM Users WHERE Username = '" + user.getUsername() +"';";
		System.out.println(db.execQuery(query));//TODO
		return db.execQuery(query).contains(user.getPassword());
	}*/

	public void deleteChannel(RoutingContext routingContext) {
		final String title = requireNonNull(routingContext.request().getParam("title"));
		final String query = "DROP TABLE IF EXISTS " + title + ";";
		if (!title.isEmpty()) {
			db.setQueryUpdate(query);
			routingContext.response().putHeader("content-type", "application/json").end();
			return;
		} 
		routingContext.response().end();
	}

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

	public void getMessages(RoutingContext routingContext) {
		final String channel = "Chan_" + routingContext.request().getParam("channel");
		final String query = "SELECT Content, Time, Username FROM " + channel + " LIMIT 20;";
		final String response = db.execQuery(query).toString();
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8").end(response);
	}

	public void getChannels(RoutingContext routingContext) {
		final String query = "SELECT NAME FROM sqlite_master WHERE type=\"table\" AND name LIKE \"Chan%\";";
		final String response = db.execQuery(query).toString();
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(response);
	}
	
	public void getUser(RoutingContext ctx){
		final Cookie ck = ctx.getCookie("user");
		final String username = ck.getValue();
		final String sql = "SELECT _id, Username FROM Users WHERE Username = '"+username+"'";
		final String response = db.execQuery(sql).toString();
		ctx.response().putHeader("content-type", "application/json; charset=utf-8").end(response);
	}
	
	public void logout(RoutingContext ctx){
		final Cookie ck = ctx.getCookie("user");
		ck.setMaxAge(0);
		ctx.response().putHeader("location", "http://localhost:9997/login.html")
			.sendFile(PATH_SYSTEM_DIRECTORY + "/public/login.html")
			.end();
	}

}
