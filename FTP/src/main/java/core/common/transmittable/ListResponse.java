package core.common.transmittable;

import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the response of the {@link ListQuery}: list of files and list of directories placed in
 * asked path.
 */
public class ListResponse implements FTPPackage {

    @NotNull
    private final List<String> directories;
    @NotNull
    private final List<String> files;

    /**
     * Creates the List response with {@code directories} as directories list and
     * {@code files} as files list.
     * @param directories - list of names of directories
     * @param files - list of names of files
     */
    public ListResponse(@NotNull List<String> directories, @NotNull List<String> files) {
        this.directories = directories;
        this.files = files;
    }

    /**
     * @return list of directories placed in asked path.
     */
    @NotNull
    public List<String> getDirectories() {
        return directories;
    }

    /**
     * @return list of files placed in asked path.
     */
    @NotNull
    public List<String> getFiles() {
        return files;
    }
}
