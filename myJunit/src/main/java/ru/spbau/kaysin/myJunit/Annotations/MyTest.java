package ru.spbau.kaysin.myJunit.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods annotated as {@link MyTest} will be recognized by the testing system as test-case methods.
 * Test can be ignored if the {@code ignore} parameter is assigned with ignoring reason.
 * Test-method is successful if it doesn't throw any exception and the parameter {@code expected} is unassigned, or it throws
 * an exception which is instance of the exception type contained in {@code expected} parameter.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTest {

    /**
     * String representing unassigned string parameter.
     */
    String UNASSIGNED_STRING_OPTION = "[unassigned]";

    /**
     * Class representing unassigned exception type parameter.
     */
    class None extends Throwable {
    }

    /**
     * Ignore the test.
     * @return the reason of ignoring.
     */
    String ignore() default UNASSIGNED_STRING_OPTION;

    /**
     * Expect exception thrown during the test.
     * @return exception type.
     */
    Class <? extends Throwable> expected() default None.class;
}
