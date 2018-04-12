var saveUsername = document.getElementById("username-input");
var textInfo = document.getElementById("info-text");
var username;

saveUsername.focus();

saveUsername.addEventListener('keypress', function (e) {
	if (e.keyCode == 13){
		username = saveUsername.value;
		addUser();
	}
});

function addUser(){
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){
		if (this.readyState == 4 && this.status == 200 && req.responseText === "OK"){
			window.location = "index.html";
			window.history.pushState({}, 'MutliChat', '/index.html');
		} else {
			saveUsername.value = "";
		}
	}
	req.open("POST", "/adduser", true);
	req.setRequestHeader("Content-Type", "text/html");
	if (username){
		req.send(
			username
		);
	}
}