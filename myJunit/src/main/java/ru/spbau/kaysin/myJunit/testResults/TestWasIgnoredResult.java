package ru.spbau.kaysin.myJunit.testResults;

import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;

/**
 * The test result describing the case when the test was annotated as {@link jdk.nashorn.internal.ir.annotations.Ignore}
 * and wasn't tested.
 */
public class TestWasIgnoredResult extends BaseTestResult {

    @NotNull
    private final String reason;

    /**
     * Creates a new instance.
     * @param testedClass class which was tested
     * @param testedMethod test-case method
     * @param time testing time in mils.
     * @param reason why this test was ignored.
     */
    public TestWasIgnoredResult(@NotNull Class testedClass, @NotNull Method testedMethod, long time, @NotNull String reason) {
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
    @NotNull
    public String getReason() {
        return reason;
    }
}
