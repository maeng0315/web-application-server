package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;
import util.ParserUtils;
import util.SplitUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    private int contentLength = 0;

    private boolean isLoginCookie = false;

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
            String responseCode = "200";
            boolean isLoginPage = false;
            boolean isLogin = false;

            printHeader(br, read);

            String requestPath = ParserUtils.getRequestPath(url);
            log.debug("requestPath : {}", requestPath);
            String queryString = ParserUtils.getQueryString(url);
            if ("POST".equals(methodType)) {
                queryString = IOUtils.readData(br, contentLength);
            }
            log.debug("queryString : {}", queryString);

            if ("/user/create".equals(requestPath)) {
                User user = ParserUtils.getUser(queryString);
                if (user != null) {
                    log.debug("user : {}", user);
                    DataBase.addUser(user);
                    responseCode = "302";
                    requestPath = "/index.html";
                }
            }

            if ("/user/login".equals(requestPath)) {
                isLoginPage = true;
                responseCode = "302";
                requestPath = "/user/login_failed.html";
                Map<String, String> queryMap = HttpRequestUtils.parseQueryString(queryString);
                User findUser = DataBase.findUserById(queryMap.get("userId"));
                if (findUser != null) {
                    if (findUser.getPassword().equals(queryMap.get("password"))) {
                        isLogin = true;
                        requestPath = "/index.html";
                    }
                }
            }

            if ("/user/list".equals(requestPath)) {
                if (!isLoginCookie) {
                    responseCode = "302";
                    requestPath = "/index.html";
                }
            }

            DataOutputStream dos = new DataOutputStream(out);

//            byte[] body = "Hello Maeng".getBytes();
            byte[] body = Files.readAllBytes(new File("./webapp" + requestPath).toPath());

            responseHeader(dos, body.length, responseCode, requestPath, isLogin, isLoginPage);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void printHeader(BufferedReader br, String read) throws IOException {
        while (!"".equals(read) && read != null) {
            if (read.startsWith("Content-Length:")) {
                contentLength = Integer.parseInt(read.split(":")[1].trim());
            }
            if (read.startsWith("Cookie:")) {
                Map<String, String> parseCookies = HttpRequestUtils.parseCookies(read.split("Cookie:")[1].trim());
                isLoginCookie = Boolean.parseBoolean(parseCookies.get("logined"));
            }
            log.debug("read : {}", read);
            read = br.readLine();
        }
    }

    private void responseHeader(DataOutputStream dos, int lengthOfBodyContent, String responseCode, String requestPath, boolean isLogin, boolean isLoginPage) {
        try {
            String contentType = "text/html;charset=utf-8\r\n";
            if (requestPath.indexOf(".css") > 0) {
                contentType = "text/css\r\n";
            }
            dos.writeBytes("HTTP/1.1 " + responseCode + " OK \r\n");
            dos.writeBytes("Content-Type: "+contentType);
            if (isLoginPage) {
                dos.writeBytes("Set-Cookie: logined="+isLogin+"\r\n");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            if ("302".equals(responseCode)) {
                dos.writeBytes("Location: "+requestPath + "\r\n");
            }
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
