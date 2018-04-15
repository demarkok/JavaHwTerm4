package ru.spbau.kaysin.myJunit.testResults;

/**
 * Interface representing the result of test method.
 */
public interface TestResult {

    /**
     * Returns test result.
     * @return true if the test succeed, false - otherwise.
     */
    boolean isSuccessful();

    /**
     * Returns the test result in text form.
     * @return string representing text form of the test result.
     */
    default String getReportMessage() {
        if (isSuccessful()) {
            return "Test passed.";
        } else {
            return "Test failed.";
        }
    }
}
