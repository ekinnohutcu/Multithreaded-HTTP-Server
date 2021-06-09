import java.io.*;
import java.net.*;

public class Server extends Thread {

    // Create required variables
    Socket connectedClient = null;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;

    public Server(Socket client) {
        connectedClient = client;
    }

    // Run the server
    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            // Check if the request string contains undesired situations
            String requestString = inFromClient.readLine();
            if (requestString == null || requestString.contains("favicon")) {
                return;
            }
            System.out.println("(SERVER) The Client " + connectedClient.getInetAddress() + ": "
                    + connectedClient.getPort() + " is connected.");
            System.out.println("(SERVER) Request String = " + requestString);

            // Call the request and the repsponse
            HttpRequest httpRequest = new HttpRequest(requestString);
            httpRequest.splitRequestString();
            httpRequest.sendRequest(httpRequest.getResponseBuffer(inFromClient), outToClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Run the server for proxy
    public void run(StringBuffer responseBuffer, String requestString) {
        try {
            System.out.println("(SERVER) The Proxy Server send the request to the Web Server ");

            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            // Call the request and the repsponse
            HttpRequest httpRequest = new HttpRequest(requestString);
            httpRequest.splitRequestString();
            httpRequest.sendRequest(responseBuffer, outToClient);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws Exception {
        // Read the requested port number
        int PORT;
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter the Port number = ");
        PORT = Integer.parseInt(bf.readLine());

        // Initialize the ServerSocket
        ServerSocket server = new ServerSocket(PORT, 10, InetAddress.getByName("127.0.0.1"));
        System.out.println("Server waiting for client on port " + PORT);
        System.out.println("---------------------------------------");

        // Run the server for each client
        while (true) {
            Socket connected = server.accept();
            (new Server(connected)).start();
        }
    }
}
