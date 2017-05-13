package ru.spbau.kaysin.myJunit;

import static java.lang.System.exit;

import java.util.List;
import ru.spbau.kaysin.myJunit.Exceptions.ClassIsAbstractException;
import ru.spbau.kaysin.myJunit.Exceptions.ExceptionInAfterClassException;
import ru.spbau.kaysin.myJunit.Exceptions.ExceptionInBeforeClassException;
import ru.spbau.kaysin.myJunit.Exceptions.NoEmptyConstructorException;
import ru.spbau.kaysin.myJunit.testResults.TestResult;

/**
 * Basic UI class.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("One argument expected.");
            exit(0);
        }
        String className = args[0];

        try {
            Class classToTest = Class.forName(className);
            Tester tester = new Tester(classToTest);
            List<TestResult> results = tester.test();
            for (TestResult result: results) {
                String message;
                if (result.isSuccessful()) {
                    message = "[OK] ";
                } else {
                    message = "[FAIL] ";
                }
                message += result.getReportMessage();

                System.out.println(message);
            }
        } catch (ClassNotFoundException e) {
            error("class not found.");
        } catch (IllegalAccessException e) {
            error("illegal access.");
        } catch (ClassIsAbstractException e) {
            error("class is abstract.");
        } catch (NoEmptyConstructorException e) {
            error("class hasn't got empty constructor.");
        } catch (ExceptionInBeforeClassException e) {
            error("exception in method annotated as @BeforeClass.");
        } catch (ExceptionInAfterClassException e) {
            error("exception in method annotated as @AfterClass.");
        } catch (InstantiationException e) {
            error("can't instantiate the class for some reason.");
        }
    }

    private static void error(String message) {
        System.out.println("ERROR: " + message);
        exit(0);
    }
}
