package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Created by demarkok on 12-May-17.
 */
public abstract class BaseTestResult implements TestResult{
    private final Class testedClass;
    private final Method testedMethod;
    private final long time;

    public BaseTestResult(Class testedClass, Method testedMethod, long time) {
        this.testedClass = testedClass;
        this.testedMethod = testedMethod;
        this.time = time;
    }

    protected Class getTestedClass() {
        return testedClass;
    }

    protected Method getTestedMethod() {
        return testedMethod;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String getReportMessage() {
        return "Test " + getTestedMethod().getName() + " in class " + getTestedClass().getName() + " ";
    }
}
