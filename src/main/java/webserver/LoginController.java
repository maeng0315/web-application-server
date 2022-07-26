package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController implements Controller {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Override
    public void service(HttpRequest request, HttpResponse response) {
        User user = DataBase.findUserById(request.getParameter("userId"));
        if (user != null) {
            if (request.getParameter("password").equals(user.getPassword())) {
                response.addHeader("Set-Cookie", "logined=true");
                response.sendRedirect("/index.html");
            } else {
                response.sendRedirect("/user/login_failed.html");
            }
        }
    }
}
