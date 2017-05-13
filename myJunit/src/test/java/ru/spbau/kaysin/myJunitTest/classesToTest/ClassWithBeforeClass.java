package ru.spbau.kaysin.myJunitTest.classesToTest;

import org.junit.Test;
import ru.spbau.kaysin.myJunit.Annotations.AfterClass;
import ru.spbau.kaysin.myJunit.Annotations.BeforeClass;
import ru.spbau.kaysin.myJunit.Annotations.MyTest;

/**
 * Created by demarkok on 13-May-17.
 */
public class ClassWithBeforeClass {

    private int x = 0;

    @BeforeClass
    public void beforeClass() {
        x++;
    }

    @MyTest
    public void test1() {
        assert x == 1;
    }

    @MyTest
    public void test2() {
        assert x == 1;
    }
}
