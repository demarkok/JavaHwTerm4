import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by demarkok on 19-Mar-17.
 */
public class RepoManagerTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void createRepoTest() throws Exception {

        Path dir = tmpFolder.getRoot().toPath();


        Repo repo = new Repo(dir);
        repo.initialize();

        File[] files = dir.resolve(Repo.HIT_FOLDER_NAME).toFile().listFiles();
        assertNotNull(files);
        Collection<String> fileNames = Stream.of(files).map(File::getName).collect(Collectors.toSet());
        assertEquals(fileNames, new HashSet<>(Arrays.asList(Repo.BRANCHES_FILE_NAME,
                                                            Repo.OBJECTS_FOLDER_NAME,
                                                            Repo.HEAD_FILE_NAME)));

    }

    @Test
    public void treeSerializationTest() throws Exception {

        Path dir = tmpFolder.getRoot().toPath();
        Repo repo = new Repo(dir);
        repo.initialize();

        String fileName = "file";
        String folderName = "folder";

        Path filePath = dir.resolve(fileName);
        Files.createFile(filePath);

        Path folderPath = dir.resolve(folderName);
        Files.createDirectory(folderPath);


        Repo.Tree folderTree = repo.new Tree();
        Repo.Blob blob = repo.new Blob(filePath);

        ImmutableMap<String, String> blobs = ImmutableMap.of(fileName, blob.sha);
        ImmutableMap<String, String> trees = ImmutableMap.of(folderName, folderTree.sha);


        Repo.Tree storedTree = repo.new Tree(blobs, trees);
        Repo.Tree loadedTree = repo.new Tree(storedTree.sha);

        assertEquals(storedTree.getBlobs(), loadedTree.getBlobs());
        assertEquals(storedTree.getTrees(), loadedTree.getTrees());
    }

}