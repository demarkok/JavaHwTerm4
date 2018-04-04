package GUI.client;

import java.nio.file.Path;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FilePathTreeItem extends TreeItem<String> {

    private final boolean isDirectory;
    private boolean isExpanded;
    private final Path path;
    private static final Image FOLDER_COLLAPSE_IMAGE = new Image("icons/folder.png");
    private static final Image FOLDER_EXPAND_IMAGE = new Image("icons/folder-open.png");
    private static final Image FILE_IMAGE = new Image("icons/file.png");

    private final Explorer explorer;


    public FilePathTreeItem(Path path, boolean isDirectory, Explorer explorer) {
        super(path.toString());

        this.explorer = explorer;
        this.isDirectory = isDirectory;

        this.path = path;

        if (isDirectory) {
            setGraphic(new ImageView(FOLDER_COLLAPSE_IMAGE));
        } else {
            setGraphic(new ImageView(FILE_IMAGE));
        }

        setValue(path.getFileName().toString());

        addEventHandler(TreeItem.branchExpandedEvent(),
            (EventHandler<TreeModificationEvent<String>>) event -> {
                FilePathTreeItem source = (FilePathTreeItem) event.getSource();
                if (source.isDirectory() && source.isExpanded()) {
                    ((ImageView) source.getGraphic()).setImage(FOLDER_EXPAND_IMAGE);
                }
                for (TreeItem<String> it: source.getChildren()) {
                    if ((it instanceof FilePathTreeItem)) {
                        ((FilePathTreeItem) it).expand();
                    }
                }
            });

        addEventHandler(TreeItem.branchCollapsedEvent(),
            (EventHandler<TreeModificationEvent<String>>) event -> {
                FilePathTreeItem source=(FilePathTreeItem)event.getSource();
                if (source.isDirectory() && !source.isExpanded()) {
                    ((ImageView) source.getGraphic()).setImage(FOLDER_COLLAPSE_IMAGE);
                }
            });
    }

    public void expand() {
        if (isDirectory && !isExpanded) {
            for (String dirName : explorer.getDirectories(path)) {
                FilePathTreeItem treeNode = new FilePathTreeItem(path.resolve(dirName),
                    true, explorer);
                getChildren().add(treeNode);
            }

            for (String fileName : explorer.getFiles(path)) {
                FilePathTreeItem treeNode = new FilePathTreeItem(path.resolve(fileName),
                    false, explorer);
                getChildren().add(treeNode);
            }
            isExpanded = true;
        }
    }

    public Path getPath() {
        return path;
    }
    public boolean isDirectory() {
        return isDirectory;
    }
}