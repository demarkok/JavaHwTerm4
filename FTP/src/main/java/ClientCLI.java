import java.io.Console;

/**
 *  Represents command line interface of the ftp client.
 */
public class ClientCLI {

    private static final String HELP_MSG = "commands:\n"
        + "    connect [ip]\n"
        + "    disconnect\n"
        + "    list\n"
        + "    get [file]\n";

    private ClientInterface client;

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            error("[!] console error");
        }
        console.printf("%s\n", HELP_MSG);
    }

    private static void error(String message) {
        System.out.println(message);
        System.exit(0);
    }
}
