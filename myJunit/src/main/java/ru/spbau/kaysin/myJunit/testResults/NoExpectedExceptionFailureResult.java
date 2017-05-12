package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Created by demarkok on 13-May-17.
 */
public class NoExpectedExceptionFailureResult extends BaseTestResult {

    private final Class<? extends Throwable> expectedException;

    public NoExpectedExceptionFailureResult(Class testedClass, Method testedMethod,
            Class<? extends Throwable> expectedException) {
        super(testedClass, testedMethod);
        this.expectedException = expectedException;
    }

    @Override
    public boolean isSuccessful() {
        return false;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "failed. " + expectedException.getName() + " expected.";
    }
}
