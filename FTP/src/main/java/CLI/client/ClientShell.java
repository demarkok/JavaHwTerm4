package CLI.client;

import asg.cliche.Command;
import asg.cliche.Param;
import asg.cliche.Shell;
import asg.cliche.ShellFactory;
import asg.cliche.ShellManageable;
import core.client.Client;
import core.client.ClientInterface;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.NotYetConnectedException;
import java.nio.file.NotDirectoryException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * The client console application.
 */
public class ClientShell implements ShellManageable {

    private static final String PROMPT = "ftp-cli";
    private static final String APP_NAME = "FTP-Client";
    private static final String COMMANDS_MESSAGE = "usage:\n"
        + "    connect [ip] - connect to server\n"
        + "    disconnect - disconnect\n"
        + "    list [path] - show containing files and directories\n"
        + "    get [src] [dst] - download the file\n"
        + "    exit";
    private static ClientInterface client;

    private static boolean connected;

    /**
     * Connects to the server hosted on {@code ip}.
     * @param ip - host address
     * @return message representing the result of connection.
     */
    @NotNull
    @Command(description = "connect to ftp server")
    public String connect(@Param(name = "ip", description = "address of server") @NotNull String ip) {

        if (client.connect(ip)) {
            connected = true;
            return info("Connected.");
        } else {
            return error("Unable to connect.");
        }
    }

    /**
     * Executes the list query: show files and directories placed in {@code path}.
     * @param path - absolute path on server
     * @return message representing the result.
     */
    @NotNull
    @Command
    public String list(@NotNull String path) {
        try {
            List<String> result =  client.executeList(path);
            return StringUtils.join(result, "\n");
        } catch (NotDirectoryException e) {
            return error("Not a directory.");
        } catch (FileNotFoundException e) {
            return error("No such directory.");
        } catch (NotYetConnectedException e) {
            return error("Not connected.");
        } catch (IOException e) {
            e.printStackTrace();
            return error("IO Error.\n");
        }
    }

    /**
     * Executes the get query: download the file placed on server in {@code pathSrc}.
     * @param pathSrc - absolute file path on server
     * @param pathDst - absolute file path on client
     * @return message representing the result.
     */
    @NotNull
    @Command
    public String get(@NotNull String pathSrc, @NotNull String pathDst) {
        try {
            client.executeGet(pathSrc, pathDst);
        } catch (FileNotFoundException e) {
            return error("No such file.");
        } catch (NotYetConnectedException e) {
            return error("Not connected.");
        } catch (IOException e) {
            e.printStackTrace();
            return error("IO Error.");
        }
        return info("Ok.");
    }

    /**
     * Tries to disconnect from server.
     * @return message representing the result.
     */
    @NotNull
    @Command
    public String disconnect(){
        try {
            if (!connected) {
                return error("Not connected yet.");
            }
            client.disconnect();
            connected = false;
            return info("Disconnected.");
        } catch (IOException e) {
            return error("Unable to disconnect.");
        }
    }

    /**
     * Runs the shell.
     * @param args is expected to be empty.
     */
    public static void main(String[] args) {
        client = new Client();
        try {
            Shell shell = ShellFactory.createConsoleShell(PROMPT, APP_NAME, new ClientShell());
            System.out.println(COMMANDS_MESSAGE);
            shell.commandLoop();
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    @NotNull
    private static String error(@NotNull String message) {
        return "  [!] " + message;
    }

    @NotNull
    private static String info(@NotNull String message) {
        return "  [i] " + message;
    }

    @Override
    public void cliEnterLoop() {

    }

    @Override
    public void cliLeaveLoop() {
        if (connected) {
            disconnect();
        }
    }
}
