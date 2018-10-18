package cs425.mp1.client;

import cs425.mp1.dto.GrepCommand;
import cs425.mp1.dto.GrepResult;
import cs425.mp1.util.config;
import cs425.mp1.util.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * A client class that can send and interpret with our server
 */
public class Client {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Server meta
    private int port;
    private String serverIp;
    private String serverHostname;

    // Runtime
    private String globalFailedInfo;
    private GrepCommand gc;

    // Result
    private GrepResult grepResult;

    /**
     * A new client instance to connect to one server
     *
     * @param hostname Server hostname
     * @param port     Server port
     */
    public Client(String hostname, int port) {
        this.serverHostname = hostname;
        this.serverIp = util.getIpFromHostname(hostname);
        this.port = port;
        this.globalFailedInfo = String.format("Connection to <%s> failed.", this.serverHostname);
        if (this.serverIp.isEmpty())
            throw new RuntimeException(String.format("Hostname '%s' look up failed.", hostname));
    }

    private Socket connectToServer(SocketAddress ra) throws IOException {
        Socket s = new Socket();
        // Potential higher performance with SO_KA
        s.setKeepAlive(true);
        s.connect(ra, config.CONNECT_TIMEOUT_SECOND * 1000);
        s.setSoTimeout(config.RW_TIMEOUT_SECOND * 1000);
        logger.info("Connected to server {}", this.serverIp);
        return s;
    }

    public GrepResult getGrepResult() {
        return grepResult;
    }

    /**
     * Interactive mode for user
     */
    public String startInteractive() {
        String file = util.getUserInput("What's the path of the target file:\t");
        String pattern = util.getUserInput("What's the grep pattern:\t");
        return this.startArgs(file.trim(), pattern.trim());
    }

    /**
     * Send and read the result
     */
    public String startArgs(String filename, String pattern) {
        gc = new GrepCommand(this.serverHostname, filename, pattern);
        try {
            Socket s = connectToServer(new InetSocketAddress(this.serverIp, port));
            return this.processCommand(s, gc);
        } catch (IOException e) {
            logger.debug("Failed to establish connection: {}", util.exceptionToString(e));
        }
        return String.format("Failed to connect <%s>", this.serverHostname);
    }

    /**
     * General netIO processing
     */
    private String processCommand(Socket socket, GrepCommand gc) {
        GrepResult g = null;
        try {
            // Output goes first or the input will block forever
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));

            out.writeObject(gc);
            out.flush();
            logger.info("Grep command sent on '{}'.", gc.getTimestamp());

            // Some blocking here for sure
            g = (GrepResult) in.readObject();

            // Communication finished, notice the sequence
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            logger.error("System IO failed: {}", util.exceptionToString(e));
            return globalFailedInfo;
        } catch (ClassNotFoundException e) {
            logger.error("Client received malformed data!");
            return globalFailedInfo;
        }
        return processResult(g);
    }

    /**
     * Generate a text-based result with given grep result
     */
    private String processResult(GrepResult result) {
        if (result == null || this.gc == null) {
            return globalFailedInfo;
        }
        this.grepResult = result;
        StringBuilder sb = new StringBuilder(String.format("Result from machine <%s> on '%s':%n",
                result.getMachineId(), result.getTimestamp().toString()));
        if (result.isHasFatalError()) {
            // If there is a fatal error, then no useful info else in the class
            sb.append("\tFatal error occurred on remote server, no result.");
        } else {
            if (result.isHasError()) {
                // Grep command failed?
                sb.append(String.format("\tFailed to execute: %s%n", result.getStdErr()));
                if (!result.getStdOut().isEmpty())
                    sb.append(String.format("\tAdditionally, grep output these info: \"%s\".%n", result.getStdOut()));
            } else {
                // Good result
                sb.append(String.format("\tFile path \"%s\" has %d results.%n",
                        result.getFilePath(), result.getNumOfResult()));
                //TODO: Should we display all line contents???
                // printEveryResult(sb, result);
                if (!result.getStdErr().isEmpty())
                    sb.append(String.format("\tAdditionally, grep output these warning: \"%s\".%n", result.getStdErr()));
            }
        }
        sb.append(String.format("\tServer running time: %s.%n", timeDelta(result.getTimestamp(), gc.getTimestamp())));
        String displayString = sb.toString();
        // For archiving purpose
        logger.debug(displayString);
        return displayString;
    }

    private String timeDelta(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toString();
    }

    /**
     * Method for extracting every result from grep
     */
    private void printEveryResult(StringBuilder sb, GrepResult g) {
        String[] lines = util.stringSplitByLine(g.getStdOut());
        for (int i = 0; i < g.getNumOfResult(); i++) {
            sb.append(String.format("\t\tPattern on line %d: ", g.getLineNumber().get(i)));
            sb.append(lines[i]);
            sb.append(String.format("%n"));
        }
    }

}