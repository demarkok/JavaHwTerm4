package core.common.transmittable;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a wrapper of exception which can be sent and received.
 * Sent from server to client if the client causes an exception on the server,
 * e.g. asks for incorrect path.
 */
public class ErrorResponse implements FTPPackage {

    @NotNull
    private final IOException exception;

    /**
     * Creates the wrapper of the exception.
     * @param exception - wrapped exception.
     */
    public ErrorResponse(@NotNull IOException exception) {
        this.exception = exception;
    }

    /**
     * @return contained exception.
     */
    @NotNull
    public IOException getException() {
        return exception;
    }
}
