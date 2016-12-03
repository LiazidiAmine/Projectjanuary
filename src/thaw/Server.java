package thaw;

import static java.util.Objects.requireNonNull;

import java.io.IOException;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends AbstractVerticle {
	private Connection connection = null;
	ObjectMapper mapper = new ObjectMapper();

  @Override
  public void start() throws Exception, SQLException {

    Router router = Router.router(vertx);

    router.route("/*").handler(BodyHandler.create());

    // Allow events for the designated addresses in/out of the event bus bridge
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

    // Create a router endpoint for the static content.
    router.route("/*").handler(StaticHandler.create("webroot"));

    // Start the web server and tell it to use the router to handle requests.
    vertx.createHttpServer().requestHandler(router::accept).listen(9997);

    EventBus eb = vertx.eventBus();

    // Register to listen for messages coming IN to the server
    eb.consumer("chat.to.server").handler(message -> {
      Message obj = null;
      // Create a timestamp string
      SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-mm-dd");
      String date = dt1.format(new Date());
      try {
		obj = mapper.readValue(message.body().toString(), Message.class);
		obj.setDate(date);//TODO dateformat
		obj.setUsername("Amine");
		
		System.out.println(obj.toJson() + "Sended");
		postMsg(obj);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      // Send the message back out to all clients with the timestamp prepended.
      eb.publish("chat.to.client", obj.toJson());
    });

  }
  
  private void login(RoutingContext routingContext) {
	HttpServerResponse response = routingContext.response();
	String username = routingContext.request().getParam("uname");
    String psw = routingContext.request().getParam("psw");
    System.out.println(username);
    System.out.println(psw);
	if (username.isEmpty() || psw.isEmpty()) {  
        response.setStatusCode(404).end();
        return;
    } 
    response.putHeader("content-type", "application/json").end();
  }
  
  private void deleteChannel(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    HttpServerRequest request = routingContext.request();
    String title = requireNonNull(request.getParam("title"));
    System.out.println(title);
    String query = "drop table if exists "+title+";";
    if (title.isEmpty()) {  
        response.setStatusCode(404).end();
        return;
    } else {
    	setQueryUpdate(query);
    }
    routingContext.response()
       .putHeader("content-type", "application/json")
       .end();
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
	    String query = "CREATE TABLE IF NOT EXISTS Chan_" + title + "("
	    				+ "_id INTEGER PRIMARY KEY, "
	    				+ "Content VARCHAR(20),"
						+ "Username VARCHAR(20), "
						+ "Time Date);";
	    setQueryUpdate(query);
    }
    routingContext.response()
       .putHeader("content-type", "application/json")
       .end();  
  }
  
  private void postMsg(Message msg) {
	  if(msg != null && !msg.getContent().isEmpty()){
		  System.out.println(msg.getChannel());
		  String query_insert = "INSERT INTO "+msg.getChannel()+"("
		  		+ "Content, Username, Time) VALUES ("
		  		+"'"+ msg.getContent() +"'"+ ", " +"'"+msg.getUsername()+"'"+", "+"'"+msg.getDate()+"'"+");";
		  System.out.println(query_insert);
		  setQueryUpdate(query_insert);
	  }
  }
  
  private void getMessages(RoutingContext routingContext){
	  String channel = routingContext.request().getParam("channel");
	  String query =  "SELECT Content, Time, Username FROM "+channel;
	  routingContext.response()
	      .putHeader("content-type", "application/json; charset=utf-8")
	      .end(execQuery(query).toString());
  }

  private void getChannels(RoutingContext routingContext) {
	  String query = "select name from sqlite_master where type=\"table\" and name LIKE \"Chan%\";";
	  routingContext.response()
	  	.putHeader("content-type", "application/json; charset=utf-8")
	  	.end(execQuery(query).toString());
  }
  
  private void setQueryUpdate(String query) {
	  // create database connection
	  try(Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")){
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
	  try(Connection connection = DriverManager.getConnection("jdbc:sqlite:db.db")){
		  Statement statement = connection.createStatement();
		  statement.setQueryTimeout(30);
		  ResultSet rs = statement.executeQuery(query);
		  JsonArray jsonArray = new JsonArray();
		  while(rs.next()){
			  int rows = rs.getMetaData().getColumnCount();
			  JsonObject obj = new JsonObject();
			  for (int i = 0; i < rows; i++){
				  obj.put(rs.getMetaData().getColumnLabel(i+1).toLowerCase(), rs.getObject(i+1));
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
