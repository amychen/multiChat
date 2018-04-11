import java.io.*;
import java.net.*;

public class TCPServer implements Runnable{

  Socket connectionSocket;

  public TCPServer(Socket s){
    try{
      connectionSocket = s;
    } catch (Exception e){
      e.printStackTrace();
    }
  }

  public void run(){
    try {
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

      String methodHeader = inFromClient.readLine();
      System.out.println(methodHeader);
      String[] headLine = methodHeader.split(" "); 
      String method = headLine[0];
      String path = headLine[1];

      if (method.equals("GET")){
        if (path.endsWith("/")) {
          InputStream loginHTML = new FileInputStream("web_files/login.html");
          byte[] l_bytes = new byte[loginHTML.available()];

          outToClient.writeBytes("HTTP/1.1 200 OK \r\n");
          outToClient.writeBytes("Access-Control-Allow-Origin: * \r\n");
          outToClient.writeBytes("Connection: close \r\n");
          outToClient.writeBytes("\r\n");

          loginHTML.read(l_bytes);
          outToClient.write(l_bytes);
          outToClient.flush();
          loginHTML.close();

          System.out.println("HI");
        } 
        else if (path.endsWith("/index.html")){
          InputStream indexHTML = new FileInputStream("web_files/index.html");
          byte[] i_bytes = new byte[indexHTML.available()];

          outToClient.writeBytes("HTTP/1.1 200 OK \r\n");
          outToClient.writeBytes("Access-Control-Allow-Origin: * \r\n");
          outToClient.writeBytes("Connection: close \r\n");
          outToClient.writeBytes("\r\n");

          indexHTML.read(i_bytes);
          outToClient.write(i_bytes);
          outToClient.flush();
          indexHTML.close();

          System.out.println("BYE");
        } 
        else {
          outToClient.writeBytes("HTTP/1.1 404 Not Found\r\n");
          outToClient.writeBytes("\r\n");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String argv[]) throws Exception {
    ServerSocket welcomeSocket = new ServerSocket(8080);

    while (true) {
      final Socket connectionSocket = welcomeSocket.accept();
      TCPServer server = new TCPServer(connectionSocket);

      Thread serverThread = new Thread(server);
      serverThread.start();
    }
  }
}