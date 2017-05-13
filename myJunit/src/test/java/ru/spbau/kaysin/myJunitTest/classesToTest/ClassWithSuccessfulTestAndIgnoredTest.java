package ru.spbau.kaysin.myJunitTest.classesToTest;

import ru.spbau.kaysin.myJunit.Annotations.MyTest;

/**
 * Class to test
 */
public class ClassWithSuccessfulTestAndIgnoredTest {

    public static final String REASON = "Because I can";

    /**
     * Successful test
     */
    @MyTest
    public void success() {
    }

    /**
     * Test which should be ignored.
     */
    @MyTest(ignore = REASON)
    public void ignored() {
        throw new NullPointerException();
    }

}
