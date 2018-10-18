import cs425.mp1.client.Client;
import cs425.mp1.dto.GrepResult;
import cs425.mp1.server.Server;
import cs425.mp1.util.config;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    static Server serverInstance;
    static String host = "localhost";
    static String file = "testlog.txt";

    @BeforeEach
    @Test
    void initServer() {
        serverInstance = new Server(config.PORT);
        assertNotNull(serverInstance);
        serverInstance.start();
    }

    @Test
    void clientTest1() {
        Client c1 = new Client(host, config.PORT);
        c1.startArgs(file, "你？");
        GrepResult r = c1.getGrepResult();
        assertFalse(r.isHasError());
        assertFalse(r.isHasFatalError());
        assertEquals(r.getNumOfResult(), 0);
    }


    @Test
    void clientTest2() {
        Client c1 = new Client(host, config.PORT);
        c1.startArgs(file, "01");
        assertEquals(c1.getGrepResult().getNumOfResult(), 33);
    }

    @Test
    void clientTest3() {
        Client c1 = new Client(host, config.PORT);
        c1.startArgs(file, "vh(*EWYt97p8&P!@#YP*");
        GrepResult r = c1.getGrepResult();
        assertNotNull(r);
        assertEquals(r.getNumOfResult(), 0);
        assertFalse(r.isHasError());
        assertFalse(r.isHasFatalError());
    }

    @AfterEach
    @Test
    void stopServer() {
        serverInstance.stop();
    }

}
