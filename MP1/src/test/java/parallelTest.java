
import cs425.mp1.client.ParallelClient;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parallel tests running on EE-VMs
 * Start server on VMs first!
 */
public class parallelTest {

    @Test
    void paraTest1() throws InterruptedException {
        String p = "lmf";
        for (int i = 1; i <= 10; i++) {
            assertEquals(paraTestCommon(p, i), 857);
        }
    }

    @Test
    void paraTest2() throws InterruptedException {
        String p = "3.1";
        for (int i = 1; i <= 4; i++) {
            assertEquals(paraTestCommon(p, i), 861304);
        }
    }

    @Test
    void paraTest3() throws InterruptedException {
        String p = "3.14";
        for (int i = 1; i <= 4; i++) {
            assertEquals(paraTestCommon(p, i), 66520);
        }
    }

    @Test
    void paraTest4() throws InterruptedException {
        String p = "3.14159";
        for (int i = 1; i <= 4; i++) {
            assertEquals(paraTestCommon(p, i), 15);
        }
    }

    long paraTestCommon(String p, int t) throws InterruptedException {
        Instant start = Instant.now();
        ParallelClient pc;
        pc = new ParallelClient(t, p, Paths.get(".", "server_list_test1.txt"));
        pc.doGrep();
        Instant end = Instant.now();
        long res = pc.getNumberOfAllMatches();
        System.err.println(String.format("Running time (<%d> threads, <%s> pattern, <%s> results): %s%n",
                t, p, res, Duration.between(start, end).toString()));
        return res;
    }

}
