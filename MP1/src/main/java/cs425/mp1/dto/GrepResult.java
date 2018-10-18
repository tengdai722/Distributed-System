package cs425.mp1.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

/**
 * Grep result send from servers to client
 */
public class GrepResult implements Serializable {
    private static final long serialVersionUID = -9082757785696291762L;

    /**
     * Machine ID (Hostname or/and IP)
     */
    private String machineId;

    /**
     * Exceptions in processing process?
     */
    private boolean hasFatalError = false;

    /**
     * Grep success?
     */
    private boolean hasError = false;

    /**
     * Timestamp when the grep finished
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Line numbers
     */
    private List<Integer> lineNumber;

    /**
     * File name of the processed file
     */
    private String filePath;

    /**
     * Copy of the output of standard error
     */
    private String stdErr;

    /**
     * Copy of the output of standard output
     */
    private String stdOut;

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Final executed grep command
     */
    private String grepCommands;

    public String getGrepCommands() {
        return grepCommands;
    }

    public void setGrepCommands(String grepCommands) {
        this.grepCommands = grepCommands;
    }

    public int getNumOfResult() {
        return this.lineNumber.size();
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<Integer> getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(List<Integer> lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public boolean isHasFatalError() {
        return hasFatalError;
    }

    public void setHasFatalError(boolean hasFatalError) {
        this.hasFatalError = hasFatalError;
    }

}
