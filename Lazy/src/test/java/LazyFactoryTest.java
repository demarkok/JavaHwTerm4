import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;


public class LazyFactoryTest {
    private static final int NUMBER_OF_THREADS = 100;

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
        final Counter counter = new Counter();
        Lazy<Integer> lazy = LazyFactory.createSynchronizedLazy(() -> {
            counter.inc();
            return 1;
        });

        ArrayList<Thread> threads = new ArrayList<>();
        Runnable task = lazy::get;
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread(task);
            thread.run();
            threads.add(thread);
        }
        for (Thread thread: threads) {
            thread.join();
        }
        assertEquals(new Integer(1), lazy.get());
        assertEquals(1, counter.get());
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