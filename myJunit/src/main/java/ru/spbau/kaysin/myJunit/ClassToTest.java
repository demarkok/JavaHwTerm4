package ru.spbau.kaysin.myJunit;

import ru.spbau.kaysin.myJunit.Annotations.MyTest;

/**
 * Created by demarkok on 13-May-17.
 */
public class ClassToTest {

    @MyTest
    public void foo() {
        int z = 239;
        for (int i = 0; i < 100000000; i++) {
            z += z * z + 239;
        }
        System.out.println(z);
    }
    @MyTest
    public void bar() {
        int z = 239;
        for (int i = 0; i < 100000000; i++) {
            z += z * z + 239;
        }
        System.out.println(z);
        throw new RuntimeException();
    }

}
