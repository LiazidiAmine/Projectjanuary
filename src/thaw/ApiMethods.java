package thaw;

import static java.util.Objects.requireNonNull;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.RoutingContext;

public class ApiMethods {
	
	private static ApiMethods api;
	private static DataBase db;
	
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
		final String username = routingContext.request().getParam("uname");
		final String psw = routingContext.request().getParam("psw");
		final User user = new User(username, psw);
		//TODO parse user
		if(isRegistred(user).isEmpty()){
			register(user);
		}
		//check email and password validity
		routingContext.addCookie(Cookie.cookie("user", username));
		routingContext.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
		
	}

	private void register(User user) {
		
		if (isRegistred(user).isEmpty()) {
			String create_table = "CREATE TABLE IF NOT EXISTS Users " + "( _id INTEGER PRIMARY KEY, "
					+ "Username TEXT NOT NULL, " + "Password TEXT NOT NULL);";
			String insert = "INSERT INTO Users (Username, Password, Time) VALUES ('" + user.getUsername() + "', '"
					+ user.getPassword() + "');";
			db.setQueryUpdate(create_table);
			db.setQueryUpdate(insert);
		}
	}

	private JsonArray isRegistred(User user) {
		String sql = "SELECT _id FROM Users WHERE Username = '" + user.getUsername() + "';";
		return db.execQuery(sql);
	}

	public void deleteChannel(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		String title = requireNonNull(request.getParam("title"));
		System.out.println(title);
		String query = "drop table if exists " + title + ";";
		if (title.isEmpty()) {
			response.setStatusCode(404).end();
			return;
		} else {
			db.setQueryUpdate(query);
		}
		routingContext.response().putHeader("content-type", "application/json").end();
	}

	public void addChannel(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		String title = requireNonNull(request.getParam("ch-title"));
		System.out.println(title);
		if (title.isEmpty()) {
			response.setStatusCode(404).end();
			return;
		} else {
			String query = "CREATE TABLE IF NOT EXISTS Chan_" + title + "(" + "_id INTEGER PRIMARY KEY, "
					+ "Content VARCHAR(20)," + "Username VARCHAR(20), " + "Time Date);";
			System.out.println("channel added");
			db.setQueryUpdate(query);
		}
		routingContext.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
	}

	public void postMsg(Message msg) {
		if (msg != null && !msg.getContent().isEmpty()) {
			String query_insert = "INSERT INTO " + msg.getChannel() + "(" + "Content, Username, Time) VALUES (" + "'"
					+ msg.getContent() + "'" + ", " + "'" + msg.getUsername() + "'" + ", " + "'" + msg.getDate() + "'"
					+ ");";
			System.out.println(query_insert);
			db.setQueryUpdate(query_insert);
		}
	}

	public void getMessages(RoutingContext routingContext) {
		String channel = routingContext.request().getParam("channel");
		String query = "SELECT Content, Time, Username FROM " + channel;
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(db.execQuery(query).toString());
	}

	public void getChannels(RoutingContext routingContext) {
		String query = "select name from sqlite_master where type=\"table\" and name LIKE \"Chan%\";";
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(db.execQuery(query).toString());
	}
	
	public void getUser(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		String username = ck.getValue();
		String sql = "select from Users where Username = '"+username+"'";
		String response = db.execQuery(sql).toString();
		ctx.response().putHeader("content-type", "application/json; charset=utf-8").end(response);
	}
	
	public void logout(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		ck.setMaxAge(0);
		String path = System.getProperty("user.dir");
		ctx.response().putHeader("location", "http://localhost:9997/login.html").sendFile(path+"/public/login.html").end();
		
	}

}
