import java.nio.file.Path;
import java.util.List;

/**
 * Represents interface of ftp client.
 */
public interface ClientInterface {
    public boolean connect(String ip);
    public boolean disconnect();
    public List<String> executeList();
    public byte[] executeGet(Path path);
}
