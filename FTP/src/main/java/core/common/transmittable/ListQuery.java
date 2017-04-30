package core.common.transmittable;

import org.jetbrains.annotations.NotNull;

/**
 * Represents list request which the client sends to get list of files and directories.
 * The response is sent via {@link ListResponse}.
 */
public class ListQuery implements FTPPackage {

    /**
     * @return contained path.
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Creates the request of files and folders contained on the server in {@code path}
     * @param path - absolute path contained in the request.
     */
    public ListQuery(@NotNull String path) {
        this.path = path;
    }

    @NotNull
    private final String path;
}
