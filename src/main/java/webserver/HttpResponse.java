package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;
    private Map<String, String> headers = new HashMap<>();

    public HttpResponse(OutputStream out) {
        dos = new DataOutputStream(out);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void forward(String url) throws IOException {
        final byte[] body = forwardBody(url);
        response200Header(body.length, "text/html;charset=utf-8");
        responseBody(body);
    }

    public byte[] forwardBody(String url) throws IOException {
        return Files.readAllBytes(new File("./webapp" + url).toPath());
    }

    public void response200Header(int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void sendRedirect(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void processHeaders() {
        headers.forEach((key, value)
                -> {
            try {
                dos.writeBytes(key + ": " + value + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
