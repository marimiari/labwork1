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
    private String currentUser;

    @Override
    public void start(Stage primaryStage) {
        LoginDialog loginDialog = new LoginDialog();
        if (!loginDialog.showAndWait()) {
            Platform.exit();
            return;
        }
        currentUser = loginDialog.getLoggedInUser();

        sampleService = new SampleService();
        containerService = new ContainerService();
        slotService = new SlotService(containerService);
        placementService = new PlacementService(sampleService, containerService, slotService);

        loadDemoData(); //загружает данные из файла или создаёт демо-данные

        containerListView = new ListView<>(); //список для отображения контейнеров
        detailPane = new ContainerDetailPane(containerService, slotService, placementService, sampleService, currentUser); // передаем сервисы в его конструктор, чтобы он мог вызывать их методы
        progressBar = new ProgressBar();
        progressBar.setVisible(false); //создается и скрыт по умолчанию

        Button refreshButton = new Button("Refresh"); //присваиваем кнопкам их действия
        refreshButton.setOnAction(e -> refreshContainerList());

        Button addContainerButton = new Button("Add container");
        addContainerButton.setOnAction(e -> showAddContainerDialog());

        Button addSlotsButton = new Button("Add slots");
        addSlotsButton.setOnAction(e -> showAddSlotsDialog());

        Button addSampleButton = new Button("Add Sample");
        addSampleButton.setOnAction(e -> showAddSampleDialog());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveDataToFile());

        VBox leftPane = new VBox(10, new Label("Containers"), containerListView, refreshButton, addContainerButton, addSlotsButton, progressBar);
        leftPane.setPadding(new javafx.geometry.Insets(10)); //вертикальная панель слева: заголовок, списки, кнопки и тд

        BorderPane root = new BorderPane(); //макет
        root.setLeft(leftPane); //список - слева
        root.setCenter(detailPane); //детали - справа

        containerListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> detailPane.setContainer(newVal));
        //при выборе другого контейнера в списке вызывается detailPane.setContainer(newVal), который обновляет правую панель
        refreshContainerList(); //первоначальное заполнение списка контейнеров

        Scene scene = new Scene(root, 1100, 700); //создание сцены, те самого интерфейса
        primaryStage.setTitle("Container Manager"); //заголовок окна
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadInitialData() { //загружаем или файл, или демо-данные
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
                System.out.println("Loaded data from " + SAVE_FILE);
            } catch (Exception e) {
                e.printStackTrace(); //метод, показывающий в каком месте и в какой последовательности методов произошла ошибка
                //какое исключение было выброшено и тд; дает больше деталей чем просто принт
                loadDemoData();
            }
        } else {
                loadDemoData();
        }
    }

    private void loadDemoData() { //создает начальные образцы, контейнеры, слоты, размещения (для теста)
        for (int i = 0; i < 5; i++) //используется только если файл не загрузился
            sampleService.add(currentUser);

        long cont1 = containerService.add("test-1", ContainerType.FREEZER, currentUser).getId();
        long cont2 = containerService.add("test-2", ContainerType.FRIDGE, currentUser).getId();

        slotService.createSlots(cont1, 3, 4, currentUser);
        slotService.createSlots(cont2, 2, 3, currentUser);

        placementService.add(1, cont1, "A1", currentUser);
        placementService.add(2, cont1, "B2", currentUser);
    }

    private void autoSave() {
        try {
            XmlStorage.save(Paths.get(SAVE_FILE), sampleService, containerService, slotService, placementService);
        } catch (StorageException e) {
            System.err.println("Auto-save failed: " + e.getMessage());
        }
    }

    private void saveDataToFile() {
        showProgress(true);
        new Thread(() -> { //показывает прогресс-бар, запускает отдельный поток (new Thread), чтобы окно не зависало
            try {
                XmlStorage.save(Paths.get(SAVE_FILE), sampleService, containerService, slotService, placementService); //сохранение
                Platform.runLater(() -> { //возвращает управление в главный поток, чтоб обновить ui
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Data saved to " + SAVE_FILE, ButtonType.OK);
                    alert.showAndWait();
                    showProgress(false);
                });
            } catch (StorageException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage(), ButtonType.OK);
                    alert.showAndWait();
                    showProgress(false);
                });
            }
        }).start();
    }
    //в потоке вызывается XmlStorage.save.
    // после успеха (или ошибки) через Platform.runLater возвращаемся в javafx-поток и показываем диалог, скрываем прогресс

    private void refreshContainerList() {
        showProgress(true); //показывает прогресс
        new Thread(() -> { //запускает поток с искусственной задержкой
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> {
                reloadFromFile(); //загружаем свежие данные
                List<Container> containers = List.copyOf(containerService.getAll()); //обновляем список
                containerListView.setItems(FXCollections.observableArrayList(containers));
                Container selected = containerListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    if (containers.contains(selected)) {
                        detailPane.setContainer(selected);
                    } else {
                        detailPane.setContainer(null);
                    }
                }
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
            sampleService.clear(); sampleService.addAll(data.getSamples());
            containerService.clear(); containerService.addAll(data.getContainers());
            slotService.clear(); slotService.addAll(data.getSlots());
            placementService.clear(); placementService.addAll(data.getPlacements());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgress(boolean show) { //обертка, чтобы менять видимость из любого потока
        Platform.runLater(() -> progressBar.setVisible(show)); //вызов обернут в Platform.runLater
    }

    private void showAddContainerDialog() {
        Dialog<Container> dialog = new Dialog<>();
        dialog.setTitle("Add Container");
        dialog.setHeaderText("Enter container details");

        TextField nameField = new TextField();
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("FREEZER", "FRIDGE", "BOX");
        TextField ownerField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Owner:"), 0, 2);
        grid.add(ownerField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.addEventFilter(ActionEvent.ACTION, event -> {
            String name = nameField.getText().trim();
            String typeStr = typeBox.getValue();
            String owner = ownerField.getText().trim();

            if (name.isEmpty()) {
                showAlert("Name cannot be empty");
                event.consume();
                return;
            }
            if (typeStr == null) {
                showAlert("Please select a type");
                event.consume();
                return;
            }
            if (owner.isEmpty()) {
                showAlert("Owner cannot be empty");
                event.consume();
                return;
            }

            try {
                ContainerType type = ContainerType.valueOf(typeStr);
                Container newContainer = containerService.add(name, type, owner);

                try {
                    XmlStorage.save(Paths.get(SAVE_FILE), sampleService, containerService, slotService, placementService);
                } catch (StorageException e){
                    showAlert("Auto-save failed: " + e.getMessage());
                }
                ObservableList<Container> items = FXCollections.observableArrayList(containerListView.getItems());
                items.add(newContainer);
                containerListView.setItems(items);
                containerListView.getSelectionModel().select(newContainer);
                dialog.close();
                showInfo("Container added");
            } catch (IllegalArgumentException e){
                showAlert("Failed to add container: " + e.getMessage());
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void showAddSlotsDialog() {
        Container selected = containerListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select a container first");
            return;
        }

        if (!selected.getOwnerUsername().equals(currentUser)) {
            showAlert("You are not the owner of this container");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Add slots");
        dialog.setHeaderText("Create slots for container: " + selected.getName());

        TextField rowsField = new TextField();
        TextField colsField = new TextField();
        GridPane grid = new GridPane();
        grid.add(new Label("Rows (1-26):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Columns (1+):"), 0, 1);
        grid.add(colsField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createButton);
        createBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                int rows = Integer.parseInt(rowsField.getText().trim());
                int cols = Integer.parseInt(colsField.getText().trim());
                if (rows < 1 || rows > 26) {
                    showAlert("Rows must be between 1 and 26");
                    event.consume();
                    return;
                }
                if (cols < 1) {
                    showAlert("Columns must be at least 1");
                    event.consume();
                    return;
                }
                slotService.createSlots(selected.getId(), rows, cols, currentUser);
                autoSave();
                detailPane.setContainer(selected);
                dialog.close();
                showInfo("Slots created");
            } catch (NumberFormatException e) {
                showAlert("Invalid number");
                event.consume();
            } catch (IllegalArgumentException e) {
                showAlert("Creation failed: " + e.getMessage());
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    private void showAddSampleDialog() {
        sampleService.add(currentUser);
        autoSave();
        showInfo("New sample created. Total samples: " + sampleService.getAll().size());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
