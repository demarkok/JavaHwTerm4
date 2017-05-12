import Exceptions.ClassIsAbstractException;
import Exceptions.NoEmptyConstructorException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Created by demarkok on 04-May-17.
 */
public class Tester {

    private final Class<?> testClass;
    private final Object instance;
    private final Map<Method, MyTest> annotatedMethods;

    public Tester(String testClassName)
        throws ClassNotFoundException, NoEmptyConstructorException,
        ClassIsAbstractException, IllegalAccessException {

        testClass = Class.forName(testClassName);
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

    public List<MethodTestResult> test() {
        List <MethodTestResult> result = new LinkedList<>();
        for (Entry<Method, MyTest> entry: annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            MyTest annotation = entry.getValue();

            boolean isSuccess = true;
            if (annotation.ignore().equals(MyTest.UNASSIGNED_STRING_OPTION)) {
                continue;
            }
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                isSuccess = false;
            }
            result.add(new MethodTestResult(method.getName(), isSuccess));
        }
        return result;
    }

    static class MethodTestResult {
        private String methodName;
        private final boolean success;

        public boolean isSuccess() {
            return success;
        }

        public MethodTestResult(String methodName, boolean success) {
            this.methodName = methodName;
            this.success = success;
        }
    }

}
