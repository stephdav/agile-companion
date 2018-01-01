/* Handle request for JWT token and local storage*/
function getToken(login, successCallback) {
	var postData = "login=" + encodeURIComponent(login);

	$.ajax({
	    url: "/api/security/authenticate",
	    type: "POST",
	    data: postData,
	    xhrFields: {
            withCredentials: true
        },
        crossDomain: true
	}).done(function(data, textStatus, jqXHR) {
		if (data.status == "Authentication successful !") {
			log("Token received and stored in session storage !");
			sessionStorage.setItem("token", data.token);
			successCallback();
		} else {
			log(data.status);
			sessionStorage.removeItem("token");
		}
	}).fail(function(jqXHR, status, errorThrown) {
		log("Fail : " + errorThrown);
		sessionStorage.removeItem("token");
	});
}

/* Handle request for JWT token validation */
function validateToken() {
	var token = sessionStorage.getItem("token");

	if (token == undefined || token == "") {
		log("Obtain a JWT token first :)");
		return;
	}

	$.ajax({
		url : "/api/security/validate",
		type : "POST",
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", "bearer " + token);
		},
		success : function(data) {
			log("Success : " + data.status);
		},
		error : function(jqXHR, textStatus, error) {
			log("Error: " + error);
		},
	});
}

/* Handle request for JWT token revocation (logout) */
function revokeToken(callback) {
	var token = sessionStorage.getItem("token");

	if (token == undefined || token == "") {
		log("Obtain a JWT token first :)");
		return;
	}

	$.ajax({
		url : "/api/security/revoke",
		type : "POST",
		beforeSend : function(xhr) {
			xhr.setRequestHeader("Authorization", "bearer " + token);
		},
		success : function(data) {
			log("Success : " + data.status);
			callback();
		},
		error : function(jqXHR, textStatus, error) {
			log("Error : " + error);
		},
	});
}