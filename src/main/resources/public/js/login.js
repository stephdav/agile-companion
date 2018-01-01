$(document).ready(function() {
	initLogin();
});


function initLogin() {
	$('#login').on('click', function(e) {
		e.stopPropagation();
		getToken($("#username").val(),redirectToHome);
	});
}