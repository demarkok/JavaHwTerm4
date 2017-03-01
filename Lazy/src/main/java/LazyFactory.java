import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * The factory of {@link Lazy}.
 */
public final class LazyFactory {

    /**
     * Generates non-concurrent lazy supplier.
     *
     * @param supplier held supplier
     * @param <T> the type of supplied value
     * @return non-concurrent lazy supplier
     */
    public static <T> Lazy<T> createLazy(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    /**
     * Generates concurrent version of lazy supplier.
     *
     * @param supplier held supplier
     * @param <T> the type of supplied value
     * @return concurrent version of lazy supplier
     */
    public static <T> Lazy<T> createSynchronizedLazy(Supplier<T> supplier) {
        return new SynchronizedLazySupplier<>(supplier);
    }

    /**
     * Generates lock free concurrent version of lazy supplier.
     *
     * @param supplier held supplier
     * @param <T> the type of supplied value
     * @return lock free concurrent version of lazy supplier
     */
    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new LockFreeLazySupplier<>(supplier);
    }


    /**
     * Abstract class which represents a common part for all lazy suppliers.
     *
     * @param <T> the type of supplied value
     */
    private static abstract class AbstractSupplier<T> implements Lazy<T> {
        protected static Object marker = new Object();
        volatile protected Object value =  marker;
        protected Supplier<T> supplier;

        private AbstractSupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        abstract public T get();
    }


    /**
     * Class representing simple, non-concurrent lazy supplier.
     *
     * @param <T> the type of supplied value
     */
    private static class LazySupplier<T> extends AbstractSupplier<T> implements Lazy<T> {

        private LazySupplier(Supplier<T> supplier) {
            super(supplier);
        }

        /**
         * In the first invocation uses {@code supplier.get()} to get supplied value and saves it in field {@value}.
         * In further invocations returns saved value.
         *
         * @return supplied value in lazy way
         */
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            if (value == marker) {
                value = supplier.get();
            }
            return (T)value;
        }
    }

    /**
     * Class representing a concurrent version of lazy supplier.
     * It is guaranteed that {@code supplier.get()} will be invoked once.
     *
     *
     * @param <T> the type of supplied value
     */
    private static class SynchronizedLazySupplier<T> extends AbstractSupplier<T> implements Lazy<T> {
        private SynchronizedLazySupplier(Supplier<T> supplier) {
            super(supplier);
        }


        /**
         * In the first invocation uses {@link #supplier#get()} to get supplied value and saves it in field {@value}.
         * In further invocations returns saved value.
         * Uses double-checked locking to provide concurrent behaviour.
         *
         * @return supplied value
         */
        @SuppressWarnings("unchecked")
        @Override
        public T get() {
            if (value == marker) {
                synchronized (this) {
                    if (value == marker) {
                        value = supplier.get();
                    }
                }
            }
            return (T)value;
        }
    }

    /**
     * Class representing a concurrent lock-free version of lazy supplier.
     * It isn't guaranteed that {@link #supplier#get()} will be invoked once.
     * But every {@link #get()} invocation returns the same object.
     *
     * @param <T> the type of supplied value
     */
    private static class LockFreeLazySupplier<T> extends AbstractSupplier<T> implements Lazy<T> {

        private static final AtomicReferenceFieldUpdater <AbstractSupplier, Object> updater =
                AtomicReferenceFieldUpdater.newUpdater(AbstractSupplier.class, Object.class, "value");

        private LockFreeLazySupplier(Supplier<T> supplier) {
            super(supplier);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get() {

            if (value == marker) {
                updater.compareAndSet(this, marker, supplier.get());
            }

            return (T)value;
        }
    }
}

