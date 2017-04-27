package core.common.transmittable;

import org.jetbrains.annotations.NotNull;

/**
 * Represents get request which the client sends to download the file.
 */
public class GetQuery implements FTPPackage {

    /**
     * @return contained file path
     */
    @NotNull
    public String getPath() {
        return path;
    }

    /**
     * Creates the request of file contained on the server in {@param path}
     */
    public GetQuery(@NotNull String path) {
        this.path = path;
    }

    @NotNull
    private final String path;
}
