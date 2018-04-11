# MultiChat

Designed a web-based multi-client instant messenger, which allows at least two users to chat with each other. This is a
webpage built with HTML/CSS/JavaScript, AJAX, and Java. The browser establishes a TCP connection with a request to the server
and also uses XMLHttpRequest objects.

The server opens a listening socket at port 8080 and is also multithreaded. Each server generates JSON files embedded in a
HTTP response for each AJAX request it receives.

To run, compile on the terminal "javac TCPServer.java" 
and run at "java TCPServer 8080". 

To run the client browser, go to 
localhost:8080, or for other devices, 
open at [yourIPaddress]:8080
