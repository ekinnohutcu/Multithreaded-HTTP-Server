import java.io.*;
import java.net.*;
import java.util.*;

public class Proxy extends Thread {

    // Create required variables
    Socket connectedClient = null;
    BufferedReader inFromClient = null;
    DataOutputStream outToClient = null;
    String mainReqString = null;
    StringBuffer mainRespBuffer = null;
    Server webServer = null;

    public Proxy(Socket client) {
        try {
            connectedClient = client;
        } catch (Exception e) {
            System.out.println("!!!!THERE IS NO PORT!!!!");
        }
    }

    // Run the proxy server
    public void run() {
        try {
            inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
            outToClient = new DataOutputStream(connectedClient.getOutputStream());

            // Read the request line
            mainReqString = inFromClient.readLine();

            createRequest(mainReqString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read the request and call the response
    public void createRequest(String requestString) throws Exception {
        // Return if the request string is null
        if (requestString == null) {
            return;
        }

        // Split the request for required strings
        StringTokenizer tokenizer = new StringTokenizer(requestString);
        String httpMethod = tokenizer.nextToken();
        String httpQueryString = tokenizer.nextToken();
        String[] queryArray = httpQueryString.split("/");
        String queryString = null;

        if (queryArray.length > 1) {
            queryString = "/" + queryArray[queryArray.length - 1];
        } else {
            queryString = "/";
        }

        StringBuffer responseBuffer = new StringBuffer();

        // Read and store the remaining contents of request
        while (inFromClient.ready()) {
            responseBuffer.append(requestString + "<BR>");
            requestString = inFromClient.readLine();
        }

        this.mainRespBuffer = responseBuffer;

        // Check the undesired requests
        // if (!httpQueryString.contains("localhost")) {
        //     return;
        // }
        if (httpQueryString.contains("favicon.ico")) {
            return;
        }

        System.out.println("(PROXY) The Client " + connectedClient.getInetAddress() + ": " + connectedClient.getPort()
                + " is connected.");
        System.out.println("(PROXY) Request String = " + mainReqString);

        // Condition of GET method
        if (httpMethod.equals("GET")) {
            String fileName = queryString.replaceFirst("/", "");
            // Check if the query string is numeric
            if (isNumeric(fileName)) {
                int sizeOfDoc = Integer.parseInt(fileName);
                fileName += ".html";
                // Check the size of document
                if (sizeOfDoc <= 9999) {
                    // Check if the request is even
                    if (sizeOfDoc % 2 == 0) {
                        // If the request is even, always modify it
                        webServer = new Server(connectedClient);
                        if (webServer == null) {
                            createContent(404, "<b>404 - WEB SERVER IS NOT RUNNING CURRENTLY!</b>", false);
                            System.out.println("404 - NOT FOUND - WEB SERVER IS NOT RUNNING CURRENTLY!");
                            System.out.println("---------------------------------------");
                        } else {
                            webServer.run(mainRespBuffer, mainReqString);
                        }
                    } else {
                        // If the request is odd check if the file exits
                        // Return the file if it exits, else create it
                        if (new File(fileName).isFile()) {
                            createContent(200, fileName, true);
                            System.out.println("---------------------------------------");
                        } else {
                            webServer = new Server(connectedClient);
                            if (webServer == null) {
                                createContent(404, "<b>404 - WEB SERVER IS NOT RUNNING CURRENTLY!</b>", false);
                                System.out.println("404 - NOT FOUND - WEB SERVER IS NOT RUNNING CURRENTLY!");
                                System.out.println("---------------------------------------");
                            } else {
                                webServer.run(mainRespBuffer, mainReqString);
                            }
                        }
                    }
                } else {
                    // Return the size error
                    createContent(414,
                            "<b>414 - REQUESTED URI IS TOO LONG - PROXY ONLY SENDS REQUESTS WHICH ARE LOWER OR EQUAL THAN 9999!</b>",
                            false);
                    System.out.println("414 - REQUEST-URI TOO LONG!");
                    System.out.println("---------------------------------------");
                }
            } else {
                // Return the numeric input error
                createContent(400, "<b>400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!</b>", false);
                System.out.println("400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!");
                System.out.println("---------------------------------------");
            }
        } else {
            // Return the false method request error
            createContent(501, "<b>THE PROXY SERVER ONLY SENDS GET MESSAGES TO THE WEB SERVER!</b>", false);
            System.out.println("501 - NOT IMPLEMENTED!");
            System.out.println("---------------------------------------");
        }
    }

    // Check a string is numeric
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Create the content of the response
    public void createContent(int statusCode, String responseString, boolean isFile) throws Exception {
        String statusLine = null;
        String content = null;
        String fileName = null;
        String serverDetails = "Server: Java HTTPServer" + "\r\n";
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        String contentLengthLine = "Content-Length: ";

        FileInputStream fin = null;

        switch (statusCode) {
            case 200:
                statusLine = "HTTP/1.1 200 OK\r\n";
                break;
            case 400:
                statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
                content = "400 Bad Request";
                contentLengthLine += content.length();
                break;
            case 501:
                statusLine = "HTTP/1.1 501 Not Implemented\r\n";
                content = "501 Not Implemented";
                contentLengthLine += content.length();
                break;
            case 404:
                statusLine = "HTTP/1.1 404 Not Found\r\n";
                content = "404 Not Found";
                contentLengthLine += content.length();
                break;
            case 414:
                statusLine = "HTTP/1.1 414 Request-URI Too Long\r\n";
                content = "414 Request-URI Too Long";
                contentLengthLine += content.length();
                break;
            default:
                break;
        }

        // If the file exits form the response, else form the error message
        if (isFile) {
            fileName = responseString; // 100.html
            fin = new FileInputStream(fileName);
            System.out.println("Cache has the requested file " + fileName + ".");
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n"; // size
        } else {
            responseString = "<html>" + "<title>Proxy Server in java</title>" + "<body>" + responseString + "</body>"
                    + "</html>";
            contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
        }

        // Print the contents to the DataOutputStream
        outToClient.writeBytes(statusLine);
        outToClient.writeBytes(serverDetails);
        outToClient.writeBytes(contentTypeLine);
        outToClient.writeBytes(contentLengthLine);
        outToClient.writeBytes("Connection: close\r\n");
        outToClient.writeBytes("\r\n");

        // Return the output
        if (isFile) {
            sendFile(fin, outToClient);
        } else {
            outToClient.writeBytes(responseString);
        }

        outToClient.close();
    }

    // Return the output file
    public void sendFile(FileInputStream fin, DataOutputStream out) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public static void main(String args[]) throws Exception {
        int PORT = 8888;
        String ADDRESS = "127.0.0.1";

        ServerSocket server = new ServerSocket(PORT, 10, InetAddress.getByName(ADDRESS));
        System.out.println("Proxy Server waiting for client on port " + PORT);
        System.out.println("---------------------------------------");

        // Run the server for each client
        while (true) {
            try {
                Socket connected = server.accept();

                (new Proxy(connected)).start();
            } catch (Exception e) {
                continue;
            }
        }
    }
}
