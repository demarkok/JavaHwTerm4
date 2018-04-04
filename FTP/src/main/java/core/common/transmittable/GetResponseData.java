package core.common.transmittable;

import org.jetbrains.annotations.NotNull;

/**
 * Represents the data part of response to the {@link GetQuery}:
 * Chunk of asked file.
 */
public class GetResponseData implements FTPPackage {
    @NotNull
    private final byte[] data;

    /**
     * Creates the response with {@code data} as chunk of file.
     * @param data - contained file chunk
     */
    public GetResponseData(@NotNull byte[] data) {
        this.data = data;
    }

    /**
     * @return contained chunk.
     */
    @NotNull
    public byte[] getData() {
        return data;
    }
}
