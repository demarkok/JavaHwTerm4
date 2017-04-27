package core.server;

import core.common.exceptions.ServerAlreadyStartedException;
import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Represents interface of ftp server.
 */
public interface ServerInterface {

    /**
     * Starts the server.
     * @param exceptionHandler the handler of exceptions thrown in server thread.
     * @throws ServerAlreadyStartedException if the server has already started.
     */
    void start(UncaughtExceptionHandler exceptionHandler) throws ServerAlreadyStartedException;

    /**
     * Stops the server.
     */
    void stop();
}
