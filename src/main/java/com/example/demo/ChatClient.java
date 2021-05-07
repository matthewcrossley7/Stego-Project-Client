package com.example.demo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;


public class ChatClient extends Thread{
    private Socket server;
    Socket socket1;
    BufferedReader br;
    PrintWriter pw;

    //Creates a connection with the server on specified address and port
    public void SetPortAddress(String address, int port) {
        try {
            socket1 = new Socket("86.9.92.222", port);
            br = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            pw = new PrintWriter(socket1.getOutputStream(), true);
            System.out.println("Connected to server with address "+ address + " and port "+ port);

        //Errors handled
        } catch (UnknownHostException e) {
            System.out.println("UnknownHost");
        } catch (IOException e) {
            System.out.println("No server found at address and port"); //Error caught if no server found causes client application to close
            exitClient();
        }

    }
    //Function sends JSON object to server
    public void sendJSON(JSONObject sendJSON) throws IOException, JSONException {
        System.out.println("SENDING JSON: "+sendJSON);
        pw.println(sendJSON.toString());
    }
    //Function receives JSON reply from server
    public JSONObject recieveMsg() throws JSONException, IOException {
        String str = br.readLine();
        System.out.println("RECEIVED JSON: " + str);
        return new JSONObject(str);
}
    //Closes connection to server
    public void closeSocket(){
    try {
        br.close();
        pw.close();
        socket1.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
}
    //closes connection with server
    public void exitClient() {

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
