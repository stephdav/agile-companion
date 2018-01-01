package org.kik.agile.companion;

import static spark.Spark.post;
import static spark.Spark.staticFileLocation;
import static spark.Spark.webSocket;

public class App {

	public static void main(String[] args) {

		staticFileLocation("/public");

		webSocket("/echo", EchoWebSocket.class);

		// port(5678); <- Uncomment this if you want spark to listen to port
		// 5678 instead of the default 4567

		new CompanionControler().setup();

		post("/api/login", (request, response) -> "Hello World: " + request.body());

		// get("/private", (request, response) -> {
		// response.status(401);
		// return "Go Away!!!";
		// });
		//
		// get("/users/:name", (request, response) -> "Selected user: " +
		// request.params(":name"));
		//
		// get("/news/:section", (request, response) -> {
		// response.type("text/xml");
		// return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><news>" +
		// request.params("section") + "</news>";
		// });
		//
		// get("/protected", (request, response) -> {
		// halt(403, "I don't think so!!!");
		// return null;
		// });
		//
		// get("/redirect", (request, response) -> {
		// response.redirect("/news/world");
		// return null;
		// });
		//
		// get("/", (request, response) -> "root");
	}

}
