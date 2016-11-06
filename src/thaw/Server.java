package thaw;

import static java.util.Objects.requireNonNull;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.rxjava.core.Future;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;

public class Server extends AbstractVerticle {
	private Connection connection = null;

  @Override
  public void start() throws Exception {

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
    router.post("/addCh").handler(arg0 -> {
		try {
			addChannel(arg0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	});

    // Create a router endpoint for the static content.
    router.route("/*").handler(StaticHandler.create("webroot"));

    // Start the web server and tell it to use the router to handle requests.
    vertx.createHttpServer().requestHandler(router::accept).listen(9997);

    EventBus eb = vertx.eventBus();

    // Register to listen for messages coming IN to the server
    eb.consumer("chat.to.server").handler(message -> {
      // Create a timestamp string
      String timestamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date.from(Instant.now()));
      System.out.println(message.body());
      // Send the message back out to all clients with the timestamp prepended.
      eb.publish("chat.to.client", timestamp + ": " + message.body());
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
    if (title.isEmpty()) {  
        response.setStatusCode(404).end();
        return;
    } 
    routingContext.response()
       .putHeader("content-type", "application/json")
       .end();
  }
  
  private void addChannel(RoutingContext routingContext) throws SQLException {  
	HttpServerResponse response = routingContext.response();
    HttpServerRequest request = routingContext.request();
    String title = requireNonNull(request.getParam("ch-title"));
    System.out.println(title);
    if (title.isEmpty()) {  
        response.setStatusCode(404).end();
        return;
    } else {
	    String query = "CREATE TABLE IF NOT EXISTS Channel" + title + "("
	    				+"_id INTEGER PRIMARY KEY, "
	    				+"Title VARCHAR(20),"
						+ "Username VARCHAR(20), "
						+ "Timestamp INTEGER);";
	    String insert = "INSERT INTO Channel" + title + " ("
	    		+ "Title, Username, Timestamp) "
	    		+ "VALUES ("+title+", Amine,"+Date.from(Instant.now())+");";
	    setQueryUpdate(query);
	    setQueryUpdate(insert);
    }
    routingContext.response()
       .putHeader("content-type", "application/json")
       .end();  
  }
  
  private void getMessages(RoutingContext routingContext) throws SQLException {
	  HttpServerResponse response = routingContext.response();
	  HttpServerRequest request = routingContext.request();
	  String title = requireNonNull(request.getParam("ch-title"));
	  System.out.println("get message from "+title);
	  
  }
  
  private void saveMessage(String msg, String channel, String timestamp, String user){
	  
  }
  
  private void setQueryUpdate(String query) throws SQLException {
	  // create database connection
	  try(Connection connection = DriverManager.getConnection("jdbc:sqlite:thaw.db")){
		  Statement statement = connection.createStatement();
		  statement.setQueryTimeout(30);
		  statement.executeUpdate(query);
	  }
  }
  
	@Override
	public void stop() throws Exception {
		// Close the database connection.
		connection.close();
	}
}
