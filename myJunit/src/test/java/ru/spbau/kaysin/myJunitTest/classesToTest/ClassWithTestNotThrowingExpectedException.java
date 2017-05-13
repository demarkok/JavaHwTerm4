package ru.spbau.kaysin.myJunitTest.classesToTest;

import java.util.NoSuchElementException;
import ru.spbau.kaysin.myJunit.Annotations.MyTest;

/**
 * Class to test.
 */
public class ClassWithTestNotThrowingExpectedException {

    public static final Class<? extends Throwable> EXPECTED_EXCEPTION_CLASS = NoSuchElementException.class;

    /**
     * Test which should fail due to it doesn't throw expected exception.
     * @return 42
     */
    @MyTest(expected = NoSuchElementException.class)
    public int foo() {
        return 42;
    }

}
