package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;

/**
 * Basic abstract class simplifying the implementing of TestResult.
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseTestResult implements TestResult{
    protected final Class testedClass;
    protected final Method testedMethod;
    protected final long time;

    /**
     * Initializes basic fields.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     */
    public BaseTestResult(Class testedClass, Method testedMethod, long time) {
        this.testedClass = testedClass;
        this.testedMethod = testedMethod;
        this.time = time;
    }

    @Override
    public String getReportMessage() {
        return "Test " + testedMethod.getName() + " in class " + testedClass.getSimpleName() + " ";
    }
}
