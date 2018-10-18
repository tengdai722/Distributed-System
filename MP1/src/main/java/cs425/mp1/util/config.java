package cs425.mp1.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Configuration class, should not be initialized
 */
public class config {
    private config() {
    }

    /**
     * Path to grep program (Varies between Mac/Linux/Windows)
     */
    public static final String GREP_PATH = "grep";

    /**
     * Assemble a command for *NIX based system
     */
    public static final String[] GREP_NIX_COMMAND(String assembledCommand) {
        return new String[]{"sh", "-c", assembledCommand};
    }

    public static final int PORT = 9276;
    public static final int CONNECT_TIMEOUT_SECOND = 60;
    public static final int RW_TIMEOUT_SECOND = 300;

    public static final Charset UTF8_CHARSET = Charset.forName(StandardCharsets.UTF_8.name());
    public static final int CORE_NUMBER = Runtime.getRuntime().availableProcessors();
}
