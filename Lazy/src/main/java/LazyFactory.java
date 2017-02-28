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

    private static class LazySupplier<T> implements Lazy<T> {
        private static Object marker = new Object();
        private Object value =  marker;
        private Supplier<T> supplier;

        private LazySupplier(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (value == marker) {
                value = supplier.get();
            }
            return (T)value;
        }
    }

    private static class synchronizedLazySupplier<T> implements Lazy<T> {
        private synchronizedLazySupplier(Supplier<T> supplier) {
        }

        @Override
        public T get() {
            return null;
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

