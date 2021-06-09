import java.io.*;

public class HttpResponse {

    // Create and print the response messages
    public void sendResponse(int statusCode, String responseString, boolean isFile, DataOutputStream outToClient)
            throws Exception {

        // Holds required contents
        String statusLine = null;
        String serverDetails = "Server: Java HTTPServer" + "\r\n";
        String contentLengthLine = null;
        String fileName = null;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        FileInputStream fin = null;

        if (statusCode == 200) {
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        } else if (statusCode == 400) {
            statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
        } else if (statusCode == 501) {
            statusLine = "HTTP/1.1 501 Not Implemented" + "\r\n";
        }

        // If the file exists read it, else form the html string
        if (isFile) {
            fileName = responseString; // 100.html
            fin = new FileInputStream(fileName);
            contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n"; // size
        } else {
            responseString = "<html>" + "<title>HTTP Server in java</title>" + "<body>" + responseString + "</body>"
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
        if (isFile)
            sendFile(fin, outToClient);
        else
            outToClient.writeBytes(responseString);

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

    // Set the html string for the file
    public void setHtmlString(int sizeOfDoc) throws IOException {

        String body = "";
        int counter = 0;
        int temp = sizeOfDoc;

        // Hold the length of the size
        while (temp >= 1) {
            temp = temp / 10;
            counter++;
        }

        // Fill the file until it reach the requested size
        // 70 is size of the constant html file strings
        for (int i = 0; i < sizeOfDoc - 70 - counter; i++) {
            body += "a";
        }

        String htmlString = "<HTML>" + "<HEAD>" + "<TITLE>I am " + sizeOfDoc + " bytes long</TITLE>" + "</HEAD>"
                + "<BODY>" + body + "</BODY>" + "</HTML>";
        System.out.println(htmlString);
        createHtmlFile(htmlString, String.valueOf(sizeOfDoc));
    }

    // Create the html file
    public void createHtmlFile(String htmlString, String sizeOfDoc) throws IOException {
        try {
            FileWriter myWriter = new FileWriter(sizeOfDoc + ".html");
            myWriter.write(htmlString);
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred while creating HTML file.");
            e.printStackTrace();
        }
    }
}
