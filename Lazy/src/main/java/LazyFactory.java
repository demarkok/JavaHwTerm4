import java.util.function.Supplier;

public class LazyFactory {

    public static <T> Lazy<T> createLazy(Supplier<T> supplier) {
        return new LazySupplier<>(supplier);
    }

    public static <T> Lazy<T> createSynchronizedLazy(Supplier<T> supplier) {
        return new synchronizedLazySupplier<>(supplier);
    }

    public static <T> Lazy<T> createLockFreeLazy(Supplier<T> supplier) {
        return new lockFreeLazySupplier<>(supplier);
    }


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


    private static class LazySupplier<T> extends AbstractSupplier<T> implements Lazy<T> {

        private LazySupplier(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        public T get() {
            if (value == marker) {
                value = supplier.get();
            }
            return (T)value;
        }
    }

    private static class synchronizedLazySupplier<T> extends AbstractSupplier<T> implements Lazy<T> {
        private synchronizedLazySupplier(Supplier<T> supplier) {
            super(supplier);
        }

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

    private static class lockFreeLazySupplier<T> implements Lazy<T> {
        private lockFreeLazySupplier(Supplier<T> supplier) {
        }

        @Override
        public T get() {
            return null;
        }
    }
}

