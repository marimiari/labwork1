package ru.itmo.marimiari.ui;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.service.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerDetailPane extends VBox { //вертикальный контейнер, элементы друг под другом
    private final TextField nameField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>(); //выпадающий список
    private final Label ownerLabel = new Label();
    private final Label createdLabel = new Label();
    private final Label updatedLabel = new Label();
    private final TableView<Slot> slotTable = new TableView<>(); //для вывода таблицы
    private final TableView<Placement> placementTable = new TableView<>();

    private Container currentContainer; //текущий контейнер, для которого показываются детали
    private final ContainerService containerService;
    private final SlotService slotService;
    private final PlacementService placementService;
    private final SampleService sampleService;
    private final String currentUser;

    private Button updateButton;
    private Button addSlotsButton;

    public ContainerDetailPane(ContainerService containerService, SlotService slotService, PlacementService placementService, SampleService sampleService, String currentUser){
        this.containerService = containerService;
        this.slotService = slotService;
        this.placementService = placementService;
        this.sampleService = sampleService;
        this.currentUser = currentUser;
        initUI(); //построение интерфейса
    }

    private void initUI(){
        typeBox.getItems().addAll("FREEZER","FRIDGE","BOX");
        GridPane grid = new GridPane(); //создание сетки
        grid.setHgap(10); //отступы
        grid.setVgap(5);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeBox, 1, 1);
        grid.add(new Label("Owner:"), 0, 2);
        grid.add(ownerLabel, 1, 2);
        grid.add(new Label("Created:"), 0, 3);
        grid.add(createdLabel, 1, 3);
        grid.add(new Label("Updated:"), 0, 4);
        grid.add(updatedLabel, 1, 4);

        Button updateButton = new Button("Update"); //какая кнопка за какой метод отвечает
        updateButton.setOnAction(e -> updateContainer());
        grid.add(updateButton,1,5);

        TableColumn<Slot, String> codeCol = new TableColumn<>("Code"); //создает колонку таблицы, в которой строки - объекты slot, а в каждой ячейке текст string
        codeCol.setCellValueFactory(cellData -> //setCellValueFactory задаёт правило, как из объекта slot (который находится в текущей строке таблицы) получить значение для отображения в ячейке
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCode()));
        TableColumn<Slot, String> occupiedCol = new TableColumn<>("Occupied"); //создает колонку, которая будет отображать строки string, а данные берёт из объектов slot
        occupiedCol.setCellValueFactory(cellData -> //задаёт правило: для каждого слота в строке таблицы берем его поле isOccupied()
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isOccupied() ? "YES" : "NO")); //и превращаем его в строку
        slotTable.getColumns().addAll(codeCol, occupiedCol); //добавляет обе колонки в таблицу
        slotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //колонки растягивает в ширину

        TableColumn<Placement, Long> sampleIdCol = new TableColumn<>("Sample ID");
        sampleIdCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(cellData.getValue().getSampleId()).asObject());
        TableColumn<Placement, String> slotCodeCol = new TableColumn<>("Slot"); //получаем объект слот и берем его код
        slotCodeCol.setCellValueFactory(cellData -> {
            Slot slot = slotService.get(cellData.getValue().getSlotId()).orElse(null);
            return new javafx.beans.property.SimpleStringProperty(slot != null ? slot.getCode() : "?");
        });
        placementTable.getColumns().addAll(sampleIdCol, slotCodeCol);
        placementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //добавление колонок в таблицу размещений

        Button placeButton = new Button("Place sample");
        placeButton.setOnAction(e -> placeSample());
        Button moveButton = new Button("Move sample");
        moveButton.setOnAction(e -> moveSample());
        Button removeSelectedButton = new Button("Remove sample");
        removeSelectedButton.setOnAction(e -> removeSelectedSample());
        addSlotsButton = new Button("Add slots");
        addSlotsButton.setOnAction(e -> showAddSlotsDialog()); //обозначаем кнопки

        VBox buttonsBox = new VBox(5, placeButton, moveButton, removeSelectedButton, addSlotsButton); //выстраиваем кнопки в колонку с отступом

        this.getChildren().addAll(
                new Label("Container details:"), grid,
                new Label("Slots:"), slotTable,
                new Label("Placements:"), placementTable,
                buttonsBox); //добавляет все элементы в главное место с информацией
        this.setSpacing(10);
        this.setPadding(new javafx.geometry.Insets(10)); //отступы + от края
        setContainer(null); //если нет контейнера изначально, чистит поля
    }

    public void setContainer(Container container){
        this.currentContainer = container;
        if (container == null) {
            nameField.clear();
            typeBox.getSelectionModel().clearSelection();
            ownerLabel.setText("");
            createdLabel.setText("");
            updatedLabel.setText("");
            slotTable.getItems().clear();
            placementTable.getItems().clear();
            if (updateButton != null)
                updateButton.setDisable(true);
            if (addSlotsButton != null)
                addSlotsButton.setDisable(true);
            return;
        }

        nameField.setText(container.getName());
        typeBox.setValue(container.getType().name());
        ownerLabel.setText(container.getOwnerUsername());
        createdLabel.setText(container.getCreatedAt().toString());
        updatedLabel.setText(container.getUpdatedAt().toString()); //заполнение полей данными

        slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(container.getId())));
        refreshPlacements();

        boolean isOwner = container.getOwnerUsername().equals(currentUser);
        if (updateButton != null) updateButton.setDisable(!isOwner);
        if (addSlotsButton != null) addSlotsButton.setDisable(!isOwner);
    }

    private void refreshPlacements(){
        if (currentContainer == null)
            return;
        List<Placement> placements = placementService.getAll().stream()
                .filter(p -> p.getContainerId() == currentContainer.getId())
                .collect(Collectors.toList()); //берем все размещения из placementService, оставляем только те, у которых containerId совпадает с текущим
        placementTable.getItems().setAll(FXCollections.observableArrayList(placements)); //обновляем
    }

    private void updateContainer(){
        if (currentContainer == null)
            return;
        if (!currentContainer.getOwnerUsername().equals(currentUser)) {
            showAlert("You are not the owner");
            return;
        }
        String newName = nameField.getText().trim();
        if (newName.isEmpty()){
            showAlert("Name cannot be empty");
            return;
        }
        String typeStr = typeBox.getValue();
        if (typeStr == null){
            showAlert("Please select a type");
            return;
        }
        try{
            ContainerType type = ContainerType.valueOf(typeStr); //преобразуем строку в enum и вызываем метод обновления в containerService
            containerService.update(currentContainer.getId(), newName, type, currentUser);
            Container updated = containerService.get(currentContainer.getId()).orElse(null);
            setContainer(updated);
            showInfo("Container updated successfully");
        } catch (IllegalArgumentException e){
            showAlert("Updated failed: " + e.getMessage());
        }
    }

    private void showAddSlotsDialog() {
        if (currentContainer == null) return;
        if (!currentContainer.getOwnerUsername().equals(currentUser)) {
            showAlert("You are not the owner");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Add slots");
        dialog.setHeaderText("Create slots for container: " + currentContainer.getName());

        TextField rowsField = new TextField();
        TextField colsField = new TextField();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
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
                slotService.createSlots(currentContainer.getId(), rows, cols, currentUser);
                slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
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

    private void placeSample(){
        if (currentContainer == null){
            showAlert("No container selected");
            return;
        }
        List<Long> sampleIds = sampleService.getAll().stream()
                .map(Sample::getId)
                .collect(Collectors.toList());
        if (sampleIds.isEmpty()) {
            showAlert("No samples available. Please add a sample first.");
            return;
        }
        ChoiceDialog<Long> sampleDialog = new ChoiceDialog<>(sampleIds.get(0), sampleIds);
        sampleDialog.setTitle("Place sample");
        sampleDialog.setHeaderText("Select sample ID");
        Optional<Long> sampleOptional = sampleDialog.showAndWait();
        if (!sampleOptional.isPresent()) return;
        long sampleId = sampleOptional.get();

        List<Slot> freeSlots = slotService.getByContainer(currentContainer.getId()).stream()
                .filter(slot -> !slot.isOccupied())
                .collect(Collectors.toList());
        if (freeSlots.isEmpty()){
            showAlert("No free slots in this container");
            return;
        }
        ChoiceDialog<Slot> slotDialog = new ChoiceDialog<>(freeSlots.get(0), freeSlots);
        slotDialog.setTitle("Select slot");
        slotDialog.setHeaderText("Choose a free slot");
        Optional<Slot> slotOptional = slotDialog.showAndWait();
        if (!slotOptional.isPresent()) return;
        Slot slot = slotOptional.get();

        try {
            placementService.add(sampleId, currentContainer.getId(), slot.getCode(), currentUser);
            refreshPlacements();
            slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample placed successfully");
        } catch (IllegalArgumentException e) {
            showAlert("Placement failed: " + e.getMessage());
        }
    }

    private void moveSample(){
        if (currentContainer == null){
            showAlert("No container selected");
            return;
        }
        Placement selectedPlacement = placementTable.getSelectionModel().getSelectedItem();
        if (selectedPlacement == null) {
            showAlert("Please select a sample from the placements table");
            return;
        }
        long sampleId = selectedPlacement.getSampleId();

        Dialog<AbstractMap.SimpleEntry<Long, String>> dialog = new Dialog<>();
        dialog.setTitle("Move sample");
        dialog.setHeaderText("Move sample " + sampleId + " to:");

        ComboBox<Container> containerCombo = new ComboBox<>();
        containerCombo.getItems().addAll(containerService.getAll());
        containerCombo.setConverter(new javafx.util.StringConverter<Container>() {
            @Override
            public String toString(Container c) {
                return c != null ? "#" + c.getId() + " " + c.getName() + " (" + c.getType() + ")" : "";
            }
            @Override
            public Container fromString(String s) { return null; }
        });
        if (!containerCombo.getItems().isEmpty()) containerCombo.getSelectionModel().selectFirst();

        ComboBox<String> slotCombo = new ComboBox<>();
        slotCombo.setDisable(true);
        containerCombo.valueProperty().addListener((obs, old, newContainer) -> {
            if (newContainer != null) {
                List<String> freeSlots = slotService.getByContainer(newContainer.getId()).stream()
                        .filter(s -> !s.isOccupied())
                        .map(Slot::getCode)
                        .collect(Collectors.toList());
                slotCombo.getItems().setAll(freeSlots);
                slotCombo.setDisable(freeSlots.isEmpty());
                if (!freeSlots.isEmpty()) slotCombo.getSelectionModel().selectFirst();
            } else {
                slotCombo.getItems().clear();
                slotCombo.setDisable(true);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("New container:"), 0, 0);
        grid.add(containerCombo, 1, 0);
        grid.add(new Label("New slot:"), 0, 1);
        grid.add(slotCombo, 1, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Container targetContainer = containerCombo.getValue();
                String targetSlot = slotCombo.getValue();
                if (targetContainer != null && targetSlot != null) {
                    return new AbstractMap.SimpleEntry<>(targetContainer.getId(), targetSlot);
                }
            }
            return null;
        });

        Optional<AbstractMap.SimpleEntry<Long, String>> result = dialog.showAndWait();
        result.ifPresent(entry -> {
            long newContainerId = entry.getKey();
            String newSlotCode = entry.getValue();
            try {
                placementService.move(sampleId, newContainerId, newSlotCode, currentUser);
                refreshPlacements();
                slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
                showInfo("Sample moved");
            } catch (IllegalArgumentException e) {
                showAlert("Move failed: " + e.getMessage());
            }
        });
    }

    private void removeSelectedSample() {
        Placement selected = placementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No sample selected");
            return;
        }
        if (!selected.getOwnerUsername().equals(currentUser)) {
            showAlert("You are not the owner of this placement");
            return;
        }
        try {
            placementService.removeBySample(selected.getSampleId(), currentUser);
            refreshPlacements();
            slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample removed");
        } catch (IllegalArgumentException e) {
            showAlert("Remove failed: " + e.getMessage());
        }
    }

    private void showAlert(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait(); //показывает окно с крестиком при ошибке
    }

    private void showInfo(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.showAndWait(); //показывает информационное окно с синим значком
    }
}