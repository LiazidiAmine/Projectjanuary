package thaw;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import io.netty.handler.codec.http.ServerCookieEncoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends AbstractVerticle {
	
	private Connection connection = null;
	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void start() throws Exception, SQLException {
		Router router = Router.router(vertx);

		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		
		router.route("/home.html").handler(ctx -> {
			Cookie ck = ctx.getCookie("user");
			String path = System.getProperty("user.dir");
			if(ck == null || ck.getValue() == null){
				ctx.response().putHeader("location", "http://localhost:9997/login.html").sendFile(path+"/public/login.html");
				
			} else{
				String user = ck.getValue();
				System.out.println(user);
				ctx.response().putHeader("content text", "text/html").sendFile(path+"/public/home.html");
			}
			
		});
		
		router.route("/").handler(ctx -> {
			Cookie ck = ctx.getCookie("user");
			String path = System.getProperty("user.dir");
			if(ck == null || ck.getValue() == null){
				ctx.response().putHeader("content text", "text/html").sendFile(path+"/public/login.html");
				
			} else{
				String user = ck.getValue();
				System.out.println(user);
				ctx.response().putHeader("content text", "text/html").sendFile(path+"/public/home.html");
			}
			
		});
		
		router.route("/login.html").handler(ctx -> {
			Cookie ck = ctx.getCookie("user");
			String path = System.getProperty("user.dir");
			if(ck == null || ck.getValue() == null || ck.getValue() == ""){
				System.out.println("Redirection to login");
				ctx.response().putHeader("content text", "text/html").sendFile(path+"/public/login.html");
			} else{
				String user = ck.getValue();
				System.out.println(user + "ALREADY LOGIN");
				ctx.response().putHeader("content text", "text/html").sendFile(path+"/public/home.html");
			}
			
		});
		
		router.route("/*").handler(BodyHandler.create());
		
		// Allow events for the designated addresses in/out of the event bus
		// bridge
		BridgeOptions opts = new BridgeOptions()
				.addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
				.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));

		// Create the event bus bridge and add it to the router.
		SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);

		router.route("/eventbus/*").handler(ebHandler);
		router.post("/login").handler(this::login);
		router.post("/deleteCh").handler(this::deleteChannel);
		router.get("/channels").handler(this::getChannels);
		router.get("/messages/:channel").handler(this::getMessages);
		router.post("/addCh").handler(this::addChannel);
		router.get("/getUser").handler(this::getUser);
		router.get("/logout").handler(this::logout);

		// Create a router endpoint for the static content.
		router.route("/*").handler(StaticHandler.create("public"));
				
		// Start the web server and tell it to use the router to handle
		// requests.
		vertx.createHttpServer().requestHandler(router::accept).listen(9997);

		EventBus eb = vertx.eventBus();

		// Register to listen for messages coming IN to the server
		eb.consumer("chat.to.server").handler(message -> {
			String str_msg = message.body().toString();
			Message msg = Parser.Parser.strToMessage(str_msg);
			System.out.println(msg.toJson() + " Sended");
			postMsg(msg);
			eb.publish("chat.to.client", msg.toJson());

			Message msg_bot = Parser.Parser.parseBotMsg(msg);
			if (msg_bot != null) {
				System.out.println("publish msg_bot");
				postMsg(msg_bot);
				eb.publish("chat.to.client", msg_bot.toJson());
			}
		});

	}

	private void login(RoutingContext routingContext) {
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
			setQueryUpdate(create_table);
			setQueryUpdate(insert);
		}
	}

	private JsonArray isRegistred(User user) {
		String sql = "SELECT _id FROM Users WHERE Username = '" + user.getUsername() + "';";
		return execQuery(sql);
	}

	private void deleteChannel(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		String title = requireNonNull(request.getParam("title"));
		System.out.println(title);
		String query = "drop table if exists " + title + ";";
		if (title.isEmpty()) {
			response.setStatusCode(404).end();
			return;
		} else {
			setQueryUpdate(query);
		}
		routingContext.response().putHeader("content-type", "application/json").end();
	}

	private void addChannel(RoutingContext routingContext) {
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
			setQueryUpdate(query);
		}
		routingContext.response().putHeader("location", "http://localhost:9997/home.html").setStatusCode(302).end();
	}

	private void postMsg(Message msg) {
		if (msg != null && !msg.getContent().isEmpty()) {
			String query_insert = "INSERT INTO " + msg.getChannel() + "(" + "Content, Username, Time) VALUES (" + "'"
					+ msg.getContent() + "'" + ", " + "'" + msg.getUsername() + "'" + ", " + "'" + msg.getDate() + "'"
					+ ");";
			System.out.println(query_insert);
			setQueryUpdate(query_insert);
		}
	}

	private void getMessages(RoutingContext routingContext) {
		String channel = routingContext.request().getParam("channel");
		String query = "SELECT Content, Time, Username FROM " + channel;
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(execQuery(query).toString());
	}

	private void getChannels(RoutingContext routingContext) {
		String query = "select name from sqlite_master where type=\"table\" and name LIKE \"Chan%\";";
		routingContext.response().putHeader("content-type", "application/json; charset=utf-8")
				.end(execQuery(query).toString());
	}
	
	private void getUser(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		String username = ck.getValue();
		String sql = "select from Users where Username = '"+username+"'";
		ctx.response().putHeader("content-type", "application/json; charset=utf-8").end(execQuery(sql).toString());
	}
	
	private void logout(RoutingContext ctx){
		Cookie ck = ctx.getCookie("user");
		ck.setMaxAge(0);
		String path = System.getProperty("user.dir");
		ctx.response().putHeader("location", "http://localhost:9997/login.html").sendFile(path+"/public/login.html").end();
		
	}

	private void setQueryUpdate(String query) {
		// create database connection
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			statement.executeUpdate(query);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JsonArray execQuery(String query) {
		// create database connection
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")) {
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30);
			ResultSet rs = statement.executeQuery(query);
			JsonArray jsonArray = new JsonArray();
			while (rs.next()) {
				int rows = rs.getMetaData().getColumnCount();
				JsonObject obj = new JsonObject();
				for (int i = 0; i < rows; i++) {
					obj.put(rs.getMetaData().getColumnLabel(i + 1).toLowerCase(), rs.getObject(i + 1));
				}
				jsonArray.add(obj);
			}
			statement.close();
			return jsonArray;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new JsonArray();
		}
	}

	@Override
	public void stop() throws Exception {
		// Close the database connection.
		connection.close();
	}
}
