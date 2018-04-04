package GUI.client;

import java.nio.file.Path;
import java.util.Set;

/**
 * File system explorer interface.
 */
public interface Explorer {

    /**
     * Explores files in the directory.
     * @param path path to directory
     * @return list of files placed in {@code path}.
     */
    Set<String> getFiles(Path path);

    /**
     * Explores directories in the directory.
     * @param path path to directory
     * @return list of directories placed in {@code path}.
     */
    Set<String> getDirectories(Path path);

}
