package util;

public class SplitUtils {

    public static String getUrl(String readHeader) {
        return readHeader.split(" ")[1];
    }

    public static String getMethodType(String readHeader) {
        return readHeader.split(" ")[0];
    }

}
