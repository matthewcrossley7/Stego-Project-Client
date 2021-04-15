package com.example.demo;







import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Signature;

public class ChatClient extends Thread{
    private Socket server;
    PrintWriter serverOut;

    Socket socket1;
    BufferedReader br;
    PrintWriter pw;
    public void SetPortAddress(String address, int port) {
        try {

           // server = new Socket(address,port);
            socket1 = new Socket("86.9.92.222", port);
            br = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            pw = new PrintWriter(socket1.getOutputStream(), true);
            System.out.println("Connected to server with address "+ address + " and port "+ port);


        } catch (UnknownHostException e) {

            System.out.println("UnknownHost");
        } catch (IOException e) {

            System.out.println("No server found at address and port"); //Error caught if no server found causes client application to close
            exitClient();
        } /*catch (JSONException e) {
            e.printStackTrace();
        }*/

    }

    public void sendJSON(JSONObject sendJSON) throws IOException, JSONException {


        pw.println(sendJSON.toString());


        /*serverOut = new PrintWriter(server.getOutputStream(), true);
        serverOut.println(msg);
        System.out.println("SENT MSG");
        Socket socket = new Socket("localhost",14001);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        String receiveLine = reader.readLine();
        System.out.println(receiveLine);


*/
    }
public JSONObject recieveMsg() throws JSONException, IOException {
        String str = br.readLine();
      //  System.out.println("I RECIEVED " + str);
        /*System.out.println("ABOUT TO RECIEVE MSG");
        Socket socket = new Socket("localhost",14001);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        String line = reader.readLine();
        System.out.println("RECIEVED FROM SERVER "+line);
        JSONObject jsonObject = new JSONObject(line);*/
        return new JSONObject(str);
}
public void closeSocket(){
    try {
        br.close();
        pw.close();
        socket1.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

}
    public void exitClient() {

        System.out.println("Program exiting in 3 seconds");
        try {
            Thread.sleep(3000);    			//3 Second wait time
        } catch (InterruptedException e) {
        }
        try {
            server.close();
        } catch (IOException e) { //Closes socket to server
            e.printStackTrace();
        }catch(NullPointerException NE) {

        }
        System.exit(0);		//Clean exit
    }





}
