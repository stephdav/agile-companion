$(document).ready(function() {
	initMSG();
});


function initMSG() {
	$('.card-body').on('click', function(e) {
		e.stopPropagation();
		log($(this).data("score")+ " selected");
	});
}