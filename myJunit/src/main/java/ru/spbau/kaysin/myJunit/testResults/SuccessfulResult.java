package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Created by demarkok on 13-May-17.
 */
public class SuccessfulResult extends BaseTestResult {

    public SuccessfulResult(Class testedClass, Method testedMethod) {
        super(testedClass, testedMethod);
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "successfully passed.";
    }
}
