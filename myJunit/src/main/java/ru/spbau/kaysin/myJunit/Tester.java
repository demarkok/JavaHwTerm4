package ru.spbau.kaysin.myJunit;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import ru.spbau.kaysin.myJunit.Annotations.After;
import ru.spbau.kaysin.myJunit.Annotations.AfterClass;
import ru.spbau.kaysin.myJunit.Annotations.Before;
import ru.spbau.kaysin.myJunit.Annotations.BeforeClass;
import ru.spbau.kaysin.myJunit.Annotations.MyTest;
import ru.spbau.kaysin.myJunit.Exceptions.ClassIsAbstractException;
import ru.spbau.kaysin.myJunit.Exceptions.ExceptionInAfterClassException;
import ru.spbau.kaysin.myJunit.Exceptions.ExceptionInBeforeClassException;
import ru.spbau.kaysin.myJunit.Exceptions.NoEmptyConstructorException;
import ru.spbau.kaysin.myJunit.Annotations.MyTest.None;
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
    private final Map<Method, MyTest> testCaseMethods;
    private final List<Method> beforeMethods;
    private final List<Method> afterMethods;
    private final List<Method> beforeClassMethods;
    private final List<Method> afterClassMethods;



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

        testCaseMethods = Arrays.stream(testClass.getMethods())
            .filter(x->x.getAnnotation(MyTest.class) != null)
            .collect(Collectors.toMap(x->x, x->x.getAnnotation(MyTest.class)));

        beforeMethods = getAnnotatedMethods(testClass, Before.class);
        afterMethods = getAnnotatedMethods(testClass, After.class);
        beforeClassMethods = getAnnotatedMethods(testClass, BeforeClass.class);
        afterClassMethods = getAnnotatedMethods(testClass, AfterClass.class);

//        System.out.println(Arrays.stream(testClass.getMethods()).map(Method::getName).collect(Collectors.toList()));
//        System.out.println(testCaseMethods.keySet().stream().map(Method::getName).collect(Collectors.toList()));
    }

    public List<TestResult> test()
        throws ExceptionInBeforeClassException, ExceptionInAfterClassException {

        try {
            runListOfMethods(beforeClassMethods);
        } catch (InvocationTargetException e) {
            throw new ExceptionInBeforeClassException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // TODO
        }

        List <TestResult> result = new LinkedList<>();
        
        for (Entry<Method, MyTest> entry: testCaseMethods.entrySet()) {
            result.add(testMethod(entry.getKey(), entry.getValue()));
        }

        try {
            runListOfMethods(afterClassMethods);
        } catch (InvocationTargetException e) {
            throw new ExceptionInAfterClassException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // TODO
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
            runListOfMethods(beforeMethods);
            method.invoke(instance);
            runListOfMethods(afterMethods);
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

    private void runListOfMethods(List<Method> methods)
        throws InvocationTargetException, IllegalAccessException {
        for (Method method: methods) {
            method.invoke(instance);
        }
    }

    private List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
       return Arrays.stream(clazz.getMethods())
            .filter(x->x.getAnnotation(annotationClass) != null)
            .collect(Collectors.toList());
    }
}
