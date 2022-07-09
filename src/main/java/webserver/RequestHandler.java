package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ParserUtils;
import util.SplitUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

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
            String url = SplitUtils.getUrl(read);;
            String requestPath = ParserUtils.getRequestPath(url);
            log.debug("url : {}", url);
            log.debug("requestPath : {}", requestPath);
            printHeader(br, read);
            User user = getUser(requestPath, url);
            log.debug("user : {}", user);

            DataOutputStream dos = new DataOutputStream(out);

//            byte[] body = "Hello Maeng".getBytes();
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private User getUser(String requestPath, String url) {
        if ("/user/create".equals(requestPath)) {
            return ParserUtils.getUser(ParserUtils.getQueryString(url));
        }
        return null;
    }

    private void printHeader(BufferedReader br, String read) throws IOException {
        while (!"".equals(read) && read != null) {
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
