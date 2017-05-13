package ru.spbau.kaysin.myJunit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import ru.spbau.kaysin.myJunit.Exceptions.ClassIsAbstractException;
import ru.spbau.kaysin.myJunit.Exceptions.NoEmptyConstructorException;
import ru.spbau.kaysin.myJunit.MyTest.None;
import ru.spbau.kaysin.myJunit.testResults.NoExpectedExceptionFailureResult;
import ru.spbau.kaysin.myJunit.testResults.SuccessfulResult;
import ru.spbau.kaysin.myJunit.testResults.TestResult;
import ru.spbau.kaysin.myJunit.testResults.TestWasIgnoredResult;
import ru.spbau.kaysin.myJunit.testResults.UnexpectedExceptionFailureResult;


/**
 * The tester class which allows to run annotated test methods in a specific class and get the result.
 */
public class Tester {

    private final Class<?> testClass;
    private final Object instance;
    private final Map<Method, MyTest> annotatedMethods;


    public Tester(Class<?> testClass)
        throws ClassNotFoundException, NoEmptyConstructorException,
        ClassIsAbstractException, IllegalAccessException {

        this.testClass = testClass;

        try {
            instance = testClass.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new NoEmptyConstructorException();
        } catch (IllegalAccessException e) {
            throw e;
        } catch (InstantiationException e) {
            throw new RuntimeException();
        } catch (InvocationTargetException e) {
            throw new ClassIsAbstractException();
        }

        annotatedMethods = Arrays.stream(testClass.getMethods())
            .filter(x->x.getAnnotation(MyTest.class) != null)
            .collect(Collectors.toMap(x->x, x->x.getAnnotation(MyTest.class)));
//        System.out.println(Arrays.stream(testClass.getMethods()).map(Method::getName).collect(Collectors.toList()));
//        System.out.println(annotatedMethods.keySet().stream().map(Method::getName).collect(Collectors.toList()));
    }

    public List<TestResult> test() {
        
        List <TestResult> result = new LinkedList<>();
        
        for (Entry<Method, MyTest> entry: annotatedMethods.entrySet()) {
            result.add(testMethod(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    private TestResult testMethod(Method method, MyTest annotation) {
        long start = System.currentTimeMillis();
        if (!annotation.ignore().equals(MyTest.UNASSIGNED_STRING_OPTION)) {
            return new TestWasIgnoredResult(testClass, method, System.currentTimeMillis() - start,
                annotation.ignore());
        }
        try {
            method.invoke(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();  // TODO  
        } catch (InvocationTargetException e) {
            if (annotation.expected().isInstance(e.getCause())) {
                    return new SuccessfulResult(testClass, method, System.currentTimeMillis() - start);
            }
            return new UnexpectedExceptionFailureResult(testClass, method, System.currentTimeMillis() - start,
                e.getCause().getClass());
        }
        
        if (!annotation.expected().equals(None.class)) {
            return new NoExpectedExceptionFailureResult(testClass, method, System.currentTimeMillis() - start, annotation.expected());
        }

        return new SuccessfulResult(testClass, method, System.currentTimeMillis() - start);
    }


}
