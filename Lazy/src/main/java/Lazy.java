/**
 * Provides a lazy calculation.
 * The first invocation of {@link #get()} returns the result supplied by the supplier.
 * Further invocations return the same object.
 * @param <T> supplied type
 */
public interface Lazy <T> {
    T get();
}
