package thaw.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.groovy.ext.web.sstore.ClusteredSessionStore;
import thaw.bots.BotsHandler;
import thaw.chatroom.Message;
import thaw.parser.*;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends AbstractVerticle {
	
	private DataBase db = DataBase.getInstance();
	private ApiMethods api = ApiMethods.getInstance(db);
	private BotsHandler botsHandler = BotsHandler.getInstance();
	ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("deprecation")
	@Override
	public void start() throws Exception {
		Router router = Router.router(vertx);

		router.route().handler(CookieHandler.create());
		SessionStore store = LocalSessionStore.create(vertx);
		SessionHandler sessionHandler = SessionHandler.create(store);
		router.route().handler(sessionHandler
				.setCookieHttpOnlyFlag(true)
				.setCookieSecureFlag(true)
		);
				
		/*router.route().handler(ctx->{
			sessionHandler.handle(ctx);
		});*/
		router.route("/home.html").handler(ctx -> {
			Session session = ctx.session();
			String username = session.get("user") == null ? "" : session.get("user").toString();
			//Cookie ck = ctx.getCookie("user");
			if(username == null){
				ctx.response().putHeader("location", "http://localhost:9997/login.html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/login.html");
				return;
			} else{
				ctx.response().putHeader("content text", "text/html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/home.html");
			}
		});
		
		router.route("/").handler(ctx -> {
			Session session = ctx.session();
			String username = session.get("user") == null ? "" : session.get("user").toString();
			//Cookie ck = ctx.getCookie("user");
			if(username == null){
				ctx.response().putHeader("content text", "text/html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/login.html");
				return;
			} else{
				ctx.response().putHeader("content text", "text/html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/home.html");
			}
		});
		
		router.route("/login.html").handler(ctx -> {
			Session session = ctx.session();
			String username = session.get("user") == null ? "" : session.get("user").toString();
			//Cookie ck = ctx.getCookie("user");
			if(username == null || username == ""){
				ctx.response().putHeader("content text", "text/html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/login.html");
				return;
			} else{
				ctx.response().putHeader("content text", "text/html")
					.sendFile(ApiMethods.PATH_SYSTEM_DIRECTORY + "/public/home.html");
			}
		});
		
		router.route("/*").handler(BodyHandler.create());
		
		// Allow events for the designated addresses in/out of the event bus
		// bridge
		BridgeOptions opts = new BridgeOptions()
				.addInboundPermitted(new PermittedOptions().setAddress("chat.to.server"))
				.addOutboundPermitted(new PermittedOptions().setAddress("chat.to.client"));

		// Create the event bus bridge and add it to the router.
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx).bridge(opts);
		/*sockJSHandler.bridge(opts, be->{
			if(be.type() == BridgeEventType.RECEIVE){
				System.out.println("SESSION "+be.socket().webSession().id());
				JsonObject rawMessage = be.getRawMessage();
				rawMessage.put("headers",  be.socket().webSession().id());
				//be.setRawMessage(rawMessage);
			}
			
			//be.complete(true);
		});*/

		router.route("/eventbus").handler(sockJSHandler);
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
			Message msg = Parser.strToMessage(str_msg);
			String sessionId = message.headers().get("headers");
			System.out.println(sessionId);
			/*store.get(sessionId, res->{
				Session session = res.result();
				System.out.println(session.get("user").toString());
				//msg.setUsername(session.get("user"));
			});*/
			api.postMsg(msg);
			eb.publish("chat.to.client", msg.toJson());

			Message msg_bot = null;
			JsonObject json_response = null;
			if(botsHandler.isBotCall(msg.getContent())){
				json_response = botsHandler.botCall(msg.toJson().getString("content"));
				Objects.requireNonNull(json_response);
				msg_bot = Parser.parseBotMsg(json_response);
				if (msg_bot != null) {
					msg_bot.setChannel(msg.getChannel());
					api.postMsg(msg_bot);
					eb.publish("chat.to.client", msg_bot.toJson());
				}
			}
		});

	}

	@Override
	public void stop() throws Exception {
		// Close the database connection.
		db.close();
	}
	
}
