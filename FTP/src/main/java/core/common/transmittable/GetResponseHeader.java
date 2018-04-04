package core.common.transmittable;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the information part of response to the {@link GetQuery}:
 * name and size of asked file.
 */
public class GetResponseHeader implements FTPPackage {

    @NotNull
    private final String filePath;
    private final long fileSize;

    /**
     * @return size of asked file.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * @return absolute path of asked file.
     */
    @NotNull
    public String getFilePath() {
        return filePath;
    }

    /**
     * Creates the response with {@code filePath} as path of asked file and
     * {@code fileSize} as length of asked file.
     * @param filePath - absolute path of asked file
     * @param fileSize - length of asked file
     */
    public GetResponseHeader(@NotNull String filePath, long fileSize) {
        this.filePath = filePath;
        this.fileSize = fileSize;
    }
}
