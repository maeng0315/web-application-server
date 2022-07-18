package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            String url = request.getPath();

            if (url.startsWith("/user/create")) {
                User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"), request.getParameter("email"));
                log.debug("user : {}", user);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            } else if ("/user/login".equals(url)) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user == null) {
                    response.forward("/user/login_failed.html");
                    return;
                }
                if (request.getParameter("password").equals(user.getPassword())) {
                    response.addHeader("Set-Cookie", "logined=true");
                    response.sendRedirect("/index.html");
                } else {
                    response.addHeader("Set-Cookie", "logined=false");
                    response.forward("/user/login_failed.html");
                }
            } else if ("/user/list".equals(url)) {
                if (!isLogin(request.getHeader("Cookie"))) {
                    response.forward("/user/login.html");
                    return;
                }
                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");

                byte[] body = sb.toString().getBytes();
                response.response200Header(body.length, "text/html;charset=utf-8");
                response.responseBody(body);
            } else if (url.endsWith(".css")) {
                byte[] body = response.forwardBody(url);
                response.response200Header(body.length, "text/css");
                response.responseBody(body);
            } else {
                response.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private boolean isLogin(String headerLine) {
        Map<String, String> cookies = HttpRequestUtils.parseCookies(headerLine.trim());
        String value = cookies.get("logined");
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }
}
