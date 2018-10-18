package cs425.mp1.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Utility class, should NOT be initialized
 */
public class util {
    private util() {
    }

    private static final Logger logger = LoggerFactory.getLogger(util.class);

    /**
     * Split a string by lines (Regex \R pattern)
     */
    public static String[] stringSplitByLine(String s) {
        return s.split("\\R");
    }

    /**
     * Convert contents from reader to string
     */
    public static String readerToString(Reader r) throws IOException {
        if (r == null) return "";
        StringBuilder sb = new StringBuilder();
        int c = 0;
        while ((c = r.read()) != -1) {
            sb.append((char) c);
        }
        r.close();
        return sb.toString();
    }

    /**
     * Get input from user
     *
     * @param infoToUser Tip text for user
     * @return User input
     */
    public static String getUserInput(String infoToUser) {
        Scanner sc = new Scanner(System.in);
        String res = "";
        try {
            System.out.print(infoToUser);
            res = sc.nextLine().trim();
        } catch (Exception e) {
            logger.error("Get user input failed: {}", util.exceptionToString(e));
        }
        return res;
    }

    /**
     * Get IP from hostname
     */
    public static String getIpFromHostname(String host) {
        try {
            InetAddress i = InetAddress.getByName(host);
            return i.getHostAddress();
        } catch (UnknownHostException e) {
            logger.error("Get IP failed for host: {}", host);
        }
        return "";
    }

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
