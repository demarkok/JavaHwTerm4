package core.common;

import core.common.transmittable.FTPPackage;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the package processor which gets assembled package, processes it and form the response (if wanted).
 */
public interface PackageProcessor {

    /**
     * Process received package. E.g. save the file.
     * @param ftpPackage - the received package which is going to be processed.
     * throws IOException
     */
    void process(@NotNull FTPPackage ftpPackage) throws IOException;

    /**
     * Forms the response based on received data.
     * @return response, null if the response wasn't formed
     */
    @Nullable
    FTPPackage formResponse();
}
