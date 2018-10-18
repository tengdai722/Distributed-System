package cs425.mp1.server;

import cs425.mp1.dto.GrepCommand;
import cs425.mp1.dto.GrepResult;
import cs425.mp1.util.config;
import cs425.mp1.util.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cs425.mp1.util.util.readerToString;

/**
 * A multi-threaded server that runs grep command
 */
public class Server {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String serverHostname;
    private ServerSocket serverSocket;

    // Runtime variable
    private ExecutorService exec;
    private boolean isStopped = true;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
            this.serverHostname = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (Exception e) {
            logger.error("Failed to initialize server: {}", util.exceptionToString(e));
        }
    }

    /**
     * Start running the server loop
     */
    public void start() {
        if (this.serverSocket == null) {
            logger.info("Server failed to start listening...");
            return;
        }
        this.isStopped = false;
        // Parallel processing from clients
        int threads = config.CORE_NUMBER;
        this.exec = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            this.exec.submit(this.serverMainThread());
        }
        logger.info("Server started listening...");
    }

    /**
     * Stop running the server
     */
    public void stop() {
        try {
            if (this.serverSocket != null) this.serverSocket.close();
        } catch (IOException e) {
            logger.error("Killed an ongoing connection.");
        }
        this.isStopped = true;
        if (this.exec == null || this.exec.isShutdown() || this.exec.isTerminated()) {
            logger.error("Try to shutdown a closed main server thread.");
        } else {
            try {
                this.exec.shutdown();
                this.exec.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Server termination timeout, forcing exit...");
                System.exit(2);
            }
        }
        logger.info("Server stopped gracefully...");
    }

    /**
     * Main thread routine for processing commands from client
     */
    private Runnable serverMainThread() {
        return () -> {
            Thread.currentThread().setName(String.format("Server-loop-<%s>", this.serverHostname));
            while (!this.isStopped) {
                Socket clientSocket;
                try {
                    clientSocket = this.serverSocket.accept();
                    clientSocket.setSoTimeout(config.RW_TIMEOUT_SECOND * 1000);
                    logger.info("Connection from client {}.", clientSocket.getRemoteSocketAddress());
                } catch (IOException e) {
                    if (this.isStopped) logger.debug("Server stopped too early {}", e.getLocalizedMessage());
                    else logger.error("Server socket failed to accept: {}", util.exceptionToString(e));
                    return;
                }
                try {
                    // From now on, we should start to handle the commands from client and run for a result
                    // Output goes first or the input will block forever
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                    GrepCommand command = (GrepCommand) in.readObject();
                    GrepResult gr = runForGrepResult(command);

                    out.writeObject(gr);
                    out.flush();

                    // Exchange finished, close connection and notice the sequence
                    in.close();
                    out.close();
                    clientSocket.close();
                    logger.info("Grep result built on '{}' was sent.", gr.getTimestamp());
                } catch (IOException e) {
                    logger.error("Server processing error: {}", util.exceptionToString(e));
                } catch (ClassNotFoundException e) {
                    logger.error("Server received malformed data!");
                }
            }
            logger.info("Server thread exited...");
        };
    }

    /**
     * Analyze the command and run it, then return the result object
     */
    private GrepResult runForGrepResult(GrepCommand command) {
        GrepResult result = new GrepResult();
        if (command == null) {
            // Such a serious problem just do nothing
            result.setHasFatalError(true);
            return result;
        }
        logger.info("Grep request with timestamp '{}' received.", command.getTimestamp());
        grepCommandBuilder gcb = new grepCommandBuilder(command);
        String grepComm = gcb.toString();
        // Save more info for debugging and testing
        logger.debug("Final grep command: {}", grepComm);
        result.setGrepCommands(grepComm);
        result.setFilePath(command.getFilePath());

        // Execute grep command from system and get the result
        try {
            Process p = Runtime.getRuntime().exec(cs425.mp1.util.config.GREP_NIX_COMMAND(grepComm));

            String stdout = readerToString(new BufferedReader(new InputStreamReader(p.getInputStream(), config.UTF8_CHARSET)));
            String stderr = readerToString(new BufferedReader(new InputStreamReader(p.getErrorStream(), config.UTF8_CHARSET)));

            /*
             * From Linux grep man page:
             * Exit status is 0 if any line is selected, 1 otherwise;
             * If any error occurs and -q is not given, the exit status is 2.
             */
            result.setMachineId(this.serverHostname);
            result.setStdOut(stdout);
            result.setStdErr(stderr);
            result.setTimestamp(LocalDateTime.now());
            // Get the return code for grep, wait if necessary
            switch (p.waitFor()) {
                case 0:
                case 1:
                    processGrepLineData(result, stdout);
                    break;
                case 2:
                default:
                    result.setHasError(true);
                    break;
            }
        } catch (Exception e) {
            logger.error("Grep command failed: {}", util.exceptionToString(e));
            result.setHasFatalError(true);
            result.setStdErr(e.getLocalizedMessage());
        }

        return result;
    }

    /**
     * Process the grep returned result to populate complicated fields for GrepResult
     */
    private void processGrepLineData(GrepResult r, String data) {
        String[] lines = util.stringSplitByLine(data);
        List<Integer> lineId = new LinkedList<>();
        for (String l : lines) {
            try {
                String[] target = l.split(":");
                if (target.length > 0) {
                    int lineN = Integer.parseInt(target[0].trim());
                    lineId.add(lineN);
                }
            } catch (NumberFormatException n) {
                logger.error("Fail to parse number {}", n.getLocalizedMessage());
                continue;
            }
        }
        r.setLineNumber(lineId);
    }

}