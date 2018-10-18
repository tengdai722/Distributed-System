package cs425.mp1.server;

import cs425.mp1.dto.GrepCommand;
import cs425.mp1.util.config;

public class grepCommandBuilder {

    private GrepCommand userConfig;

    private String command = config.GREP_PATH + " --help";

    /**
     * Build the final grep command from client
     */
    public grepCommandBuilder(GrepCommand command) {
        this.userConfig = command;
        this.build();
    }

    private void build() {
        String pattern = this.userConfig.getArgs();
        if (pattern == null || pattern.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder(config.GREP_PATH);
        sb.append(' ');

        // Add a show line number switch if user not specified
        if (!pattern.contains("-n"))
            sb.append("-n ");

        // Wrap every user's input with quote to avoid problems
        sb.append(' ');
        sb.append(pattern);
        sb.append(" \"");
        sb.append(this.userConfig.getFilePath());
        sb.append('"');

        this.command = sb.toString();
    }

    @Override
    public String toString() {
        return this.command;
    }
}
