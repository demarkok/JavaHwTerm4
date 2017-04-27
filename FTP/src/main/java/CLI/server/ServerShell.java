package CLI.server;

import asg.cliche.Command;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import core.common.exceptions.ServerAlreadyStartedException;
import core.server.Server;
import core.server.ServerInterface;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * The server console application.
 */
public class ServerShell {

    private static final String COMMANDS_MESSAGE = "usage:\n"
        + "    start - start ftp server\n"
        + "    stop - stop ftp server\n"
        + "    exit";
    private static final String PROMPT = "ftp-srv";
    private static final String APP_NAME = "FTP-server";

    private static ServerInterface server;

    /**
     * Starts the server.
     * @return message representing the result.
     */
    @NotNull
    @Command(description = "start ftp server")
    public String start() {
        try {
            server.start((t, e) -> e.printStackTrace());
        } catch (ServerAlreadyStartedException e) {
            return error("Server has already started.");
        }
        return info("Server started.");
    }

    /**
     * Stops the server.
     * @return message representing the result.
     */
    @NotNull
    @Command(description = "stop ftp server")
    public String stop() {
        server.stop();
        return info("Server stopped.");
    }

    /**
     * Runs the shell.
     * {@param args} is expected to be empty.
     */
    public static void main(String[] args) {
        server = new Server();

        try {
            Shell shell = ShellFactory.createConsoleShell(PROMPT, APP_NAME, new ServerShell());
            System.out.println(COMMANDS_MESSAGE);
            shell.commandLoop();
        } catch (IOException e) {
            System.out.println(error("Error."));
            e.printStackTrace();
        }
    }

    @NotNull
    private static String error(@NotNull String message) {
        return "[!]  " + message;
    }

    private static String info(@NotNull String message) {
        return "[i]  " + message;
    }
}
