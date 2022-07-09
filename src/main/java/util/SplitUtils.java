package util;

public class SplitUtils {

    public static String getUrl(String httpHeader) {
        return httpHeader.split(" ")[1];
    }

}
