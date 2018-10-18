package cs425.mp1.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Command send by client to servers
 */
public class GrepCommand implements Serializable {
    private static final long serialVersionUID = 6218034033477918071L;

    /**
     * Target machine ID (Hostname)
     */
    private String targetId;

    /**
     * Time that this being send
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * User args for grep command
     */
    private String args;

    /**
     * File name
     */
    private String filePath;

    /**
     * Initialize grep command object
     *
     * @param tid Target machine ID
     * @param fn  Target file name
     * @param p   Target grep pattern
     */
    public GrepCommand(String tid, String fn, String p) {
        this.targetId = tid;
        this.filePath = fn;
        this.args = p;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
