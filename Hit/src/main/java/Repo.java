import Exceptions.*;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Repo {
    Path rootDirectory;
    Head head = new Head(); // sha of commit
    Map <String, String> branches = new HashMap<>(); // name - commitSha

    static final String HIT_FOLDER_NAME = ".hit";
    static final String BRANCHES_FILE_NAME = "BRANCHES";
    static final String OBJECTS_FOLDER_NAME = "objects";
    static final String HEAD_FILE_NAME = "HEAD";
    static final String MASTER_BRANCH_NAME = "master";


    private final Path OBJECTS_PATH;
    private final Path HEAD_PATH;
    private final Path BRANCHES_PATH;

    public Repo(Path rootDirectory) {
        this.rootDirectory = rootDirectory;
        OBJECTS_PATH = rootDirectory.resolve(HIT_FOLDER_NAME).resolve(OBJECTS_FOLDER_NAME);
        HEAD_PATH = rootDirectory.resolve(HIT_FOLDER_NAME).resolve(HEAD_FILE_NAME);
        BRANCHES_PATH = rootDirectory.resolve(HIT_FOLDER_NAME).resolve(BRANCHES_FILE_NAME);
    }

    public void initialize() throws IOException {
        Path hitDirectory = rootDirectory.resolve(HIT_FOLDER_NAME);
        Files.createDirectory(hitDirectory);
        Files.createFile(hitDirectory.resolve(BRANCHES_FILE_NAME));
        Files.createDirectory(hitDirectory.resolve(OBJECTS_FOLDER_NAME));
        Files.createFile(hitDirectory.resolve(HEAD_FILE_NAME));

        Commit firstCommit = new Commit(buildTree(rootDirectory).sha, ImmutableSet.of(), "init");
        branches.put(MASTER_BRANCH_NAME, firstCommit.sha);
        head.moveToBranch(MASTER_BRANCH_NAME);
        storeHead();
        storeBranches();
    }

    public void load() throws IOException {
        loadHead();
        loadBranches();
    }

    public abstract class HitObject {

        protected String sha;

        HitObject(String sha) throws IncorrectSHAException {
            Path objFile = rootDirectory.resolve(HIT_FOLDER_NAME).resolve(OBJECTS_FOLDER_NAME).resolve(sha);
            if (!Files.exists(objFile)) {
                throw new IncorrectSHAException();
            }
        }

        HitObject() {}
    }

    public class Blob extends HitObject {

        Blob(String sha) throws IncorrectSHAException {
            super(sha);
        }

        Blob(Path path) throws IOException {
            this.sha = DigestUtils.sha1Hex(Files.readAllBytes(path));
            Files.copy(path, Repo.this.OBJECTS_PATH.resolve(this.sha), REPLACE_EXISTING);
        }
    }

    public class Tree extends HitObject implements Serializable {

        private final Map<String, String> blobs; // name -> sha
        private final Map<String, String> trees; // name -> sha

        @SuppressWarnings("unchecked")
        public Tree(String sha) throws IncorrectSHAException, IOException {
            super(sha);

            byte[] data = Files.readAllBytes(OBJECTS_PATH.resolve(sha));
            try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object treesObj = ois.readObject();
                Object blobsObj = ois.readObject();

                if (!(treesObj instanceof Map &&
                        blobsObj instanceof Map)) {
                    throw new IncorrectHitObjectException();
                }

                trees = (Map)treesObj;
                blobs = (Map)blobsObj;

            } catch (ClassNotFoundException e) {
                throw new IncorrectHitObjectException();
            }
        }

        public Tree(Map<String, String> blobs, Map<String, String> trees) throws IOException {
            this.blobs = blobs;
            this.trees = trees;

            byte[] data;

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(trees);
                oos.writeObject(blobs);
                oos.flush();
                data = baos.toByteArray();
            }
            sha = DigestUtils.sha1Hex(data);
            Files.write(OBJECTS_PATH.resolve(sha), data);
        }

        public Tree() throws IOException {
            this(new HashMap<>(), new HashMap<>());
        }

        public Map<String, String> getBlobs() {
            return blobs;
        }

        public Map<String, String> getTrees() {
            return trees;
        }
    }

    public class Commit extends HitObject implements Serializable {
        private final String tree; // sha
        private final Set <String> parents; // sha of parent commits
        private final String message;
        private final Date date;
        private final String author;


        @SuppressWarnings("unchecked")
        Commit(String sha) throws IncorrectSHAException, IOException {
            super(sha);

            byte[] data = Files.readAllBytes(Repo.this.OBJECTS_PATH.resolve(sha));
            try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {

                Object treeObj = ois.readObject();
                Object parentsObj = ois.readObject();
                Object messageObj = ois.readObject();
                Object dateObj = ois.readObject();
                Object authorObj = ois.readObject();


                if (!(treeObj instanceof String &&
                        parentsObj instanceof Set &&
                        messageObj instanceof String &&
                        dateObj instanceof Date &&
                        authorObj instanceof String)) {
                    throw new IncorrectHitObjectException();
                }

                this.tree = (String) treeObj;
                this.parents = (Set<String>) parentsObj;
                this.message = (String) messageObj;
                this.date = (Date) dateObj;
                this.author = (String) authorObj;

            } catch (ClassNotFoundException e) {
                throw new IncorrectHitObjectException();
            }
        }

        Commit(String tree, Set <String> parents, String message) throws IOException {
            this.tree = tree;
            this.parents = parents;
            this.message = message;
            this.date = new Date();
            this.author = System.getProperty("user.name");

            byte[] data;

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(tree);
                oos.writeObject(parents);
                oos.writeObject(message);
                oos.writeObject(this.date);
                oos.writeObject(this.author);

                oos.flush();
                data = baos.toByteArray();
            }
            this.sha = DigestUtils.sha1Hex(data);
            Files.write(Repo.this.OBJECTS_PATH.resolve(this.sha), data);
        }

        Commit(String tree, String parent, String message) throws IOException {
            this(tree, ImmutableSet.of(parent), message);
        }
    }

    private class Head implements Serializable {
        private boolean detached;
        private String info;

        private void moveToBranch(String branchName) {
            this.detached = false;
            this.info = branchName;
        }
        private void moveToCommit(String commitSha) {
            this.detached = true;
            this.info = commitSha;
        }
        private String getCommitSha() {
            if (this.detached) {
                return this.info;
            } else {
                return Repo.this.branches.get(this.info);
            }
        }
    }

    private void moveBranch(String branchName, String commitSha) throws IOException {
        this.branches.put(branchName, commitSha);
        this.storeBranches();
    }

    private Tree buildTree(Path path) throws IOException {
        HashMap<String, String> blobs = new HashMap<>(); // name -> sha
        HashMap<String, String> trees = new HashMap<>(); // name -> sha

        for (File file : path.toFile().listFiles()) {
            if (file.isFile()) {
                Repo.Blob blob = new Repo.Blob(file.toPath());
                blobs.put(file.getName(), blob.sha);
            } else if (file.isDirectory()) {
                Tree tree = buildTree(file.toPath());
                trees.put(file.getName(), tree.sha);
            }
        }
        return new Tree(blobs, trees);
    }

    public Commit commit(String message) throws IOException {
        Tree tree = this.buildTree(this.rootDirectory);
        Commit commit = new Commit(tree.sha, this.head.getCommitSha(), message);
        if (this.head.detached) {
            this.head.moveToCommit(commit.sha);
        } else {
            this.moveBranch(this.head.info, commit.sha);
        }
        this.storeHead();
        return commit;
    }

    private void loadHead() throws IOException {
        serializableHeadInfoHolder holder = Repo.load(this.HEAD_PATH);
        this.head.detached = holder.detached;
        this.head.info = holder.info;
    }
    private void storeHead() throws IOException {
        Repo.store(new serializableHeadInfoHolder(this.head), this.HEAD_PATH);
    }

    private static class serializableHeadInfoHolder implements Serializable{
        private boolean detached;
        private String info;
        private serializableHeadInfoHolder(Head head) {
            this.detached = head.detached;
            this.info = head.info;
        }
    }

    private void loadBranches() throws IOException {
        this.branches = Repo.load(this.BRANCHES_PATH);
    }
    private void storeBranches() throws IOException {
        Repo.store(this.branches, this.BRANCHES_PATH);
    }

    private void clearDirectory(Path directory) throws UnableToDeleteFileException {
        File[] filesList =  directory.toFile().listFiles(pathname -> !pathname.getName().startsWith("."));

        if (filesList == null) {
            throw new InvalidRepositoryStateException();
        }

        for (File file : filesList) {
            try {
                FileUtils.forceDelete(file);
            } catch (IOException e) {
                throw new UnableToDeleteFileException();
            }
        }
    }

    private void restoreDirectory(Path directory, Tree tree) throws IOException{

        for (Entry<String, String> nameAndSha : tree.getBlobs().entrySet()) {
            FileUtils.copyFile(this.OBJECTS_PATH.resolve(nameAndSha.getValue()).toFile(),
                               directory.resolve(nameAndSha.getKey()).toFile());
        }
        for (Entry<String, String> nameAndSha : tree.getTrees().entrySet()) {
            Path folder = directory.resolve(nameAndSha.getKey());
            if (!Files.exists(folder)) {
                Files.createDirectory(folder);
            }
            try {
                this.restoreDirectory(folder, new Tree(nameAndSha.getValue()));
            } catch (IncorrectSHAException e) {
                throw new InvalidRepositoryStateException();
            }
        }
    }

    private void checoutToCommit(String commitSha) throws IOException, IncorrectSHAException, UnableToDeleteFileException {
        Tree tree = new Tree(new Commit(commitSha).tree);
        this.clearDirectory(this.rootDirectory);
        this.restoreDirectory(this.rootDirectory, tree);

        this.head.moveToCommit(commitSha);
        this.storeHead();
    }

    private void checkoutToBranch(String branchName) throws IOException, IncorrectSHAException, NoSuchBranchException, UnableToDeleteFileException {
        if (!this.branches.containsKey(branchName)) {
            throw new NoSuchBranchException();
        }
        Commit commit = new Commit(this.branches.get(branchName));
        Tree tree = new Tree(commit.tree);
        this.clearDirectory(this.rootDirectory);
        this.restoreDirectory(this.rootDirectory, tree);

        this.head.moveToBranch(branchName);
        this.storeHead();
    }

    public Commit merge(String branchName) throws NoSuchBranchException, IOException, IncorrectSHAException {
        if (!this.branches.containsKey(branchName)) {
            throw new NoSuchBranchException();
        }
        Commit thatCommit = new Commit(this.branches.get(branchName));
        this.restoreDirectory(this.rootDirectory, new Tree(thatCommit.tree));

        Tree newTree = this.buildTree(this.rootDirectory);
        Commit newCommit = new Commit(newTree.sha, head.getCommitSha(), "merge");
        if (head.detached) {
            head.moveToCommit(newCommit.sha);
        } else {
            moveBranch(head.info, newCommit.sha);
        }
        storeHead();
        return newCommit;
    }

    public void branch(String branchName) {
        branches.put(branchName, head.getCommitSha());
        head.moveToBranch(branchName);
    }


    private static <T> T load(Path path) throws IOException {
        byte[] data = Files.readAllBytes(path);
        T result = null;
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ObjectInputStream ois = new ObjectInputStream(bais)) {
            result =  (T) ois.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
    private static <T> void store(T object, Path path) throws IOException {
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(object);
            oos.flush();
            data = baos.toByteArray();
        }
        Files.write(path, data);
    }
}



