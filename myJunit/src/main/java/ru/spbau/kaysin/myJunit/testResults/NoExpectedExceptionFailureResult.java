package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * The test result describing the case when the test method didn't throw any exception, but it was expected.
 */
public class NoExpectedExceptionFailureResult extends BaseTestResult {

    private final Class<? extends Throwable> expectedException;

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     * @param expectedException an exception which was expected but wasn't thrown.
     */
    public NoExpectedExceptionFailureResult(Class testedClass, Method testedMethod, long time,
            Class<? extends Throwable> expectedException) {
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
    public Class<? extends Throwable> getExpectedException() {
        return expectedException;
    }
}
