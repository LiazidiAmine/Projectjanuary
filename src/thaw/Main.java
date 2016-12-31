package thaw;

import io.vertx.core.Vertx;
import thaw.api.Server;

public class Main {

	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx();
		vertx.deployVerticle(new Server());
	}

}
