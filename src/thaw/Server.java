package thaw;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

// java
// --add-exports java.base/sun.nio.ch=ALL-UNNAMED
// --add-exports java.base/sun.net.dns=ALL-UNNAMED

public class Server extends AbstractVerticle {

	private Connection connection = null;

	@Override
	public void start(Future<Void> fut) throws Exception {

		startBackend(
				(connection) ->  
						startWebApp((http) -> completeStartup(http, fut)), fut);

	}

	private void startBackend(Handler<AsyncResult<Connection>> next, Future<Void> fut) {
		
		try {
			// create a database connection
			try(Connection connection = DriverManager.getConnection("jdbc:sqlite:thaw.db")){
				Statement statement = connection.createStatement();
			    statement.setQueryTimeout(30);  
				connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS User ("
						+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "Username VARCHAR(20), "
						+ "Email VARCHAR(20),"
						+ "Timestamp INTEGER);");
			}
		} catch(SQLException e) {
			fut.fail(e.getMessage());
			
		}
		next.handle(Future.succeededFuture());
	}

	private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
		// Create a router object.
		Router router = Router.router(vertx);

		router.route("/*").handler(StaticHandler.create("assets"));
		/*
		 * router.get("/api/whiskies").handler(this::getAll);
		 * router.route("/api/whiskies*").handler(BodyHandler.create());
		 */

		// Create the HTTP server and pass the "accept" method to the request
		// handler.
		vertx.createHttpServer().requestHandler(router::accept).listen(
				// default port to 9997
				config().getInteger("http.port", 9997), next::handle);
		System.out.println("Server listening on port 9997");
	}

	private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
		if (http.succeeded()) {
			fut.complete();
		} else {
			fut.fail(http.cause());
		}
	}

	@Override
	public void stop() throws Exception {
		// Close the database connection.
		connection.close();
	}

}
