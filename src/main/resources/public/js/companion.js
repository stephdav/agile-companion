$(document).ready(function() {
	$('#logout').on('click', function(e) {
		e.stopPropagation();
		revokeToken(redirectToLogin);
	});
});

function log(message) {
	if (console && console.log) {
		console.log(message);
	}
}

function handleAjaxError(jqXHR, status, errorThrown) {
	log("Ajax error (jqXHR.status=" + jqXHR.status + " - status=" + status + " - errorThrown=" + errorThrown+")");
}


function parseStatusAndHeader(jqXHR) {
	var hv = {};
	hv.status = jqXHR.status;
	hv.acceptRange = jqXHR.getResponseHeader('Accept-Range');
	hv.contentRange = jqXHR.getResponseHeader('Content-Range');
	hv.location = jqXHR.getResponseHeader('Location');
	return hv;
}

function ajaxPost(url, payload, fnHandle) {
	log('POST ' + url + ' with payload ' + JSON.stringify(payload));
	$.ajax({
	    url: url,
	    type: "POST",
	    dataType: "json",
	    data: JSON.stringify(payload),
	    contentType: "application/json"
	}).done(function(data, textStatus, jqXHR) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(data, hv, "no error");
	}).fail(function(jqXHR, status, errorThrown) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(jqXHR.responseText, hv, errorThrown);
	});
}

function ajaxGet(url, fnHandle) {
	log('GET ' + url);
	$.ajax({
		url : url
	}).done(function(data, textStatus, jqXHR) {
		var hv = parseStatusAndHeader(jqXHR);
		if (fnHandle !== undefined) {
			fnHandle(data, hv);
		}
	}).fail(function(jqXHR, status, errorThrown) {
		var hv = parseStatusAndHeader(jqXHR);
		if (fnHandle !== undefined) {
			fnHandle(jqXHR.responseText, hv, errorThrown);
		}
	});
}

function ajaxDelete(url, fnHandle) {
	log('DELETE ' + url);
	$.ajax({
	    url: url,
	    type: "DELETE",
	}).done(function(data, textStatus, jqXHR) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(data, hv, "no error");
	}).fail(function(jqXHR, status, errorThrown) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(jqXHR.responseText, hv, errorThrown);
	});
}

function ajaxPatch(url, payload, fnHandle) {
	log('PATCH ' + url + ' with payload ' + JSON.stringify(payload));
	$.ajax({
	    url: url,
	    type: "PATCH",
	    dataType: "json",
	    data: JSON.stringify(payload),
	    contentType: "application/json"
	}).done(function(data, textStatus, jqXHR) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(data, hv, "no error");
	}).fail(function(jqXHR, status, errorThrown) {
		var hv = parseStatusAndHeader(jqXHR);
		fnHandle(jqXHR.responseText, hv, errorThrown);
	});
}

function redirectToHome() {
	window.open("companion/home","_self");	
}

function redirectToLogin() {
	window.open("../login","_self");	
}
