package core;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    private final UncaughtExceptionHandler handler = (t, e) -> {
        e.printStackTrace();
        fail();
    };

    @Test
    public void startStopServerTest() throws ServerAlreadyStartedException {
        ServerInterface server = new Server();
        server.start(handler, serverFolder.getRoot().getPath());
        server.stop();
        server.start(handler, serverFolder.getRoot().getPath());
        server.stop();
    }

//    @Test(expected = ServerAlreadyStartedException.class)
    @Test
    public void startStartServerTest() throws ServerAlreadyStartedException {
        ServerInterface server = new Server();
        server.start(handler, serverFolder.getRoot().getPath());
        try {
            server.start(handler, serverFolder.getRoot().getPath());
            fail("ServerAlreadyStarted exception expected");
        } catch (ServerAlreadyStartedException e) {
            server.stop();
        }
    }

    @Test
    public void connectDisconnectTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler, serverFolder.getRoot().getPath());
        assertTrue(client.connect("localhost"));
        client.disconnect();
        assertTrue(client.connect("localhost"));
        client.disconnect();
        sleep(200);
        server.stop();
    }

    @Test(expected = NotYetConnectedException.class)
    public void connectDisconnectDisconnectTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler, serverFolder.getRoot().getPath());
        assertTrue(client.connect("localhost"));
        client.disconnect();
        client.disconnect();
        server.stop();
    }

    @Test
    public void listListTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler, serverFolder.getRoot().getPath());
        assertTrue(client.connect("localhost"));
        File file = serverFolder.newFile("testFile");
        File folder = serverFolder.newFolder("testFolder");
        Map<String,Boolean> result = client.executeList(".");
        assertEquals(ImmutableMap.of(file.getName(), false, folder.getName(), true).entrySet(),
            result.entrySet());
        result = client.executeList(".");
        assertEquals(ImmutableMap.of(file.getName(), false, folder.getName(), true).entrySet(),
            result.entrySet());
        server.stop();
    }

    @Test
    public void getTest() throws ServerAlreadyStartedException, IOException {
        ServerInterface server = new Server();
        ClientInterface client = new Client();
        server.start(handler, serverFolder.getRoot().getPath());
        assertTrue(client.connect("localhost"));

        File inputFile = serverFolder.newFile("testFile");
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(inputFile.toPath(), data);

        File outputFile = clientFolder.newFile("outputFile");

        client.executeGet(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
        byte[] result = Files.readAllBytes(outputFile.toPath());
        assertArrayEquals(data, result);
        server.stop();
    }


    @Test
    public void multipleClientListTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        server.start(handler, serverFolder.getRoot().getPath());
        File file = serverFolder.newFile("testFile");
        File folder = serverFolder.newFolder("testFolder");

        List <Thread> threads = new LinkedList<>();

        int n = 100;

        for (int i = 0; i < n; i++) {
            Thread thread = new Thread(() -> {
                ClientInterface client = new Client();
                try {
                    assertTrue(client.connect("localhost"));
                    Map<String,Boolean> result = client.executeList(serverFolder.getRoot().getPath());
                    assertEquals(ImmutableMap.of(file.getName(), false, folder.getName(), true).entrySet(),
                        result.entrySet());
                    client.disconnect();

                } catch (IOException e) {
                    fail();
                }
            });
            threads.add(thread);
            thread.run();
        }
        for (int i = 0; i < n; i++) {
            threads.get(i).join();
        }
        server.stop();
    }


    @Test
    public void multipleClientGetTest()
        throws ServerAlreadyStartedException, IOException, InterruptedException {
        ServerInterface server = new Server();
        server.start(handler, serverFolder.getRoot().getPath());

        File inputFile = serverFolder.newFile("testFile");
        byte[] data = new byte[10000];
        new Random().nextBytes(data);
        Files.write(inputFile.toPath(), data);

        List <Thread> threads = new LinkedList<>();

        int n = 20;

        for (int i = 0; i < n; i++) {
            int index = i;
            Thread thread = new Thread(() -> {
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
            });
            threads.add(thread);
            thread.run();
        }
        for (int i = 0; i < n; i++) {
            threads.get(i).join();
        }
        server.stop();
    }

}