package ru.spbau.kaysin.myJunitTest.classesToTest;

import ru.spbau.kaysin.myJunit.Annotations.AfterClass;

/**
 * Created by demarkok on 13-May-17.
 */
public class ClassWithExceptionInAfterClass {

    @AfterClass
    public void afterClass() {
        throw new RuntimeException();
    }

}
