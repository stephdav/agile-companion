package org.kik.agile.companion.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import org.kik.agile.companion.model.AuthenticateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTAuthentication {

	private static final Logger LOG = LoggerFactory.getLogger(JWTAuthentication.class);

	// private static final String COOKIE_SECURE_FGP = "__Secure-Fgp";
	private static final String COOKIE_SECURE_FGP = "fgp";

	private static final List<String> loggedUsers = new ArrayList<String>();

	/**
	 * Accessor for HMAC key - Block serialization and storage as String in JVM
	 * memory
	 */
	private transient byte[] keyHMAC = null;

	/**
	 * Accessor for Ciphering key - Block serialization and storage as String in
	 * JVM memory
	 */
	private transient byte[] keyCiphering = null;

	/** Accessor for Issuer ID - Block serialization */
	private transient String issuerID = null;

	/** Random data generator */
	private SecureRandom secureRandom = new SecureRandom();

	/** Handler for token ciphering */
	private TokenCipher tokenCipher;

	/** Handler for token revokation */
	private TokenRevoker tokenRevoker;

	public JWTAuthentication() {
		keyHMAC = "puthaJEzuXe3Recr3buFAs84pUDeBRUB".getBytes();
		keyCiphering = "5EvuQaQa5es2Uswe".getBytes();
		issuerID = "5beb7bd2-8528-4e73-84dd-6e82f18a76ff";

		try {
			tokenCipher = new TokenCipher();
			tokenRevoker = new TokenRevoker();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public AuthenticateResponse authenticate(Request req, Response res) {
		String login = req.queryParams("login");

		AuthenticateResponse jsonObject = new AuthenticateResponse();
		try {
			// Validate the login parameter content to avoid malicious
			// input
			if (Pattern.matches("[a-zA-Z0-9]{1,10}", login)) {

				// Generate a random string that will constitute the
				// fingerprint for this user
				byte[] randomFgp = new byte[50];
				this.secureRandom.nextBytes(randomFgp);

				String userFingerprint = DatatypeConverter.printHexBinary(randomFgp);

				// Add the fingerprint in a hardened cookie
				// TODO : HTTPS
				// res.cookie("/", COOKIE_SECURE_FGP, userFingerprint, -1, true,
				// true);
				res.cookie("/", COOKIE_SECURE_FGP, userFingerprint, -1, false, true);

				// Compute a SHA256 hash of the fingerprint in order to
				// store the fingerprint hash (instead of the raw value) in
				// the token to prevent an XSS to be able to read the
				// fingerprint and set the expected cookie itself
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] userFingerprintDigest = digest.digest(userFingerprint.getBytes("utf-8"));
				String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

				// Create the token with a validity of 15 minutes and client
				// context (fingerprint) information
				Calendar c = Calendar.getInstance();
				Date now = c.getTime();
				c.add(Calendar.MINUTE, 15);
				Date expirationDate = c.getTime();
				Map<String, Object> headerClaims = new HashMap<>();
				headerClaims.put("typ", "JWT");
				String token = JWT.create().withSubject(login).withExpiresAt(expirationDate).withIssuer(this.issuerID)
						.withIssuedAt(now).withNotBefore(now).withClaim("userFingerprint", userFingerprintHash)
						.withHeader(headerClaims).sign(Algorithm.HMAC256(this.keyHMAC));

				// Cipher the token
				String cipheredToken = tokenCipher.cipherToken(token, this.keyCiphering);

				// Set token in data container
				jsonObject.setToken(cipheredToken);
				jsonObject.setStatus("Authentication successful !");
			} else {
				jsonObject.setToken("-");
				jsonObject.setStatus("Invalid parameter provided !");
			}

		} catch (Exception e) {
			LOG.error("Error during authentication", e);
			// Return a generic error message
			jsonObject.setToken("-");
			jsonObject.setStatus("An error occur !");
		}
		return jsonObject;
	}

	public AuthenticateResponse validate(Request req) {
		String authToken = req.headers("Authorization");

		AuthenticateResponse jsonObject = new AuthenticateResponse();
		try {
			// Retrieve the token
			String cipheredToken = authToken;
			if (cipheredToken != null) {
				cipheredToken = cipheredToken.split(" ")[1].trim();
			} else {
				throw new SecurityException("Token is mandatory !");
			}

			// Check if the token is not revoked
			if (tokenRevoker.isTokenRevoked(cipheredToken)) {
				jsonObject.setStatus("Token already revoked !");
			} else {
				// Retrieve the user fingerprint from the dedicated cookie
				String userFingerprint = req.cookie(COOKIE_SECURE_FGP);

				// Validate the userFingerprint and token parameters content
				// to avoid malicious input
				LOG.debug("FGP ===> {}", userFingerprint);
				if (userFingerprint != null && Pattern.matches("[A-Z0-9]{100}", userFingerprint)) {
					// Decipher the token
					String token = this.tokenCipher.decipherToken(cipheredToken, this.keyCiphering);
					// Compute a SHA256 hash of the received fingerprint in
					// cookie in order to compare to the fingerprint hash
					// stored in the cookie
					MessageDigest digest = MessageDigest.getInstance("SHA-256");
					byte[] userFingerprintDigest = digest.digest(userFingerprint.getBytes("utf-8"));
					String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);
					// Create a verification context for the token
					JWTVerifier verifier = JWT.require(Algorithm.HMAC256(this.keyHMAC)).withIssuer(this.issuerID)
							.withClaim("userFingerprint", userFingerprintHash).build();
					// Verify the token
					DecodedJWT decodedToken = verifier.verify(token);
					// Set token in data container
					jsonObject.setStatus("Token OK - Welcome '" + decodedToken.getSubject() + "' !");
				} else {
					jsonObject.setStatus("Invalid parameter provided !");
				}
			}

		} catch (JWTVerificationException e) {
			LOG.warn("Verification of the token failed", e);
			// Return info that validation failed
			jsonObject.setStatus("Invalid token !");
		} catch (Exception e) {
			LOG.warn("Error during token validation", e);
			// Return a generic error message
			jsonObject.setStatus("An error occur !");
		}
		return jsonObject;
	}

	public AuthenticateResponse revoke(Request req) {
		String authToken = req.headers("Authorization");

		AuthenticateResponse jsonObject = new AuthenticateResponse();
		try {
			// Retrieve the token
			String cipheredToken = authToken;
			if (cipheredToken != null) {
				cipheredToken = cipheredToken.split(" ")[1].trim();
				tokenRevoker.revokeToken(cipheredToken);
				jsonObject.setStatus("Token successfully revoked !");
			} else {
				throw new SecurityException("Token is mandatory !");
			}
		} catch (Exception e) {
			LOG.warn("Error during token validation", e);
			// Return a generic error message
			jsonObject.setStatus("An error occur !");
		}
		return jsonObject;
	}

	public String generateSessionId() {
		byte[] randomFgp = new byte[20];
		this.secureRandom.nextBytes(randomFgp);
		return DatatypeConverter.printHexBinary(randomFgp);
	}

	public void addLoggedUser(String sessionId) {
		loggedUsers.add(sessionId);
	}

	public boolean isLoggedUser(String sessionId) {
		return loggedUsers.contains(sessionId);
	}

	public void logoutUser(String sessionId) {
		if (sessionId != null) {
			loggedUsers.remove(sessionId);
		}
	}
}
