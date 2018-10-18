import cs425.mp1.util.config;
import cs425.mp1.util.logGenerator;
import cs425.mp1.util.util;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class utilTest {

    @Test
    void testHostName() {
        assertEquals(util.getIpFromHostname("eeee.illinois.edu"), "");
        assertEquals(util.getIpFromHostname("localhost"), "127.0.0.1");
        assertEquals(util.getIpFromHostname("ischool.illinois.edu"), "18.223.86.188");
    }

    @Test
    void testStreamLog() throws IOException {
        String originalString = new logGenerator(15, 45).toString();
        InputStream inputStream = new ByteArrayInputStream(originalString.getBytes());
        assertEquals(util.readerToString(new BufferedReader(new InputStreamReader(
                inputStream, config.UTF8_CHARSET))), originalString);
    }

    @Test
    @Disabled
    void userInput() {
        String test = util.getUserInput("Input this \"($*YH*)!@#($_): ");
        assertEquals(test, "\"($*YH*)!@#($_)");
    }

}
