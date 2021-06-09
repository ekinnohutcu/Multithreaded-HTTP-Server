import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

public class HttpRequest {

    // Hold required strings
    private String requestString;
    private String httpMethod;
    private String httpQueryString;
    private String queryString;
    // Hold response for each request
    private HttpResponse httpResponse;

    // Constructor initialize req str and httpresponse
    public HttpRequest(String requestString) {
        this.requestString = requestString;
        this.httpResponse = new HttpResponse();
    }

    // Read the request and store the http method and query string
    public void splitRequestString() {
        StringTokenizer tokenizer = new StringTokenizer(this.requestString);
        this.httpMethod = tokenizer.nextToken();
        this.httpQueryString = tokenizer.nextToken();
        String[] queryArray = httpQueryString.split("/");

        if (queryArray.length > 1) {
            this.queryString = "/" + queryArray[queryArray.length - 1];
        } else {
            this.queryString = "/";
        }
    }

    // Read and store the remaining contents of request
    public StringBuffer getResponseBuffer(BufferedReader inFromClient) throws IOException {
        StringBuffer responseBuffer = new StringBuffer();

        while (inFromClient.ready()) {
            responseBuffer.append(this.requestString + "<BR>");
            this.requestString = inFromClient.readLine();
        }

        return responseBuffer;
    }

    // Check the request and call the correct method according to the request
    public void sendRequest(StringBuffer responseBuffer, DataOutputStream outToClient) throws Exception {
        String file = queryString.replaceFirst("/", "");

        // Check undesired inputs and also control the if the query string is not numeric
        if (!isNumeric(file) && !httpQueryString.contains("localhost")) {
            this.httpResponse.sendResponse(400, "<b>400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!</b>",
                    false, outToClient);
            System.out.println("400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!");
            System.out.println("---------------------------------------");
            return;
        }

        // Condition of GET method
        if (httpMethod.equals("GET")) {
            String fileName = queryString.replaceFirst("/", "");
            // Check if the query string is numeric
            if (isNumeric(fileName)) {
                int sizeOfDoc = Integer.parseInt(fileName);
                // Check the size of document
                if (sizeOfDoc >= 100 && sizeOfDoc <= 20000) {
                    fileName += ".html";
                    // Set and create the html file
                    this.httpResponse.setHtmlString(sizeOfDoc);
                    // If the file is correctly created
                    if (new File(fileName).isFile()) {
                        this.httpResponse.sendResponse(200, fileName, true, outToClient);
                    }
                } else {
                    this.httpResponse.sendResponse(400,
                            "<b>400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE BETWEEN 100 AND 20.000!</b>", false,
                            outToClient);
                    System.out.println("400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE BETWEEN 100 AND 20.000!");
                }
            } else {
                this.httpResponse.sendResponse(400,
                        "<b>400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!</b>", false, outToClient);
                System.out.println("400 - BAD REQUEST - REQUESTED FILE SIZE SHOULD BE AN INTEGER!");
            }
        }
        // If the http method is not GET
        else {
            this.httpResponse.sendResponse(501, "<b>501 - NOT IMPLEMENTED!</b>", false, outToClient);
            System.out.println("501 - NOT IMPLEMENTED!");
        }
        System.out.println("---------------------------------------");
    }

    // Check if a string is numeric
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
