package cs425.mp1;

import cs425.mp1.client.Client;
import cs425.mp1.client.ParallelClient;
import cs425.mp1.server.Server;
import cs425.mp1.util.config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static Server s = null;

    public static void main(String... args) {
        if (args.length != 1 && args.length != 3) {
            printHelpText();
        }

        switch (args[0]) {
            case "c":
            case "C":
                logger.info("In client mode...");
                Client c = new Client("127.0.0.1", config.PORT);
                String res;
                if (args.length == 1) res = c.startInteractive();
                else res = c.startArgs(args[1].trim(), args[2].trim());
                System.out.println(res);
                break;
            case "s":
            case "S":
                logger.info("In server mode...");
                addServerStopHook();
                s = new Server(config.PORT);
                s.start();
                break;
            case "p":
            case "P":
                if (args.length != 3) printHelpText();
                ParallelClient pc = new ParallelClient(Integer.parseInt(args[1]), args[2].trim(),
                        Paths.get(".", "server_list.txt"));
                // Following is a blocking command
                try {
                    pc.doGrep();
                } catch (InterruptedException e) {
                    logger.error("Parallel client failed to execute: {}", e.getLocalizedMessage());
                }
                System.out.print(pc.getAllResults());
                break;
            default:
                logger.error("No such mode!\n");
                System.exit(-1);
        }
    }

    private static void printHelpText() {
        String help = "Select a mode to run:\n" +
                "  c: Grep local client (filename, grep pattern)\n  s: Grep server (No arguments)\n  p: Parallel grep client (# of threads, grep pattern)\n" +
                "    Store file name and hostname for parallel grep client in the 'server_list.txt' file!";
        logger.error(help);
        System.exit(1);
    }

    /**
     * Add this hook to clear up the server's resources
     */
    private static void addServerStopHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (s != null) s.stop();
        }));
    }

}
