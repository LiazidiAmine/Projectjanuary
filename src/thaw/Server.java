package thaw;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends AbstractVerticle {
	
	private DataBase db = DataBase.getInstance();
	private ApiMethods api = ApiMethods.getInstance(db);
	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void start() throws Exception {
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
		router.post("/login").handler(api::login);
		router.post("/deleteCh").handler(api::deleteChannel);
		router.get("/channels").handler(api::getChannels);
		router.get("/messages/:channel").handler(api::getMessages);
		router.post("/addCh").handler(api::addChannel);
		router.get("/getUser").handler(api::getUser);
		router.get("/logout").handler(api::logout);

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
			api.postMsg(msg);
			eb.publish("chat.to.client", msg.toJson());

			Message msg_bot = Parser.Parser.parseBotMsg(msg);
			if (msg_bot != null) {
				System.out.println("publish msg_bot");
				api.postMsg(msg_bot);
				eb.publish("chat.to.client", msg_bot.toJson());
			}
		});

	}

	@Override
	public void stop() throws Exception {
		// Close the database connection.
		db.close();
	}
}
