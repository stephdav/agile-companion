package org.kik.agile.companion;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.post;

import java.util.HashMap;

import org.kik.agile.companion.model.AuthenticateResponse;
import org.kik.agile.companion.security.JWTAuthentication;
import org.kik.agile.companion.utils.JsonTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.ModelAndView;
import spark.template.pebble.PebbleTemplateEngine;

public class CompanionControler {

	private static final Logger LOG = LoggerFactory.getLogger(CompanionControler.class);

	private static final String SESSION_ID = "id";

	private JWTAuthentication auth = new JWTAuthentication();

	public CompanionControler() {

	}

	public void setup() {

		before("/companion/*", (req, res) -> {
			String id = req.cookie(SESSION_ID);
			if (id == null || !auth.isLoggedUser(id)) {
				LOG.debug("not authenticated !");
				res.redirect(PageEnum.LOGIN.getPath());
				halt();
			}
		});

		for (PageEnum page : PageEnum.values()) {
			get(page.getPath(), (request, response) -> {
				return new ModelAndView(new HashMap<String, Object>(), page.getTemplate());
			}, new PebbleTemplateEngine());
		}

		before("/api/*", (request, response) -> {
			StringBuffer sb = new StringBuffer();
			for (String param : request.queryParams()) {
				if (sb.length() == 0) {
					sb.append("?");
				} else {
					sb.append("&");
				}
				sb.append(param).append("=").append(request.queryParams(param));
			}
			LOG.debug("{} {}{}", request.requestMethod(), request.uri(), sb.toString());
		});

		post("/api/security/authenticate", (req, res) -> {
			AuthenticateResponse jsonObject = auth.authenticate(req, res);
			// TODO : HTTPS
				String sessionId = auth.generateSessionId();
				res.cookie("/", SESSION_ID, sessionId, -1, false, true);
				auth.addLoggedUser(sessionId);
				res.type("application/json");
				return jsonObject;
			}, new JsonTransformer());

		post("/api/security/validate", (req, res) -> {
			AuthenticateResponse jsonObject = auth.validate(req);
			res.type("application/json");
			return jsonObject;
		}, new JsonTransformer());

		// Revoke the token (logout)
		post("/api/security/revoke", (req, res) -> {
			AuthenticateResponse jsonObject = auth.revoke(req);
			auth.logoutUser(req.cookie(SESSION_ID));
			res.type("application/json");
			return jsonObject;
		}, new JsonTransformer());
	}

}
