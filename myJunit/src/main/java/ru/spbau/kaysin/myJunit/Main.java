package ru.spbau.kaysin.myJunit;

import static java.lang.System.exit;

import ru.spbau.kaysin.myJunit.Exceptions.ClassIsAbstractException;
import ru.spbau.kaysin.myJunit.Exceptions.NoEmptyConstructorException;

/**
 * Created by demarkok on 04-May-17.
 */
public class Main {


    public static void main(String[] args) {
        if (args.length != 1) {
            // TODO
            exit(0);
        }
        String className = args[0];

        Tester tester;

        try {
            tester = new Tester(className);
            tester.test();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoEmptyConstructorException e) {
            e.printStackTrace();
        } catch (ClassIsAbstractException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }



    }
}
