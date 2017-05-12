package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Created by demarkok on 13-May-17.
 */
public class TestWasIgnoredResult extends BaseTestResult {

    private String reason;

    public TestWasIgnoredResult(Class testedClass, Method testedMethod, String reason) {
        super(testedClass, testedMethod);
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
}
