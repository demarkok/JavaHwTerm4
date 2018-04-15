package ru.spbau.kaysin.myJunit.Exceptions;

/**
 * Thrown if an exception was thrown during running method annotated {@link ru.spbau.kaysin.myJunit.Annotations.BeforeClass}.
 */
public class ExceptionInBeforeClassException extends Exception {

    public ExceptionInBeforeClassException(Throwable cause) {
        super(cause);
    }
}
