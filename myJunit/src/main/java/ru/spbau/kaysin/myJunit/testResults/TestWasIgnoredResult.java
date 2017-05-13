package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * The test result describing the case when the test was annotated as {@link jdk.nashorn.internal.ir.annotations.Ignore}
 * and wasn't tested.
 */
public class TestWasIgnoredResult extends BaseTestResult {

    private final String reason;

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     * @param reason why this test was ignored.
     */
    public TestWasIgnoredResult(Class testedClass, Method testedMethod, long time, String reason) {
        super(testedClass, testedMethod, time);
        this.reason = reason;
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "was ignored." + "Reason: " + reason;
    }

    /**
     * Returns the reason why this test was ignored.
     * @return the reason why this test was ignored.
     */
    public String getReason() {
        return reason;
    }
}
