package ru.spbau.kaysin.myJunitTest.classesToTest;

import ru.spbau.kaysin.myJunit.Annotations.After;
import ru.spbau.kaysin.myJunit.Annotations.Before;
import ru.spbau.kaysin.myJunit.Annotations.MyTest;

/**
 * Created by demarkok on 13-May-17.
 */
public class ClassWithBeforeAndAfter {

    private boolean flag;

    @Before
    public void before() {
        flag = true;
    }

    @MyTest
    public void test1() {
        assert flag;
        flag = false;
    }

    @MyTest
    public void test2() {
        assert flag;
        flag = false;
    }


    @After
    public void after() {
        assert !flag;
    }

}
