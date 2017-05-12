import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by demarkok on 04-May-17.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTest {

    public static final String UNASSIGNED_STRING_OPTION = "[unassigned]";


    static class None extends Throwable {
    }

    String ignore() default UNASSIGNED_STRING_OPTION;
    Class <? extends Throwable> expected() default None.class;
}
