package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.IOUtils;
import util.ParserUtils;
import util.SplitUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private int contentLength = 0;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String read = br.readLine();
            String url = SplitUtils.getUrl(read);
            log.debug("url : {}", url);
            String methodType = SplitUtils.getMethodType(read);
            printHeader(br, read);

            String requestPath = ParserUtils.getRequestPath(url);
            log.debug("requestPath : {}", requestPath);
            String queryString = ParserUtils.getQueryString(url);
            if ("POST".equals(methodType)) {
                queryString = IOUtils.readData(br, contentLength);
            }
            log.debug("queryString : {}", queryString);

            User user = getUser(requestPath, queryString);

            DataOutputStream dos = new DataOutputStream(out);

//            byte[] body = "Hello Maeng".getBytes();
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User getUser(String requestPath, String queryString) {
        if ("/user/create".equals(requestPath)) {
            User user = ParserUtils.getUser(queryString);
            log.debug("user : {}", user);
            return user;
        }
        return null;
    }

    private void printHeader(BufferedReader br, String read) throws IOException {
        while (!"".equals(read) && read != null) {
            if (read.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(read.split(":")[1].trim());
            }
            log.debug("read : {}", read);
            read = br.readLine();
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
