package core.client;

import java.io.IOException;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents interface of ftp client.
 */
public interface ClientInterface {

    /**
     * Connects to remote ftp server.
     * @param ip - server address.
     * @return true iff the connection was successful.
     */
    boolean connect(@NotNull String ip);

    /**
     * Closes the connection.
     * throws IOException
     */
    void disconnect() throws IOException;

    /**
     * Executes the list query.
     * @param path - absolute directory path.
     * @return list of files and directories placed in the directory.
     * throws IOException
     */
    @NotNull
    List<String> executeList(@NotNull String path) throws IOException;

    /**
     * Executes the get query. Downloads file from server to client.
     * @param pathSrc - absolute path of the file on server.
     * @param pathDst - absolute path of the destination file.
     * throws IOException
     */
    void executeGet(@NotNull String pathSrc, @NotNull String pathDst) throws IOException;
}
