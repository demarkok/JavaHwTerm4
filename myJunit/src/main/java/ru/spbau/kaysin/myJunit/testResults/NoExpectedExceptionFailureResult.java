package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * The test result describing the case when the test method didn't throw any exception, but it was expected.
 */
public class NoExpectedExceptionFailureResult extends BaseTestResult {

    @NotNull
    private final Class<? extends Throwable> expectedException;

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     * @param expectedException an exception which was expected but wasn't thrown.
     */
    public NoExpectedExceptionFailureResult(@NotNull Class testedClass, @NotNull Method testedMethod, long time,
            @NotNull Class<? extends Throwable> expectedException) {
        super(testedClass, testedMethod, time);
        this.expectedException = expectedException;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "failed. " + expectedException.getName() + " expected." +
            " time: " + time + "ms.";
    }

    /**
     * Returns contained exception which which was expected but wasn't thrown.
     * @return contained exception which which was expected but wasn't thrown.
     */
    @NotNull
    public Class<? extends Throwable> getExpectedException() {
        return expectedException;
    }
}
