import org.junit.Test;

import java.util.ArrayList;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertSame;


public class LazyFactoryTest {
    private static final int NUMBER_OF_THREADS = 100;

    @Test
    public void createLazy_Check_uniqueness_of_get_invocation() throws Exception {
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
    public void createSynchronizedLazy_Check_uniqueness_of_get_invocation() throws Exception {
        final Counter counter = new Counter();
        Lazy<Integer> lazy = LazyFactory.createSynchronizedLazy(() -> {
            counter.inc();
            return 1;
        });
        assertEquals(new Integer(1), lazy.get());
        assertEquals(new Integer(1), lazy.get());
        assertEquals(1, counter.get());
    }


    @Test
    public void createSynchronizedLazy_simple_concurrent_test() throws Exception {
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
    public void createLockFreeLazy_simple_concurrent_test() throws Exception {
        final Counter counter = new Counter();
        final Integer number = 10000;
        Lazy<Integer> lazy = LazyFactory.createLockFreeLazy(() -> {
            counter.inc();
            return number;
        });

        ArrayList<Thread> threads = new ArrayList<>();
        Runnable task = () -> assertSame(number, lazy.get());
        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            Thread thread = new Thread(task);
            thread.run();
            threads.add(thread);
        }
        for (Thread thread: threads) {
            thread.join();
        }
    }


    private static class Counter {
        private int x = 0;
        void inc() {
            x++;
        }
        int get() {
            return x;
        }
    }
}