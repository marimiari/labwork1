package ru.itmo.marimiari.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.service.*;
import ru.itmo.marimiari.storage.FileValidator;
import ru.itmo.marimiari.storage.StorageData;
import ru.itmo.marimiari.storage.StorageException;
import ru.itmo.marimiari.storage.XmlStorage;
import ru.itmo.marimiari.user.UserStorage;

import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainUI extends Application { //класс javafx приложения, переопределяет метод старт
    private static final String SAVE_FILE = "data.xml";
    private SampleService sampleService;
    private ContainerService containerService;
    private SlotService slotService;
    private PlacementService placementService;
    private ListView<Container> containerListView;
    private ContainerDetailPane detailPane;
    private ProgressBar progressBar;
    private String currentUser = null;

    private Button addContainerButton, addSlotsButton, addSampleButton, saveButton;
    private Button loginButton, registerButton, logoutButton;

    @Override
    public void start(Stage primaryStage) {
        // Сервисы
        sampleService = new SampleService();
        containerService = new ContainerService();
        slotService = new SlotService(containerService);
        placementService = new PlacementService(sampleService, containerService, slotService);
        loadInitialData();

        // Список контейнеров
        containerListView = new ListView<>();
        containerListView.setCellFactory(lv -> new ListCell<Container>() {
            @Override
            protected void updateItem(Container c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : "#" + c.getId() + " " + c.getName() + " (" + c.getType() + ")");
            }
        });

        detailPane = new ContainerDetailPane(containerService, slotService, placementService, sampleService);
        detailPane.setCurrentUser(currentUser);
        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        // Кнопки
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshContainerList());

        addContainerButton = new Button("Add container");
        addContainerButton.setOnAction(e -> showAddContainerDialog());

        addSlotsButton = new Button("Add slots");
        addSlotsButton.setOnAction(e -> showAddSlotsDialog());

        addSampleButton = new Button("Add sample");
        addSampleButton.setOnAction(e -> showAddSampleDialog());

        saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveDataToFile());

        loginButton = new Button("Login");
        loginButton.setOnAction(e -> showLoginDialog());

        registerButton = new Button("Register");
        registerButton.setOnAction(e -> showRegisterDialog());

        logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> logout());
        logoutButton.setDisable(true);

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
        updateButtonsState();

        Scene scene = new Scene(root, 1100, 700);
        primaryStage.setTitle("Container Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateButtonsState() {
        boolean loggedIn = currentUser != null;
        addContainerButton.setDisable(!loggedIn);
        addSlotsButton.setDisable(!loggedIn);
        addSampleButton.setDisable(!loggedIn);
        saveButton.setDisable(!loggedIn);
        loginButton.setDisable(loggedIn);
        registerButton.setDisable(loggedIn);
        logoutButton.setDisable(!loggedIn);
    }

    private void showLoginDialog() {
        LoginDialog dialog = new LoginDialog();
        if (dialog.showAndWait()) {
            currentUser = dialog.getLoggedInUser();
            detailPane.setCurrentUser(currentUser);
            updateButtonsState();
            Container selected = containerListView.getSelectionModel().getSelectedItem();
            if (selected != null) detailPane.setContainer(selected);
            showInfo("Logged in as " + currentUser);
        }
    }

    private void showRegisterDialog() {
        TextInputDialog loginDlg = new TextInputDialog();
        loginDlg.setTitle("Registration");
        loginDlg.setHeaderText("Enter login");
        Optional<String> loginRes = loginDlg.showAndWait();
        if (!loginRes.isPresent()) return;
        String login = loginRes.get().trim();
        if (login.isEmpty()) {
            showAlert("Login cannot be empty");
            return;
        }

        TextInputDialog passDlg = new TextInputDialog();
        passDlg.setTitle("Registration");
        passDlg.setHeaderText("Enter password");
        Optional<String> passRes = passDlg.showAndWait();
        if (!passRes.isPresent()) return;
        String password = passRes.get();
        if (password.isEmpty()) {
            showAlert("Password cannot be empty");
            return;
        }

        UserStorage storage = new UserStorage();
        if (storage.register(login, password)) {
            showInfo("Registration successful. Please login.");
        } else {
            showAlert("Login already exists");
        }
    }

    private void logout() {
        currentUser = null;
        detailPane.setCurrentUser(null);
        updateButtonsState();
        Container selected = containerListView.getSelectionModel().getSelectedItem();
        if (selected != null) detailPane.setContainer(selected);
        showInfo("Logged out");
    }

    private void loadInitialData() {
        Path path = Paths.get(SAVE_FILE);
        if (Files.exists(path)) {
            try {
                StorageData data = XmlStorage.load(path);
                FileValidator.validate(data);
                sampleService.clear();
                sampleService.addAll(data.getSamples());
                containerService.clear();
                containerService.addAll(data.getContainers());
                slotService.clear();
                slotService.addAll(data.getSlots());
                placementService.clear();
                placementService.addAll(data.getPlacements());
            } catch (Exception e) {
                e.printStackTrace();
                loadDemoData();
            }
        } else {
            loadDemoData();
        }
    }

    private void loadDemoData() {
        for (int i = 0; i < 3; i++) sampleService.add("SYSTEM");
        long c1 = containerService.add("Demo-Freezer", ContainerType.FREEZER, "SYSTEM").getId();
        long c2 = containerService.add("Demo-Fridge", ContainerType.FRIDGE, "SYSTEM").getId();
        slotService.createSlots(c1, 2, 3, "SYSTEM");
        slotService.createSlots(c2, 2, 2, "SYSTEM");
        placementService.add(1, c1, "A1", "SYSTEM");
        placementService.add(2, c1, "B2", "SYSTEM");
    }

    private void autoSave() {
        try {
            XmlStorage.save(Paths.get(SAVE_FILE), sampleService, containerService, slotService, placementService);
        } catch (StorageException e) {
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    private void saveDataToFile() {
        if (currentUser == null) {
            showAlert("Please login first");
            return;
        }
        showProgress(true);
        new Thread(() -> {
            try {
                XmlStorage.save(Paths.get(SAVE_FILE), sampleService, containerService, slotService, placementService);
                Platform.runLater(() -> {
                    showInfo("Saved");
                    showProgress(false);
                });
            } catch (StorageException e) {
                Platform.runLater(() -> {
                    showAlert("Save failed: " + e.getMessage());
                    showProgress(false);
                });
            }
        }).start();
    }

    private void refreshContainerList() {
        showProgress(true);
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> {
                reloadFromFile();
                List<Container> containers = List.copyOf(containerService.getAll());
                containerListView.setItems(FXCollections.observableArrayList(containers));
                Container selected = containerListView.getSelectionModel().getSelectedItem();
                if (selected != null && containers.contains(selected))
                    detailPane.setContainer(selected);
                else
                    detailPane.setContainer(null);
                showProgress(false);
            });
        }).start();
    }

    private void reloadFromFile() {
        Path path = Paths.get(SAVE_FILE);
        if (!Files.exists(path)) return;
        try {
            StorageData data = XmlStorage.load(path);
            FileValidator.validate(data);
            sampleService.clear();
            sampleService.addAll(data.getSamples());
            containerService.clear();
            containerService.addAll(data.getContainers());
            slotService.clear();
            slotService.addAll(data.getSlots());
            placementService.clear();
            placementService.addAll(data.getPlacements());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgress(boolean show) {
        Platform.runLater(() -> progressBar.setVisible(show));
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
                Container newCont = containerService.add(name, type, currentUser);
                autoSave();
                ObservableList<Container> items = FXCollections.observableArrayList(containerListView.getItems());
                items.add(newCont);
                containerListView.setItems(items);
                containerListView.getSelectionModel().select(newCont);
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
        if (!sel.getOwnerUsername().equals(currentUser)) {
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
                slotService.createSlots(sel.getId(), rows, cols, currentUser);
                autoSave();
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
        sampleService.add(currentUser);
        autoSave();
        showInfo("Sample added. Total: " + sampleService.getAll().size());
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
