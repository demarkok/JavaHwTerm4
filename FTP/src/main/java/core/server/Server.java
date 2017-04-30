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
import java.nio.file.AccessDeniedException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
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
    private Thread workingThread;
    private final List<SocketChannel> channelsToClose = new LinkedList<>();
    private Path rootPath;

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public void stop() {
        if (!running) {
            return;
        }
        running = false;

        try {
            workingThread.join(1000);
            if (workingThread.isAlive()) {
                workingThread.interrupt();
            }
            workingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(@NotNull UncaughtExceptionHandler exceptionHandler, @NotNull String rootPath)
            throws ServerAlreadyStartedException {
        this.rootPath = Paths.get(rootPath);
        if (running) {
            throw new ServerAlreadyStartedException();
        }
        running = true;
        workingThread = new Thread(this::go);
        workingThread.setUncaughtExceptionHandler(exceptionHandler);
        workingThread.start();
    }

    private void go() {
        try(Selector selector = Selector.open();
            ServerSocketChannel acquaintanceChannel = ServerSocketChannel.open()){

            acquaintanceChannel.configureBlocking(false);
            acquaintanceChannel.bind(new InetSocketAddress(PORT));

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

                        channelsToClose.add(connectionChannel);

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
                                    String pathString = ((ListQuery) ftpPackage).getPath();
                                    File directory = rootPath.resolve(pathString).toFile();

                                    if (!isInSubDirectory(rootPath.toAbsolutePath().normalize(),
                                        directory.toPath().toAbsolutePath().normalize())) {
                                        response = new ErrorResponse(new AccessDeniedException(pathString));
                                    } else if (!directory.exists()) {
                                        response = new ErrorResponse(new FileNotFoundException(pathString));
                                    } else if (!directory.isDirectory()) {
                                        response = new ErrorResponse(new NotDirectoryException(pathString));
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
                                    String pathString = ((GetQuery) ftpPackage).getPath();
                                    File file = rootPath.resolve(pathString).toFile();
                                    if (!isInSubDirectory(rootPath.toAbsolutePath().normalize(),
                                        file.toPath().toAbsolutePath().normalize())) {
                                        response = new ErrorResponse(new AccessDeniedException(pathString));
                                    } else if (!file.exists() || !file.isFile()) {
                                        response = new ErrorResponse(new FileNotFoundException(pathString));
                                    } else {
                                        try {
                                            beginFileSending(file);
                                        } catch (FileNotFoundException e) {
                                            response = new ErrorResponse(e);
                                            fileSendingMode = false;
                                        }
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

                            private boolean isInSubDirectory(@NotNull Path directory, @Nullable Path file) {
                                return file != null && (directory.equals(file) ||
                                    isInSubDirectory(directory, file.getParent()));
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
            for (SocketChannel channel: channelsToClose) {
                channel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}


