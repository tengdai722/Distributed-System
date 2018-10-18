package cs425.mp1.client;

import cs425.mp1.util.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * Parallelize the original Client class to speed up
 */
public class ParallelClient {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int numOfThreads;
    private String grepPattern;
    private Path serverListFile;

    private LinkedBlockingQueue<String[]> parallelJobs;
    private LinkedBlockingQueue<String> parallelResults;
    private AtomicLong totalMatches = new AtomicLong(0);

    private String allResults = "Parallel client failed to execute!";

    /**
     * Init the distributed-grep client
     *
     * @param t          Number of threads
     * @param gP         Grep pattern
     * @param serverList The server_list.txt file
     */
    public ParallelClient(int t, String gP, Path serverList) {
        this.numOfThreads = t;
        this.grepPattern = gP;
        this.serverListFile = serverList;
    }

    /**
     * Submit the jobs to start distributed-grep
     */
    public void doGrep() throws InterruptedException {
        this.parallelResults = new LinkedBlockingQueue<>();
        this.parallelJobs = populateJobs();

        ExecutorService exec = Executors.newFixedThreadPool(this.numOfThreads);
        LocalDateTime start = LocalDateTime.now();
        // Submit tasks to fill the thread pool
        for (int i = 0; i < this.numOfThreads; i++) {
            exec.submit(clientRunnable(this.grepPattern));
        }
        exec.shutdown();
        // Block for getting output and counting time
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        LocalDateTime end = LocalDateTime.now();
        // Get summary
        populateParallelResults(start, end);
    }

    public String getAllResults() {
        return this.allResults;
    }

    public long getNumberOfAllMatches() {
        return this.totalMatches.get();
    }

    /**
     * Get result generated from parallel process, also total running time.
     */
    private void populateParallelResults(LocalDateTime start, LocalDateTime end) {
        if (parallelResults.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        String result;
        int i = 0;
        while ((result = parallelResults.poll()) != null) {
            sb.append(String.format("Result %d:", ++i));
            sb.append(result);
            logger.debug(result);
        }
        // Print out the total running time for parallel client for stat purpose
        String time = String.format("%nTotal matches: %d%nClient timing: %s%n", totalMatches.get(), Duration.between(start, end).toString().substring(2));
        sb.append(time);
        logger.debug(time);

        this.allResults = sb.toString();
    }

    /**
     * Populate a task queue from a text file
     */
    private LinkedBlockingQueue<String[]> populateJobs() {
        LinkedBlockingQueue<String[]> pj = new LinkedBlockingQueue<>();
        if (!this.serverListFile.toFile().exists()) {
            logger.warn("Missing server_list.txt file!");
            return pj;
        }
        try (Stream<String> lines = Files.lines(this.serverListFile)) {
            lines.forEach(line -> {
                String[] data = line.split(",");
                if (data.length != 2) {
                    logger.error("Malformed server_list file line: '{}'.", line);
                    return;
                }
                pj.offer(data);
            });
        } catch (IOException e) {
            logger.error("Read server.list file failed: {}", e.getLocalizedMessage());
        }
        return pj;
    }

    /**
     * Start a client by sending request to server
     */
    private Runnable clientRunnable(String grepPattern) {
        return () -> {
            String[] job;
            while ((job = parallelJobs.poll()) != null) {
                Client c = new Client(job[0].trim(), config.PORT);
                parallelResults.offer(c.startArgs(job[1].trim(), grepPattern));
                totalMatches.addAndGet(c.getGrepResult().getNumOfResult());
                logger.info("{} finished.", job[0]);
            }
        };
    }

}
