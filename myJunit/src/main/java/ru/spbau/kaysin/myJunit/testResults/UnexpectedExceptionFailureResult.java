package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Created by demarkok on 13-May-17.
 */
public class UnexpectedExceptionFailureResult extends BaseTestResult {

    private final Class <? extends Throwable> thrownException;

    public UnexpectedExceptionFailureResult(Class testedClass, Method testedMethod, long time,
        Class<? extends Throwable> thrownException) {
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
            " time: " + getTime() + "ms.";
    }

    public Class<? extends Throwable> getThrownException() {
        return thrownException;
    }
}
