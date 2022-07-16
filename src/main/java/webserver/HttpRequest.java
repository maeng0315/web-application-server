package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private final InputStream in;
    private String[] tokens;
    private String method;
    private String path;
    private Map<String, String> headerMap = new HashMap<>();
    private BufferedReader br;

    public HttpRequest(InputStream in) throws IOException {
        this.in = in;
        String line = getRequestLine(in);
        setRequsetHeader(line);
    }

    private String getRequestLine(InputStream in) throws IOException {
        br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = br.readLine();
        if (line == null) {
            return "";
        }
        log.debug("request line : {}", line);
        this.tokens = line.split(" ");
        this.method = this.tokens[0];
        this.path = this.tokens[1].split("[?]")[0];
        return line;
    }

    private void setRequsetHeader(String line) throws IOException {
        while (line != null) {
            line = br.readLine();
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            if (pair == null) {
                return;
            }
            headerMap.put(pair.getKey(), pair.getValue());
        }
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHeader(String key) {
        return headerMap.get(key);
    }

    public String getParameter(String key) throws IOException {
        String queryString = "";
        if ("POST".equals(this.method)) {
            queryString = IOUtils.readData(br, Integer.parseInt(getHeader("Content-Length")));
            log.debug("Requestbody : {}", queryString);
        } else {
            int queryStringStartIndex = tokens[1].indexOf("?") + 1;
            queryString = tokens[1].substring(queryStringStartIndex);
        }
        Map<String, String> parseQueryString = HttpRequestUtils.parseQueryString(queryString);
        log.debug("parseQueryString : {}", parseQueryString);
        return parseQueryString.get(key);
    }
}
