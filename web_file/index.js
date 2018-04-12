var messages = {};
var myusername;
var userMessage = document.getElementById("message-text-field");
var sendMessageBtn = document.getElementById("send-message-button");
var chatHeader = document.getElementById('chat-header');
var nameClicked = document.getElementById('active-user-list');
var messageUL = document.getElementById("message-list");

sendMessageBtn.addEventListener("click", function(e) {
	userMessage.focus();
	sendMessageToHistory();

	e.preventDefault();
});

userMessage.addEventListener('keypress', function(e){
	userMessage.focus();
	if (e.keyCode == 13){
		sendMessageToHistory();
		e.preventDefault();
	}
});

function sendMessageToHistory(){
	// addMessageToChat(myusername, userMessage.value);
	if (userMessage.value !== ""){
		var m = {
			"date" : new Date(),
			"message" : userMessage.value
		};
		messages[myusername] = m;
		sendPOSTMessageToServer(messages);
		userMessage.value = "";
	}
}

function sendPOSTMessageToServer(jsonString){
	var sendToServerJSON = JSON.stringify(jsonString);
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){
		if (this.readyState == 4 && this.status == 200){
		}
	}
	req.open("POST", "/addMessage", true);
	req.setRequestHeader("Content-Type", "text/html");
	req.send(sendToServerJSON);
}

function addMessageToChat(name, msg){
	var entry = document.createElement("li");
	entry.appendChild(document.createTextNode(msg));
	entry.setAttribute("class", "my-message");
	messageUL.appendChild(entry);
}

function addMessageToThatUser(name, msg){
	var entry = document.createElement("li");
	entry.appendChild(document.createTextNode(name + ": " + msg));
	entry.setAttribute("class", "other-message");
	messageUL.appendChild(entry);
}

function addNewMessage(name, message){
	console.log(name);
	console.log(message);
	if (name !== undefined && name !== myusername){
		addMessageToThatUser(name, message);
	} else {
		addMessageToChat(name, message);
	}
}	

function checkForNewMessage(){
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){
		if (this.readyState == 4 && this.status == 200){
			var newMessage = JSON.parse(req.responseText);
			for (var i = 0; i < newMessage.messages.length; i++){
				var uMess = newMessage.messages[i];

				var key = Object.keys(uMess)[0];
				addNewMessage(key, uMess[key]);
			}
		}
	}
	req.open("GET", "/checkForNewMessage", true);
	req.setRequestHeader("Content-Type", "text/html");
	req.send();
}		

function showUserList(){
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){
		if (this.readyState == 4 && this.status == 200){
			var jsonObj = JSON.parse(req.responseText);
			for(i = 0; i < jsonObj.length; i++){
				updateUserList(jsonObj[i].username);
			}
		}
	}
	req.open("GET", "/getUsers", true);
	req.setRequestHeader("Content-Type", "text/html");
	req.send();
}

function updateUserList(newUser){
	var allUser = nameClicked.getElementsByTagName("button");
	var found = false;
	for (var i = 0; i < allUser.length; i++) {
		if (allUser[i].value === newUser)
			found = true;
	}
	if (!found){   
		var ulHis = document.getElementById("message-history");
		var person = document.createElement("button");
		person.appendChild(document.createTextNode(newUser));
		person.setAttribute("id", "people-list");
		person.setAttribute("value", newUser);
		nameClicked.appendChild(person);
	}
}  

function getMyName(){
	userMessage.focus();
	var req = new XMLHttpRequest();
	req.onreadystatechange = function(){
		if (this.readyState == 4 && this.status == 200){
			myusername = req.responseText;
			console.log(myusername);
		}
	}
	req.open("GET", "/getMyUsername", true);
	req.setRequestHeader("Content-Type", "text/html");
	req.send();
}   

window.onload = getMyName();
window.onload = showUserList();
setInterval(checkForNewMessage, 500);
setInterval(showUserList, 1000);