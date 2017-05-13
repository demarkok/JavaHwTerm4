package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * Basic abstract class simplifying the implementing of TestResult.
 */
@SuppressWarnings("WeakerAccess")
public abstract class BaseTestResult implements TestResult {

    @NotNull
    protected final Class testedClass;
    @NotNull
    protected final Method testedMethod;
    protected final long time;

    /**
     * Initializes basic fields.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     */
    public BaseTestResult(@NotNull Class testedClass, @NotNull Method testedMethod, long time) {
        this.testedClass = testedClass;
        this.testedMethod = testedMethod;
        this.time = time;
    }

    @Override
    public String getReportMessage() {
        return "Test " + testedMethod.getName() + " in class " + testedClass.getSimpleName() + " ";
    }
}
