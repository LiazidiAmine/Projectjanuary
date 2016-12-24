package thaw;

import io.vertx.core.Vertx;
import thaw.api.Server;

public class Main {

	public static void main(String[] args) {
		// development option, avoid caching to see changes of
		// static files without having to reload the application,
		// obviously, this line should be commented in production
		// System.setProperty("vertx.disableFileCaching", "true");

		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Server());
	}

}
