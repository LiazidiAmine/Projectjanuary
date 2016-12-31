package thaw.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
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
import thaw.bots.BotsHandler;
import thaw.chatroom.Message;
import thaw.utils.*;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.net.JksOptions;

import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Server extends AbstractVerticle {
	
	/*
	 * Create our unique DataBase instance
	 */
	private DataBase db = DataBase.getInstance();
	private ApiHandlers api = ApiHandlers.getInstance(db);
	ObjectMapper mapper = new ObjectMapper();

	@Override
	public void start(Future<Void> fut) throws Exception {
		BotsHandler.init();
		Router router = Router.router(vertx);
		
		/*
		 * Secure limit of uploads
		 */
		router.route().handler(BodyHandler.create());
		/*
		 * We need to handle sessions
		 */
		router.route().handler(CookieHandler.create());
		router.route().handler(SessionHandler
				.create(LocalSessionStore.create(vertx))
				.setSessionCookieName("thaw-vertx")
				.setCookieHttpOnlyFlag(true)
				.setCookieSecureFlag(true)
			);
		
		router.route("/home.html").handler(ctx -> {
			final Cookie ck = ctx.getCookie("user");
			if(ck != null){
				ctx.response().setChunked(true).putHeader("Content-Type", "text/html; charset=UTF-8")
				  .putHeader("Cache-Control", "no-store, no-cache")
		          .putHeader("X-Content-Type-Options", "nosniff")
		          .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
		          .putHeader("X-Download-Options", "noopen")
		          .putHeader("X-XSS-Protection", "1; mode=block")
		          .putHeader("X-FRAME-OPTIONS", "DENY")
		          .sendFile(ApiHandlers.PATH_SYSTEM_DIRECTORY + "/public/home.html");
			}else {
				ctx.response().putHeader("Location", "http://localhost:9997/login.html").setStatusCode(301).end();
				return;
			}
		});
		
		router.route("/").handler(ctx -> {
				ctx.response().putHeader("Location", "http://localhost:9997/home.html")
				.setStatusCode(301).end();
		});
		
		router.route("/login.html").handler(ctx -> {
				ctx.response().putHeader("Content-Type", "text/html; charset=UTF-8")
					// do not allow proxies to cache the data
		          .putHeader("Cache-Control", "no-store, no-cache")
		          // prevents Internet Explorer from MIME - sniffing a
		          // response away from the declared content-type
		          .putHeader("X-Content-Type-Options", "nosniff")
		          // Strict HTTPS (for about ~6Months)
		          .putHeader("Strict-Transport-Security", "max-age=" + 15768000)
		          // IE8+ do not allow opening of attachments in the context of this resource
		          .putHeader("X-Download-Options", "noopen")
		          // enable XSS for IE
		          .putHeader("X-XSS-Protection", "1; mode=block")
		          // deny frames
		          .putHeader("X-FRAME-OPTIONS", "DENY")
					.sendFile(ApiHandlers.PATH_SYSTEM_DIRECTORY + "/public/login.html");
		});
		
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
		router.get("/logout").handler(api::logout);

		// Create a router endpoint for the static content.
		router.route("/*").handler(StaticHandler.create("public"));
				
		// Start the web server and tell it to use the router to handle
		// requests.
		vertx.createHttpServer().requestHandler(router::accept).listen(9997);
		
		vertx.executeBlocking(future -> {
			HttpServerOptions httpOpts = new HttpServerOptions();
				httpOpts.setKeyStoreOptions(new JksOptions().setPath("./config/webserver/.keystore.jks").setPassword("amineliazidi"));
				httpOpts.setSsl(true);
				future.complete(httpOpts);
		}, (AsyncResult<HttpServerOptions> result) -> {
            if (!result.failed()) {
                vertx.createHttpServer(result.result()).requestHandler(router::accept).listen(9997);
                System.out.println("SSL Web server listening on port :" + 9997);
                fut.complete();
            }
        });

		EventBus eb = vertx.eventBus();

		// Register to listen for messages coming IN to the server
		eb.consumer("chat.to.server").handler(message -> {
			String str_msg = message.body().toString();
			Message msg = Parser.strToMessage(str_msg);
			api.postMsg(msg);
			eb.publish("chat.to.client", msg.toJson());

			Message msg_bot = null;
			JsonObject json_response = null;
			if(BotsHandler.isBotCall(msg.getContent())){
				json_response = BotsHandler.botCall(msg.toJson().getString("content"));
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
