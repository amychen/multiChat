import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import java.text.*;
import java.util.concurrent.ConcurrentHashMap;

public class TCPServer{

  Socket connectionSocket;
  static Vector<ClientHandler> activeUser = new Vector<>();
  static ConcurrentHashMap<String, ArrayList<JSONObject>> onlineUserArr = new ConcurrentHashMap<String, ArrayList<JSONObject>>();
  static ConcurrentHashMap<String, JSONObject> newMessage = new ConcurrentHashMap<String, JSONObject>();
  static ConcurrentHashMap<String, String> lastActive = new ConcurrentHashMap<String, String>();

  public TCPServer(Socket s){
    try                 {connectionSocket = s;}
    catch (Exception e) {e.printStackTrace();}
  } 

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(8080);

    while (true) {
      final Socket connectionSocket = welcomeSocket.accept();
      ClientHandler ch = new ClientHandler(connectionSocket);
      synchronized (activeUser){
        activeUser.add(ch);
      }
      ch.start();
      ch.updateList();
    }
  }

  public static class ClientHandler extends Thread {
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    public static String userID = "";

    public ClientHandler(Socket s) throws Exception{
      inFromClient = new BufferedReader(new InputStreamReader(s.getInputStream()));
      outToClient = new DataOutputStream(s.getOutputStream());
    }

    public void updateList() {
      ArrayList<String> delList = new ArrayList<String>();
      DateFormat dateFormat = new SimpleDateFormat("mm");
      Date date = new Date();
      String time = dateFormat.format(date);
      for (String key : onlineUserArr.keySet()) {
        String activeTime = lastActive.get(key);
        //if a user is not active for ten minutes its name will be removed
        if (activeTime != null) {
          if (Integer.parseInt(activeTime) + 10 < Integer.parseInt(time)) {
            delList.add(key);
            System.out.println(delList.size());
          }
        }
      }
      for (int i = 0; i < delList.size(); i++) {
        synchronized(activeUser) {
          activeUser.remove(delList.get(i));
          onlineUserArr.remove(delList.get(i));
        }
        System.out.println("onlines users remain:" + onlineUserArr.size());
      }
    }

    @SuppressWarnings("unchecked")
    public void run(){
      try {
        String methodHeader = inFromClient.readLine().trim();
        System.out.println(methodHeader);
        String[] headLine = methodHeader.split(" "); 
        String method = headLine[0];
        String path = headLine[1];

        if (method.equals("GET")){
          if (path.endsWith("/")) {
            InputStream loginHTML = new FileInputStream("web_files/login.html");
            byte[] l_bytes = new byte[loginHTML.available()];

            okResponse(outToClient, loginHTML.read(l_bytes));

            loginHTML.close();
            outToClient.write(l_bytes);
            outToClient.flush();
          } 
          else if (path.endsWith("/index.html")){   
            InputStream indexHTML = new FileInputStream("web_files/index.html");
            byte[] i_bytes = new byte[indexHTML.available()];

            okResponse(outToClient, indexHTML.read(i_bytes));
            
            indexHTML.close();
            outToClient.write(i_bytes);
            outToClient.flush();
          } 
          else if (path.endsWith("/getUsers")){
            JSONArray userArray = new JSONArray();
            for (String user : onlineUserArr.keySet()){
              JSONObject obj = new JSONObject();
              obj.put("username", user);
              userArray.put(obj);
            }
            String userArrayStr = userArray.toString();
            okResponse(outToClient, userArrayStr.length());
            outToClient.writeBytes(userArrayStr + "\r\n");
          }
          else if (path.endsWith("/getMyUsername")){
            okResponse(outToClient, userID.length());
            outToClient.writeBytes(userID);
          }
          else if (path.endsWith("/checkForNewMessage")){
            List<String> dates = new ArrayList<String>(newMessage.keySet());
            Collections.sort(dates);
            ArrayList<JSONObject> newMessageJSON = new ArrayList<JSONObject>();
            for (int i = 0; i < dates.size(); i++){
              newMessageJSON.add(newMessage.get(dates.get(i)));
            }
            //[{"hi":"fds"}, {"hi":"jfskla"}, {"hi":"jk"}]

            JSONObject a = new JSONObject();
            a.put("messages", newMessageJSON);

            okResponse(outToClient, a.toString().length());
            outToClient.writeBytes(a.toString());
            //newMessage = new ConcurrentHashMap<String, JSONObject>();
          }
          else {
            outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n");
            outToClient.writeBytes("\r\n");
          }
        }
        else if (method.equals("POST")){
          if (path.endsWith("/adduser")){
            char[] buf = readPostRequest(inFromClient);
            String username = new String(buf);
            if (onlineUserArr.containsKey(username)){
              okResponse(outToClient, 2);
              outToClient.writeBytes("NO \r\n");
            } else {
              //initialize the time
              DateFormat dateFormat = new SimpleDateFormat("mm");
              Date date = new Date();
              String time = dateFormat.format(date);
              lastActive.put(username, time);

              onlineUserArr.put(username, new ArrayList<JSONObject>());
              userID = username;
              okResponse(outToClient, 2);
              outToClient.writeBytes("OK \r\n");
            }
          }
          else if (path.endsWith("/addMessage")){
            char[] buf = readPostRequest(inFromClient);
            String messages = new String(buf);

            //{"currentUser":{"date": date, "message": message}}
            JSONObject obj = new JSONObject(messages);

            Iterator<String> keys= obj.keys();
            if (keys.hasNext())
            {
              String user = keys.next();
              //{"date":date, "message":message}
              JSONObject messageObj = obj.getJSONObject(user);
              String date = messageObj.getString("date");
              String message = messageObj.getString("message");

              //get the minutes
              lastActive.put(userID, date.substring(14, 16));

              //{date: message}
              JSONObject dateMessagePair = new JSONObject();
              dateMessagePair.put(date, message);

              ArrayList<JSONObject> m = onlineUserArr.get(userID);
              m.add(dateMessagePair);
              //{user = [{date:message}, {date:message}, {date:message}], user1 = [{}]}
              onlineUserArr.put(user, m);

              JSONObject jsonUserMessage = new JSONObject();
              jsonUserMessage.put(user, message);
              //{date: {user, message}, date: {user, message}}
              newMessage.put(date, jsonUserMessage);

              //{"user": user, "message": message, "date":date}
              JSONObject sendOut = new JSONObject();
              sendOut.put("user", userID);
              sendOut.put("date", date);
              sendOut.put("message", message);

              okResponse(outToClient, sendOut.toString().length());
              outToClient.writeBytes(sendOut.toString());
            }
          }
        }
      } 
      catch (Exception e) {
        e.printStackTrace();
      } finally {
        synchronized(activeUser){
          activeUser.remove(this);
          onlineUserArr.remove(this);
        }
        try{
          inFromClient.close();
          outToClient.close();
        } catch (Exception t){
          t.printStackTrace();
        }
      }
    }

    public void okResponse(DataOutputStream s, int contentlength) throws IOException{
      s.writeBytes("HTTP/1.1 200 OK \r\n");
      s.writeBytes("Content-Length: " + Integer.toString(contentlength) + "\r\n");
      s.writeBytes("Access-Control-Allow-Origin: * \r\n");
      s.writeBytes("Connection: close \r\n");
      s.writeBytes("\r\n");
    }     

    public char[] readPostRequest(BufferedReader s) throws IOException{
      boolean headersFinished = false;
      int contentLength = -1;
      while (!headersFinished) {
        String line = s.readLine();
        headersFinished = line.isEmpty();
        if (line.startsWith("Content-Length:")) {
          contentLength = Integer.parseInt(line.substring("Content-Length:".length()).trim());
        }
      }
      char[] buf = new char[contentLength];
      s.read(buf);
      return buf;
    }
  }
}