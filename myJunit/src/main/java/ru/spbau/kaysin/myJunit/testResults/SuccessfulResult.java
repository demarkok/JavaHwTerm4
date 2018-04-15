package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * The test result describing the case when it successfully passed.
 */
public class SuccessfulResult extends BaseTestResult {

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     */
    public SuccessfulResult(@NotNull Class testedClass, @NotNull Method testedMethod, long time) {
        super(testedClass, testedMethod, time);
    }

    @Override
    public boolean isSuccessful() {
        return true;
    }

    @Override
    public String getReportMessage() {
        return super.getReportMessage() + "successfully passed." +
            " time: " + time + "ms.";
    }
}
