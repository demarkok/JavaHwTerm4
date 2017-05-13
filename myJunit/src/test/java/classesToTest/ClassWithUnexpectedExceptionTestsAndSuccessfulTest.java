package classesToTest;

import java.util.NoSuchElementException;
import ru.spbau.kaysin.myJunit.MyTest;

/**
 * Class to test.
 */
public class ClassWithUnexpectedExceptionTestsAndSuccessfulTest {

    public static final Throwable THROWN_EXCEPTION = new IndexOutOfBoundsException();

    public static final Class<? extends Throwable> EXPECTED_EXCEPTION_CLASS = NoSuchElementException.class;


    /**
     * Test which should fail due to it throws the exception, but no exception expected.
     * @throws Throwable exception
     */
    @MyTest
    public void foo() throws Throwable {
        try {
            throw THROWN_EXCEPTION;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test which should fail due to it throws an exception which doesn't equal expected one.
     * @throws Throwable exception
     */
    @MyTest(expected = NoSuchElementException.class)
    public void bar() throws Throwable {
        try {
            throw THROWN_EXCEPTION;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Successful test, throwing expected exception.
     */
    @MyTest(expected = NoSuchElementException.class)
    public void baz() {
        throw new NoSuchElementException();
    }
}
