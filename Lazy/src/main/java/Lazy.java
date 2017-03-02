import org.jetbrains.annotations.Nullable;

/**
 * Provides a lazy calculation.
 * The first invocation of {@link #get()} returns the result supplied by the supplier.
 * Further invocations return the same object.
 * @param <T> supplied type
 */

@FunctionalInterface
public interface Lazy <T> {

    @Nullable
    T get();
}
