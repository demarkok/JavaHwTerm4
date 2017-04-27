package core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableSet;
import core.client.Client;
import core.client.ClientInterface;
import core.common.exceptions.ServerAlreadyStartedException;
import core.server.Server;
import core.server.ServerInterface;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.channels.NotYetConnectedException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Basic workflow test.
 */
public class CommonTest {
    @Rule
    public TemporaryFolder serverFolder = new TemporaryFolder();
    @Rule
    public TemporaryFolder clientFolder = new TemporaryFolder();

    private final UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            fail();
        }
    };

    @Test
    public void startStopServerTest() throws ServerAlreadyStartedException {
        ServerInterface server = new Server();
        server.start(handler);
        server.stop();
        server.start(handler);
        server.stop();
    }

    @Test(expected = ServerAlreadyStartedException.class)
    public void startStartServerTest() throws ServerAlreadyStartedException {
        ServerInterface server = new Server();
        server.start(handler);
        server.start(handler);
    }

    @Test
    public void connectDisconnectTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler);
        assertTrue(client.connect("localhost"));
        client.disconnect();
        assertTrue(client.connect("localhost"));
        client.disconnect();
    }

    @Test(expected = NotYetConnectedException.class)
    public void connectDisconnectDisconnectTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler);
        assertTrue(client.connect("localhost"));
        client.disconnect();
        client.disconnect();
    }

    @Test
    public void listListTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler);
        assertTrue(client.connect("localhost"));
        File file = serverFolder.newFile("testFile");
        File folder = serverFolder.newFolder("testFolder");
        List<String> result = client.executeList(serverFolder.getRoot().getPath());
        assertEquals(new HashSet<String>(result),
            ImmutableSet.of(file.getName(), folder.getName() + "/"));
        result = client.executeList(serverFolder.getRoot().getPath());
        assertEquals(new HashSet<String>(result),
            ImmutableSet.of(file.getName(), folder.getName() + "/"));
    }

    @Test
    public void getTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler);
        assertTrue(client.connect("localhost"));

        File inputFile = serverFolder.newFile("testFile");
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(inputFile.toPath(), data);

        File outputFile = clientFolder.newFile("outputFile");

        client.executeGet(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        byte[] result = Files.readAllBytes(outputFile.toPath());
        assertArrayEquals(data, result);
    }

    @Test
    public void multipleClientListTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        server.start(handler);
        File file = serverFolder.newFile("testFile");
        File folder = serverFolder.newFolder("testFolder");

        List <Thread> threads = new LinkedList<>();

        int n = 100;

        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ClientInterface client = new Client();
                    try {
                        assertTrue(client.connect("localhost"));
                        List<String> result = client.executeList(serverFolder.getRoot().getPath());
                        assertEquals(new HashSet<String>(result),
                            ImmutableSet.of(file.getName(), folder.getName() + "/"));
                        client.disconnect();

                    } catch (IOException e) {
                        fail();
                    }
                }
            });
            threads.add(thread);
            thread.run();
        }
        for (int i = 0; i < n; i++) {
            threads.get(i).join();
        }
    }


    @Test
    public void multipleClientGetTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        server.start(handler);

        File inputFile = serverFolder.newFile("testFile");
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(inputFile.toPath(), data);

        List <Thread> threads = new LinkedList<>();

        int n = 20;

        for (int i = 0; i < n; i++) {
            int index = i;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ClientInterface client = new Client();
                    try {
                        assertTrue(client.connect("localhost"));
                        File outputFile = clientFolder.newFile("output_" + String.valueOf(index));

                        client.executeGet(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
                        byte[] result = Files.readAllBytes(outputFile.toPath());
                        assertArrayEquals(data, result);

                        client.disconnect();

                    } catch (IOException e) {
                        e.printStackTrace();
                        fail();
                    }
                }
            });
            threads.add(thread);
            thread.run();
        }
        for (int i = 0; i < n; i++) {
            threads.get(i).join();
        }
    }
}