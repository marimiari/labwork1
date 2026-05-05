package ru.itmo.marimiari.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.itmo.marimiari.config.DbConfig;
import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.repository.*;
import ru.itmo.marimiari.service.*;
import ru.itmo.marimiari.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MainUI extends Application { //класс javafx приложения, переопределяет метод старт
    private static final String SAVE_FILE = "data.xml";
    private SampleService sampleService;
    private ContainerService containerService;
    private SlotService slotService;
    private PlacementService placementService;
    private User currentUser;

    private ListView<Container> containerListView;
    private ContainerDetailPane detailPane;
    private ProgressBar progressBar;

    private Button addContainerButton, addSlotsButton, addSampleButton, saveButton;
    private Button loginButton, registerButton, logoutButton;

    @Override
    public void start(Stage primaryStage) {
        try (Connection conn = DbConfig.getConnection()) {
            // ok
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Cannot connect to database.\nCheck PostgreSQL and db.properties", ButtonType.OK);
            alert.showAndWait();
            Platform.exit();
            return;
        }

        UserRepository userRepo = new UserRepository();
        UserService userService = new UserService(userRepo);

        LoginDialog loginDialog = new LoginDialog(userService);
        if (!loginDialog.showAndWait()) {
            Platform.exit();
            return;
        }
        currentUser = loginDialog.getLoggedInUser();

        SampleRepository sampleRepo = new SampleRepository();
        ContainerRepository containerRepo = new ContainerRepository();
        SlotRepository slotRepo = new SlotRepository();
        PlacementRepository placementRepo = new PlacementRepository();

        // 3. Сервисы
        sampleService = new SampleService(sampleRepo);
        containerService = new ContainerService(containerRepo);
        slotService = new SlotService(slotRepo, containerService);
        placementService = new PlacementService(placementRepo, sampleService, containerService, slotService);

        long userId = currentUser.getId();
        sampleService.setCurrentUserId(userId);
        containerService.setCurrentUserId(userId);
        slotService.setCurrentUserId(userId);
        placementService.setCurrentUserId(userId);

        containerListView = new ListView<>();
        containerListView.setCellFactory(lv -> new ListCell<Container>() {
            @Override
            protected void updateItem(Container c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) setText(null);
                else setText("#" + c.getId() + " " + c.getName() + " (" + c.getType() + ") — " + c.getOwnerLogin());
            }
        });

        detailPane = new ContainerDetailPane(containerService, slotService, placementService, sampleService, currentUser);
        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshContainerList());

        addContainerButton = new Button("Add container");
        addContainerButton.setOnAction(e -> showAddContainerDialog());

        addSlotsButton = new Button("Add slots");
        addSlotsButton.setOnAction(e -> showAddSlotsDialog());

        addSampleButton = new Button("Add sample");
        addSampleButton.setOnAction(e -> showAddSampleDialog());

        saveButton = new Button("Save");
        saveButton.setOnAction(e -> showInfo("Data is automatically saved to database"));

        loginButton = new Button("Login");
        loginButton.setDisable(true);
        registerButton = new Button("Register");
        registerButton.setDisable(true);
        logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> logout());

        VBox leftPane = new VBox(10,
                new Label("Containers"),
                containerListView,
                refreshButton,
                addContainerButton,
                addSlotsButton,
                addSampleButton,
                saveButton,
                new Separator(),
                loginButton, registerButton, logoutButton,
                progressBar);
        leftPane.setPadding(new javafx.geometry.Insets(10));

        BorderPane root = new BorderPane();
        root.setLeft(leftPane);
        root.setCenter(detailPane);

        containerListView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> detailPane.setContainer(newVal));

        refreshContainerList();

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setTitle("Container Manager - " + currentUser.getLogin());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshContainerList() {
        List<Container> containers = List.copyOf(containerService.getAll());
        containerListView.setItems(FXCollections.observableArrayList(containers));
        Container selected = containerListView.getSelectionModel().getSelectedItem();
        if (selected != null && containers.contains(selected)) {
            detailPane.setContainer(selected);
        } else {
            detailPane.setContainer(null);
        }
    }

    private void showAddContainerDialog() {
        if (currentUser == null) {
            showAlert("Please login first");
            return;
        }
        Dialog<Container> d = new Dialog<>();
        d.setTitle("Add Container");
        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("FREEZER", "FRIDGE", "BOX");
        GridPane grid = new GridPane();
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        d.getDialogPane().setContent(grid);
        ButtonType ok = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        Button btn = (Button) d.getDialogPane().lookupButton(ok);
        btn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            String name = nameField.getText().trim();
            String typeStr = typeBox.getValue();
            if (name.isEmpty()) {
                showAlert("Name empty");
                ev.consume();
                return;
            }
            if (typeStr == null) {
                showAlert("Select type");
                ev.consume();
                return;
            }
            try {
                ContainerType type = ContainerType.valueOf(typeStr);
                Container newContainer = containerService.add(name, type);
                if (newContainer == null) {
                    showAlert("Failed to add container");
                    ev.consume();
                    return;
                }
                refreshContainerList();
                containerListView.getSelectionModel().select(newContainer);
                d.close();
                showInfo("Container added");
            } catch (IllegalArgumentException e) {
                showAlert(e.getMessage());
                ev.consume();
            }
        });
        d.showAndWait();
    }

    private void showAddSlotsDialog() {
        if (currentUser == null) {
            showAlert("Please login first");
            return;
        }
        Container sel = containerListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("Select container first");
            return;
        }
        if (sel.getOwnerId() != currentUser.getId()) {
            showAlert("Not owner");
            return;
        }
        Dialog<int[]> d = new Dialog<>();
        d.setTitle("Add slots");
        TextField rowsField = new TextField(), colsField = new TextField();
        GridPane g = new GridPane();
        g.add(new Label("Rows (1-26):"), 0, 0);
        g.add(rowsField, 1, 0);
        g.add(new Label("Columns (1+):"), 0, 1);
        g.add(colsField, 1, 1);
        d.getDialogPane().setContent(g);
        ButtonType ok = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);
        Button btn = (Button) d.getDialogPane().lookupButton(ok);
        btn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            try {
                int rows = Integer.parseInt(rowsField.getText().trim());
                int cols = Integer.parseInt(colsField.getText().trim());
                if (rows < 1 || rows > 26 || cols < 1) {
                    showAlert("Rows 1-26, Cols >=1");
                    ev.consume();
                    return;
                }
                slotService.createSlots(sel.getId(), rows, cols);
                refreshContainerList(); // обновить кэш
                detailPane.setContainer(sel);
                d.close();
                showInfo("Slots created");
            } catch (NumberFormatException e) {
                showAlert("Invalid number");
                ev.consume();
            } catch (IllegalArgumentException e) {
                showAlert(e.getMessage());
                ev.consume();
            }
        });
        d.showAndWait();
    }

    private void showAddSampleDialog() {
        if (currentUser == null) {
            showAlert("Please login first");
            return;
        }
        sampleService.add();
        showInfo("Sample added. Total: " + sampleService.getAll().size());
    }

    private void logout() {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
        try {
            new MainUI().start(new Stage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
