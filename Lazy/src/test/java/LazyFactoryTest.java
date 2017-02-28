import org.junit.Test;

import static org.junit.Assert.*;


public class LazyFactoryTest {
    @Test
    public void createLazy() throws Exception {
        final Counter counter = new Counter();
        Lazy<Integer> lazy = LazyFactory.createLazy(() -> {
            counter.inc();
            return 1;
        });
        assertEquals(new Integer(1), lazy.get());
        assertEquals(new Integer(1), lazy.get());
        assertEquals(1, counter.get());
    }

    @Test
    public void createSynchronizedLazy() throws Exception {

    }

    @Test
    public void createLockFreeLazy() throws Exception {

    }


    private static class Counter {
        private int x = 0;
        private void inc() {
            x++;
        }
        private int get() {
            return x;
        }
    }
}