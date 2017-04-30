package core.client;

import core.common.Connection;
import core.common.PackageProcessor;
import core.common.transmittable.ErrorResponse;
import core.common.transmittable.FTPPackage;
import core.common.transmittable.GetQuery;
import core.common.transmittable.GetResponseData;
import core.common.transmittable.GetResponseHeader;
import core.common.transmittable.ListQuery;
import core.common.transmittable.ListResponse;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The implementation of {@link ClientInterface}.
 */
public class Client implements ClientInterface {

    private static final int PORT = 23923;

    @Nullable
    private Connection connection;
    @NotNull
    private final ClientPackageProcessor processor = new ClientPackageProcessor();
    @Nullable
    private SocketChannel channel;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean connect(@NotNull String ip) {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            connection = new Connection(processor, channel);
            return channel.connect(new InetSocketAddress(ip, PORT));

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() throws IOException {
        if (channel == null) {
            throw new NotYetConnectedException();
        }
        channel.close();
        channel = null;
        connection = null;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public List<String> executeList(@NotNull String path) throws IOException {
        if (!isConnectionValid()) {
            throw new NotYetConnectedException();
        }
        processor.formListQuery(path);
        assert connection != null;
        connection.write();
        connection.read();
        if (processor.lastListResponse == null) {
            return new LinkedList<>();
        }
        List<String> result = processor.lastListResponse.getDirectories();
        result = result.stream().map(s -> s + "/").collect(Collectors.toList());
        result.addAll(processor.lastListResponse.getFiles());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeGet(@NotNull String pathSrc, @NotNull String pathDst) throws IOException {
        if (!isConnectionValid()) {
            throw new NotYetConnectedException();
        }
        processor.formGetQuery(pathSrc, pathDst);
        assert connection != null;
        connection.write();

        do {
            connection.read();
        } while(processor.fileReadingMode);

    }

    private class ClientPackageProcessor implements PackageProcessor {

        @Nullable
        private FTPPackage query;
        @Nullable
        private ListResponse lastListResponse;

        private long expectedSize;
        private long actualSize;
        private FileOutputStream fileStream;
        private boolean fileReadingMode;

        @Override
        public void process(@NotNull FTPPackage ftpPackage) throws IOException {
            if (ftpPackage instanceof ListResponse) {
                lastListResponse = (ListResponse)ftpPackage;
            } else if (ftpPackage instanceof GetResponseHeader) {
                expectedSize = ((GetResponseHeader) ftpPackage).getFileSize();
                fileReadingMode = true;
                actualSize = 0;
            } else if (ftpPackage instanceof GetResponseData) {
                byte[] buffer = ((GetResponseData) ftpPackage).getData();
                actualSize += buffer.length;
                fileStream.write(buffer);
            } else if (ftpPackage instanceof ErrorResponse) {
                throw ((ErrorResponse) ftpPackage).getException();
            }
            if (fileReadingMode) {
                if (actualSize > expectedSize) {
                    throw new IllegalStateException();
                }
                if (actualSize == expectedSize) {
                    fileStream.close();
                    fileReadingMode = false;
                }
            }
        }

        @Nullable
        @Override
        public FTPPackage formResponse() {
            return query;
        }

        private void formListQuery(@NotNull String path) {
            query = new ListQuery(path);
        }

        private void formGetQuery(@NotNull String pathSrc, @NotNull String pathDst) throws FileNotFoundException {
            query = new GetQuery(pathSrc);
            fileStream = new FileOutputStream(pathDst);
        }
    }

    private boolean isConnectionValid() {
        return connection != null && channel != null && channel.isOpen() && channel.isConnected();
    }
}
