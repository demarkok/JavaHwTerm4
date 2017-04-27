package core.server;

import core.common.Connection;
import core.common.PackageProcessor;
import core.common.exceptions.ServerAlreadyStartedException;
import core.common.transmittable.ErrorResponse;
import core.common.transmittable.FTPPackage;
import core.common.transmittable.GetQuery;
import core.common.transmittable.GetResponseData;
import core.common.transmittable.GetResponseHeader;
import core.common.transmittable.ListQuery;
import core.common.transmittable.ListResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of {@link ServerInterface}.
 */
public class Server implements ServerInterface {

    private static final int FILE_PIECE_SIZE = 2048;
    private static final int PORT = 23923;
    private volatile boolean running;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void stop() {
        running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(@NotNull UncaughtExceptionHandler exceptionHandler) throws ServerAlreadyStartedException {
        if (running) {
            throw new ServerAlreadyStartedException();
        }
        running = true;
        Thread thread = new Thread(this::go);
        thread.setUncaughtExceptionHandler(exceptionHandler);
        new Thread(this::go).start();
    }

    private void go() {
        try(Selector selector = Selector.open();
            ServerSocketChannel acquaintanceChannel = ServerSocketChannel.open()){

            acquaintanceChannel.bind(new InetSocketAddress(PORT));
            acquaintanceChannel.configureBlocking(false);

            acquaintanceChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (running) {
                selector.select();

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator it = selectedKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();

                    if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                        ServerSocketChannel gotAcquaintanceChannel = (ServerSocketChannel) key.channel();
                        SocketChannel connectionChannel = gotAcquaintanceChannel.accept();
                        connectionChannel.configureBlocking(false);

                        SelectionKey newKey = connectionChannel.register(selector,
                            SelectionKey.OP_READ);
                        newKey.attach(new Connection(new PackageProcessor() {

                            private FTPPackage response;

                            private boolean fileSendingMode;
                            private boolean isUpToDate;

                            private FileChannel fileChannel;
                            private final ByteBuffer buffer = ByteBuffer.allocate(FILE_PIECE_SIZE);

                            @Override
                            public void process(@NotNull FTPPackage ftpPackage) {
                                if (ftpPackage instanceof ListQuery) {
                                    File directory = new File(((ListQuery) ftpPackage).getPath());

                                    if (!directory.exists()) {
                                        response = new ErrorResponse(new FileNotFoundException(directory.getPath()));
                                    } else if (!directory.isDirectory()) {
                                        response = new ErrorResponse(new NotDirectoryException(directory.getPath()));
                                    } else {
                                        File[] directoriesArray = directory.listFiles(File::isDirectory);
                                        File[] filesArray = directory.listFiles(File::isFile);
                                        if (directoriesArray == null || filesArray == null) {
                                            throw new RuntimeException();
                                        }
                                        List<String> directories = Arrays.stream(directoriesArray)
                                            .map(File::getName).collect(Collectors.toList());
                                        List<String> files = Arrays.stream(filesArray)
                                            .map(File::getName).collect(Collectors.toList());
                                        response = new ListResponse(directories, files);
                                    }
                                } else if (ftpPackage instanceof GetQuery) {
                                    try {
                                        beginFileSending(
                                            new File(((GetQuery) ftpPackage).getPath()));
                                    } catch (FileNotFoundException e) {
                                        response = new ErrorResponse(e);
                                        fileSendingMode = false;
                                    }
                                } else {
                                    throw new RuntimeException("Unable to recognize the query");
                                }
                                newKey.interestOps(SelectionKey.OP_WRITE);

                                isUpToDate = true;
                            }

                            @Nullable
                            @Override
                            public FTPPackage formResponse() {
                                if (isUpToDate) {
                                    isUpToDate = false;
                                    return response;
                                }
                                if (!fileSendingMode) {
                                    newKey.interestOps(SelectionKey.OP_READ);
                                    return null;
                                }
                                updateGetResponse();
                                return response;
                            }

                            private void updateGetResponse() {
                                try {
                                    buffer.clear();
                                    int readBytes = fileChannel.read(buffer);
                                    if (readBytes <= 0) {
                                        endFileSending();
                                        return;
                                    }
                                    buffer.flip();
                                    byte[] data = new byte[buffer.limit()];
                                    buffer.get(data);
                                    response = new GetResponseData(data);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            private void beginFileSending(@NotNull File fileToSend) throws FileNotFoundException {
                                if (!fileSendingMode) {
                                    fileSendingMode = true;
                                    response = new GetResponseHeader(fileToSend.getPath(), fileToSend.length());
                                    fileChannel = new FileInputStream(fileToSend).getChannel();
                                } else {
                                    throw new RuntimeException(new IllegalStateException());
                                }
                            }

                            private void endFileSending() throws IOException {
                                response = null;
                                isUpToDate = false;
                                fileSendingMode = false;
                                fileChannel.close();
                                newKey.interestOps(SelectionKey.OP_READ);
                            }
                        }, connectionChannel));
                    } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                        ((Connection) key.attachment()).read();
                    } else if ((key.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {
                        ((Connection) key.attachment()).write();
                    }
                    it.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


