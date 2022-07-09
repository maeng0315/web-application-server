package util;

import model.User;

import java.net.URLDecoder;
import java.util.Map;

public class ParserUtils {

    public static User getUser(String queryString) {
        Map<String, String> queryMap = HttpRequestUtils.parseQueryString(URLDecoder.decode(queryString));
        return new User(queryMap.get("userId"), queryMap.get("password"), queryMap.get("name"), queryMap.get("email"));
    }

    public static String getRequestPath(String url) {
        if (isExistQueryString(url)) {
            return url.substring(0, url.indexOf("?"));
        }
        return url;
    }

    private static boolean isExistQueryString(String url) {
        if (url.indexOf("?") != -1) {
            return true;
        }
        return false;
    }

    public static String getQueryString(String getRequestUrl) {
        int startIndex = getRequestUrl.indexOf("?") + 1;
        return getRequestUrl.substring(startIndex);
    }

}
