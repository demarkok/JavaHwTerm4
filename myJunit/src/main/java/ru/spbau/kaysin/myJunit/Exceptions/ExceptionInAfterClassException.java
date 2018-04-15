package ru.spbau.kaysin.myJunit.Exceptions;

/**
 * Thrown if an exception was thrown during running method annotated {@link ru.spbau.kaysin.myJunit.Annotations.AfterClass}.
 */
public class ExceptionInAfterClassException extends Exception {

    public ExceptionInAfterClassException(Throwable cause) {
        super(cause);
    }
}
