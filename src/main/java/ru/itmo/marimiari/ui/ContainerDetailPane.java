package ru.itmo.marimiari.ui;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.service.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ContainerDetailPane extends VBox { //вертикальный контейнер, элементы друг под другом
    private final TextField nameField = new TextField();
    private final ComboBox<String> typeBox = new ComboBox<>();
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

    public ContainerDetailPane(ContainerService containerService, SlotService slotService, PlacementService placementService, SampleService sampleService){
        this.containerService = containerService;
        this.slotService = slotService;
        this.placementService = placementService;
        this.sampleService = sampleService;
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
        TableColumn<Slot, String> occupiedCol = new TableColumn<>("Occupied");
        occupiedCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().isOccupied() ? "YES" : "NO"));
        slotTable.getColumns().addAll(codeCol, occupiedCol);
        slotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); //колонки и растягивает в ширину

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
        Button removeButton = new Button("Remove sample");
        removeButton.setOnAction(e -> removeSample()); //обозначаем кнопки

        VBox buttonsBox = new VBox(5, placeButton, moveButton, removeButton); //выстраиваем кнопки в колонку с отступом

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
        if (container == null){ //если нет контейнера, все пустое
            nameField.clear();
            typeBox.getSelectionModel().clearSelection();
            ownerLabel.setText("");
            createdLabel.setText("");
            updatedLabel.setText("");
            slotTable.getItems().clear();
            placementTable.getItems().clear();
            return;
        }

        nameField.setText(container.getName());
        typeBox.setValue(container.getType().name());
        ownerLabel.setText(container.getOwnerUsername());
        createdLabel.setText(container.getCreatedAt().toString());
        updatedLabel.setText(container.getUpdatedAt().toString()); //заполнение полей данными

        slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(container.getId())));
        refreshPlacements(); //загружаем список слотов, помещаем в таблицу и обновляем
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
            containerService.update(currentContainer.getId(), newName, type);
            Container updated = containerService.get(currentContainer.getId()).orElse(null);
            setContainer(updated);
            showInfo("Container updated successfully");
        } catch (IllegalArgumentException e){
            showAlert("Updated failed: " + e.getMessage());
        }
    }

    private void placeSample(){
        if (currentContainer == null){
            showAlert("No container selected");
            return;
        }
        TextInputDialog sampleDialog = new TextInputDialog(); //диалоговое окно для ввода
        sampleDialog.setTitle("Place sample");
        sampleDialog.setHeaderText("Enter sample ID");
        Optional<String> sampleResult = sampleDialog.showAndWait(); //showAndWait() показывает диалог и блокирует выполнение программы до тех пор, пока пользователь не закроет диалог
        if (!sampleResult.isPresent()){ //возвращает Optional<String> (контейнер, который может либо содержать значение, либо быть пустым в зависимости от действий пользователя)
            return;
        }
        long sampleId;
        try{
            sampleId = Long.parseLong(sampleResult.get());
        } catch (NumberFormatException e){
            showAlert("Invalid sample ID");
            return;
        }
        TextInputDialog slotDialog = new TextInputDialog(); //диалоговое окно для ввода кода
        slotDialog.setTitle("Place Sample");
        slotDialog.setHeaderText("Enter slot code (e.g., A1)");
        Optional<String> slotResult = slotDialog.showAndWait();
        if (!slotResult.isPresent()) return;
        String slotCode = slotResult.get().trim();
        if (slotCode.isEmpty()) {
            showAlert("Slot code cannot be empty");
            return;
        }
        try{
            placementService.add(sampleId, currentContainer.getId(), slotCode, "ui_user"); //добавление размещения
            refreshPlacements();
            slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample placed successfully");
        } catch (IllegalArgumentException e){
            showAlert("Placement failed: " + e.getMessage());
        }
    }

    private void moveSample(){
        if (currentContainer == null){
            showAlert("No container selected");
            return;
        }
        TextInputDialog sampleIdDialog = new TextInputDialog();
        sampleIdDialog.setTitle("Move sample");
        sampleIdDialog.setHeaderText("Enter sample ID to move");
        Optional<String> sampleResult = sampleIdDialog.showAndWait();
        if (!sampleResult.isPresent()){
            return;
        }
        long sampleId;
        try {
            sampleId = Long.parseLong(sampleResult.get());
        } catch (NumberFormatException e) {
            showAlert("Invalid sample ID");
            return;
        }
        if (!sampleService.exists(sampleId)) {
            showAlert("Sample " + sampleId + " does not exist");
            return;
        }

        Optional<Placement> existing = placementService.findBySample(sampleId);
        if (!existing.isPresent()){
            showAlert("Sample " + sampleId + " is not placed anywhere");
            return;
        }

        TextInputDialog containerDialog = new TextInputDialog();
        containerDialog.setTitle("Move sample");
        containerDialog.setHeaderText("Enter new container ID");
        Optional<String> containerResult = containerDialog.showAndWait();
        if (!containerResult.isPresent()){
            return;
        }
        long newContainerId;
        try {
            newContainerId = Long.parseLong(containerResult.get());
        } catch (NumberFormatException e) {
            showAlert("Invalid container ID");
            return;
        }
        if (!containerService.exists(newContainerId)) {
            showAlert("Container not found");
            return;
        }
        TextInputDialog slotDialog = new TextInputDialog();
        slotDialog.setTitle("Move Sample");
        slotDialog.setHeaderText("Enter new slot code (e.g., A1)");
        Optional<String> slotResult = slotDialog.showAndWait();
        if (!slotResult.isPresent()) return;
        String newSlotCode = slotResult.get().trim();
        if (newSlotCode.isEmpty()) {
            showAlert("Slot code cannot be empty");
            return;
        }

        try{
            placementService.move(sampleId, newContainerId, newSlotCode, "ui_user");
            slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            refreshPlacements(); //обновляем таблицы текущего контейнера
            if (currentContainer.getId() != newContainerId){
                showInfo("Sample moved to container " + newContainerId);
            } else {
                showInfo("Sample moved to new slot successfully");
            }
        } catch (IllegalArgumentException e){
            showAlert("Move failed: " + e.getMessage());
        }
    }

    private void removeSample(){
        if (currentContainer == null){
            showAlert("No container selected");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Remove sample");
        dialog.setHeaderText("Enter sample ID to remove");
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent()){
            return;
        }
        long sampleId;
        try{
            sampleId = Long.parseLong(result.get());
        } catch (NumberFormatException e){
            showAlert("Invalid sample ID");
            return;
        }
        Optional<Placement> existing = placementService.findBySample(sampleId);
        if (!existing.isPresent()){
            showAlert("Sample " + sampleId + " is not placed anywhere");
            return;
        }
        Placement placement = existing.get();
        if (placement.getContainerId() != currentContainer.getId()){
            showAlert("Sample " + sampleId + " is not in this container");
            return;
        }
        try{
            placementService.removeBySample(sampleId);
            refreshPlacements();
            slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample removed successfully");
        } catch (IllegalArgumentException e){
            showAlert("Remove failed:" + e.getMessage());
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