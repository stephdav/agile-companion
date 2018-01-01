package org.kik.agile.companion.model;

public class AuthenticateResponse {

	private String token;
	private String status;

	public AuthenticateResponse() {
		// Empty constructor
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
