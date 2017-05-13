
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import classesToTest.ClassWithSuccessfulTestAndIgnoredTest;
import classesToTest.ClassWithTestNotThrowingExpectedException;
import classesToTest.ClassWithUnexpectedExceptionTestsAndSuccessfulTest;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import ru.spbau.kaysin.myJunit.Exceptions.ClassIsAbstractException;
import ru.spbau.kaysin.myJunit.Exceptions.NoEmptyConstructorException;
import ru.spbau.kaysin.myJunit.Tester;
import ru.spbau.kaysin.myJunit.testResults.NoExpectedExceptionFailureResult;
import ru.spbau.kaysin.myJunit.testResults.SuccessfulResult;
import ru.spbau.kaysin.myJunit.testResults.TestResult;
import ru.spbau.kaysin.myJunit.testResults.TestWasIgnoredResult;
import ru.spbau.kaysin.myJunit.testResults.UnexpectedExceptionFailureResult;

/**
 * Basic workflow test
 */
public class MainTest {

    @Test
    public void successfulAndIgnoredTest()
        throws ClassNotFoundException, IllegalAccessException, ClassIsAbstractException, NoEmptyConstructorException {

        Class classToTest = Class.forName("classesToTest.ClassWithSuccessfulTestAndIgnoredTest");

        Tester tester = new Tester(classToTest);
        List<TestResult> result = tester.test();

        assertThat(result, containsInAnyOrder(successfulResult(),
            testWasIgnoredResult(ClassWithSuccessfulTestAndIgnoredTest.REASON)));
    }

    @Test
    public void classWithoutTestsTest()
        throws ClassNotFoundException, IllegalAccessException, ClassIsAbstractException, NoEmptyConstructorException {

        Class classToTest = Class.forName("classesToTest.ClassWithoutTests");

        Tester tester = new Tester(classToTest);
        List<TestResult> result = tester.test();

        assertThat(result, is(empty()));
    }


    @Test
    public void unexpectedAndExpectedExceptionsTest()
        throws ClassNotFoundException, IllegalAccessException, ClassIsAbstractException, NoEmptyConstructorException {
        Class classToTest = Class.forName(
            "classesToTest.ClassWithUnexpectedExceptionTestsAndSuccessfulTest");

        Tester tester = new Tester(classToTest);
        List<TestResult> result = tester.test();

        System.out.println(result);

        assertThat(result, containsInAnyOrder(
            unexpectedExceptionFailureResult(ClassWithUnexpectedExceptionTestsAndSuccessfulTest.THROWN_EXCEPTION.getClass()),
            unexpectedExceptionFailureResult(ClassWithUnexpectedExceptionTestsAndSuccessfulTest.THROWN_EXCEPTION.getClass()),
            successfulResult()));
    }

    @Test
    public void noExpectedExceptionTest()
        throws ClassNotFoundException, IllegalAccessException, ClassIsAbstractException, NoEmptyConstructorException {
        Class classToTest = Class.forName(
            "classesToTest.ClassWithTestNotThrowingExpectedException");

        Tester tester = new Tester(classToTest);
        List<TestResult> result = tester.test();

        System.out.println(result);

        assertThat(result, contains(
            noExpectedExceptionFailureResult(ClassWithTestNotThrowingExpectedException.EXPECTED_EXCEPTION_CLASS)));
    }


    private UnexpectedExceptionFailureResultMatcher unexpectedExceptionFailureResult(
        Class<? extends Throwable> thrownException) {
        return new UnexpectedExceptionFailureResultMatcher(thrownException);
    }
    private class UnexpectedExceptionFailureResultMatcher extends BaseMatcher<TestResult> {

        private final Class<? extends Throwable> thrownException;

        private UnexpectedExceptionFailureResultMatcher(
            Class<? extends Throwable> thrownException) {
            this.thrownException = thrownException;
        }

        @Override
        public boolean matches(Object item) {
            return (item instanceof UnexpectedExceptionFailureResult) &&
                ((UnexpectedExceptionFailureResult) item).getThrownException().equals(thrownException);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(UnexpectedExceptionFailureResult.class)
                .appendText(" with ").appendValue(thrownException).appendText("as thrown exception");
        }
    }


    private TestWasIgnoredResultMatcher testWasIgnoredResult(String reason) {
        return new TestWasIgnoredResultMatcher(reason);
    }
    private class TestWasIgnoredResultMatcher extends BaseMatcher<TestResult> {
        private final String reason;

        private TestWasIgnoredResultMatcher(String reason) {
            this.reason = reason;
        }

        @Override
        public boolean matches(Object item) {
            return (item instanceof TestWasIgnoredResult) &&
                ((TestWasIgnoredResult) item).getReason().equals(reason);
        }

        @Override
        public void describeTo(Description description) {

        }
    }


    private NoExpectedExceptionFailureResultMatcher noExpectedExceptionFailureResult(
        Class<? extends Throwable> expectedException) {
        return new NoExpectedExceptionFailureResultMatcher(expectedException);
    }
    private class NoExpectedExceptionFailureResultMatcher extends  BaseMatcher<TestResult> {

        private final Class<? extends Throwable> expectedException;

        public NoExpectedExceptionFailureResultMatcher(
            Class<? extends Throwable> expectedException) {
            this.expectedException = expectedException;
        }

        @Override
        public boolean matches(Object item) {
            return item instanceof NoExpectedExceptionFailureResult
                && ((NoExpectedExceptionFailureResult) item).getExpectedException()
                .equals(expectedException);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(NoExpectedExceptionFailureResult.class).appendText(" with ")
                .appendValue(expectedException).appendText(" as expected exception.");

        }
    }

    private SuccessfulResultMatcher successfulResult() {
        return new SuccessfulResultMatcher();
    }
    private class SuccessfulResultMatcher extends BaseMatcher<TestResult> {
        @Override
        public boolean matches(Object item) {
            return item instanceof SuccessfulResult;
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(SuccessfulResult.class);
        }
    }

}