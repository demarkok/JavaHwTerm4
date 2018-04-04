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
     * @param rootPath - path of directory where server will start. Client has got access only to
     * subdirectories of {@code rootPath}.
     * @throws ServerAlreadyStartedException if the server has already started.
     */
    void start(UncaughtExceptionHandler exceptionHandler, String rootPath) throws ServerAlreadyStartedException;

    /**
     * Stops the server.
     */
    void stop();
}
