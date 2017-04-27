package core.common;

import core.common.transmittable.FTPPackage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a channel wrapper which is responsible for work with buffers and the channel:
 * serialization, sending, receiving, gathering and deserialization packages.
 */
public class Connection {
    @NotNull
    private final ByteBuffer incomingPackageSizeBuffer;
    @Nullable
    private ByteBuffer incomingPackage;
    @Nullable
    private ByteBuffer outgoingPackage;
    @NotNull
    private final SocketChannel channel;
    @NotNull
    private final PackageProcessor processor;

    /**
     * @param processor - the package processor which is used to handleException received packages and prepare
     * response.
     * @param channel - the socket channel connected with removed side.
     */
    public Connection(@NotNull PackageProcessor processor, @NotNull SocketChannel channel) {
        this.channel = channel;
        this.processor = processor;
        incomingPackageSizeBuffer = ByteBuffer.allocate(4);
    }

    /**
     * Reads some data from the channel to the package.
     * If the read package is completed, calls the processor to handleException it.
     * Call this method if the channel is ready for reading.
     * throws IOException
     */
    public void read() throws IOException {
        try {
            if (incomingPackageSizeBuffer.hasRemaining()) {
                channel.read(incomingPackageSizeBuffer);
                if (!incomingPackageSizeBuffer.hasRemaining()) {
                    incomingPackageSizeBuffer.flip();
                    incomingPackage = ByteBuffer.allocate(incomingPackageSizeBuffer.getInt());
                } else {
                    return;
                }
            }
            while (true) {
                int bytesPiece = channel.read(incomingPackage);
                if (bytesPiece <= 0) {
                    break;
                }
            }
        } catch (IOException e) {
            channel.close();
            return;
        }
        if (!incomingPackage.hasRemaining()) {
            processPackage();
        }
    }

    /**
     * Sends data through the channel if there is any, otherwise tries to get the data from the processor.
     * Call this method if the channel is ready for writing.
     * throws IOException
     */
    public void write() throws IOException {
        if (outgoingPackage == null || !outgoingPackage.hasRemaining()) {
            FTPPackage ftpPackage = processor.formResponse();

            if (ftpPackage == null) {
                return;
            }

            try (ByteArrayOutputStream underlyingBos = new ByteArrayOutputStream();
                ObjectOutput objOut = new ObjectOutputStream(underlyingBos)) {

                objOut.writeObject(ftpPackage);
                objOut.flush();
                int objLength = underlyingBos.size();
                outgoingPackage = ByteBuffer.allocate(4 + objLength);
                outgoingPackage.putInt(objLength);
                outgoingPackage.put(underlyingBos.toByteArray().clone());
                outgoingPackage.flip();
            }
        }

        try {
            while (true) {
                int bytesWritten = channel.write(outgoingPackage);
                if (bytesWritten <= 0) {
                    return;
                }
            }
        } catch (IOException e) {
            channel.close();
        }

    }

    private void processPackage() throws IOException {
        if (incomingPackage == null) {
            return;
        }

        incomingPackage.flip();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(incomingPackage.array());
                ObjectInput in = new ObjectInputStream(bis)){
            FTPPackage ftpPackage = (FTPPackage)in.readObject();
            processor.process(ftpPackage);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            incomingPackageSizeBuffer.clear();
        }

    }
}
