package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * The test result describing the case when the test method threw an exception, but no exception was expected or
 * we expected different one.
 */
public class UnexpectedExceptionFailureResult extends BaseTestResult {

    @NotNull
    private final Class <? extends Throwable> thrownException;

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     * @param thrownException - the unexpected but thrown exception.
     */
    public UnexpectedExceptionFailureResult(@NotNull Class testedClass, @NotNull Method testedMethod, long time,
        @NotNull Class<? extends Throwable> thrownException) {
        super(testedClass, testedMethod, time);
        this.thrownException = thrownException;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "failed. " + "Unexpected exception: " + thrownException.getName() +
            " time: " + time + "ms.";
    }

    /**
     * Returns the contained exception (unexpected but thrown).
     * @return the contained exception (unexpected but thrown).
     */
    @NotNull
    public Class<? extends Throwable> getThrownException() {
        return thrownException;
    }
}
