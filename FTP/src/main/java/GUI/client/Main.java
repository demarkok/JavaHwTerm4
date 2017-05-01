package GUI.client;

import core.client.Client;
import core.client.ClientInterface;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * JavaFX client application.
 */
public class Main extends Application{

    private static final String TITLE = "FTP Client";
    private static final String DEFAULT_HOST = "localhost";
    private static final int WIDTH = 350;
    private static final int HEIGHT = 500;

    private Stage window;

    private ToggleButton connectButton;
    private TextField addressField;

    private TreeView<String> treeView;


    private ClientInterface client;
    private final VBox treeBox = new VBox();
    private final FileChooser fileChooser = new FileChooser();

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

        client = new Client();

        window = primaryStage;
        primaryStage.setTitle(TITLE);

        initializeConnectButton();
        initializeAddressField();

        treeBox.setPadding(new Insets(10,10,10,10));
        treeBox.setSpacing(10);

        treeView = new TreeView<>();
        treeBox.getChildren().addAll(new Label("File browser"),treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        HBox topLayout = new HBox();
        topLayout.setSpacing(20);
        topLayout.setPadding(new Insets(10));

        topLayout.getChildren().add(connectButton);
        topLayout.getChildren().add(addressField);

        BorderPane layout = new BorderPane();
        layout.setTop(topLayout);
        layout.setCenter(treeView);

        Scene scene = new Scene(layout, WIDTH, HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initializeConnectButton() {
        connectButton = new ToggleButton();
        connectButton.setText("connect");
        connectButton.setPrefWidth(100);
        connectButton.setOnAction(event -> {
            if (connectButton.isSelected()) {
                if (client.connect(addressField.getText())) {
                    System.out.println("ok");
                    addressField.setText("connected to " + addressField.getText());
                    addressField.setAlignment(Pos.BASELINE_CENTER);
                    addressField.setDisable(true);
                    connectButton.setText("disconnect");
                    initializeTreeView();
                } else {
                    connectButton.setSelected(false);
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection error");
                    alert.setHeaderText(null);
                    alert.setContentText("Unable to connect to server.");
                    alert.showAndWait();
                }
            } else {
                try {
                    client.disconnect();
                    connectButton.setText("connect");
                    addressField.setText(DEFAULT_HOST);
                    addressField.setAlignment(Pos.BASELINE_LEFT);
                    addressField.setDisable(false);
                    treeView.setRoot(null);
                } catch (IOException e) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection error");
                    alert.setHeaderText(null);
                    alert.setContentText("Unable to disconnect from server.");
                    alert.showAndWait();
                }
            }
        });
    }

    private void initializeAddressField() {
        addressField = new TextField();
        addressField.setPrefWidth(200);
        addressField.setText(DEFAULT_HOST);
        addressField.setAlignment(Pos.BASELINE_LEFT);
        addressField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButton.fire();
            }
        });
    }

    private void initializeTreeView() {

        FilePathTreeItem newRootNode = new FilePathTreeItem(Paths.get(""), true, new Explorer() {
            @Override
            public Set<String> getFiles(Path path) {
                try {
                    Map<String,Boolean> result = client.executeList(path.toString());
                    return result.entrySet().stream()
                        .filter(x -> !x.getValue())
                        .map(Entry::getKey)
                        .collect(Collectors.toSet());
                } catch (IOException e) {
                    return Collections.emptySet();
                }
            }

            @Override
            public Set<String> getDirectories(Path path) {
                try {
                    Map<String,Boolean> result = client.executeList(path.toString());
                    return result.entrySet().stream()
                        .filter(Entry::getValue)
                        .map(Entry::getKey)
                        .collect(Collectors.toSet());
                } catch (IOException e) {
                    return Collections.emptySet();
                }
            }
        });

        newRootNode.expand();

        treeView.setRoot(newRootNode);
        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                if (item instanceof FilePathTreeItem) {
                    if (!((FilePathTreeItem) item).isDirectory()) {
                        Path filePath = ((FilePathTreeItem) item).getPath();
                        try {
                            fileChooser.setTitle("Download file");
                            fileChooser.setInitialFileName(filePath.getFileName().toString());
                            File dstFile = fileChooser.showSaveDialog(window);
                            if (dstFile != null) {
                                client.executeGet(filePath.toString(),
                                    dstFile.getPath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }
}


