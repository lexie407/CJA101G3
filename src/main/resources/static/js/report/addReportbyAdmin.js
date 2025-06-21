document.addEventListener("DOMContentLoaded", function() {

	var successMessageElement = document.getElementById('successMessage');
		var message = successMessageElement.value;
		if (message) {
			alert(message);
		}

});