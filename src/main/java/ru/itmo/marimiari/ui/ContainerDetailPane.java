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
    private final ComboBox<String> typeBox = new ComboBox<>();
    private final Label ownerLabel = new Label();
    private final Label createdLabel = new Label();
    private final Label updatedLabel = new Label();
    private final TableView<Slot> slotTable = new TableView<>();
    private final TableView<Placement> placementTable = new TableView<>();

    private Container currentContainer;
    private final ContainerService containerService;
    private final SlotService slotService;
    private final PlacementService placementService;
    private final SampleService sampleService;
    private String currentUser;

    private Button updateButton, addSlotsButton, placeButton, moveButton, removeButton;

    public ContainerDetailPane(ContainerService containerService, SlotService slotService,
                               PlacementService placementService, SampleService sampleService) {
        this.containerService = containerService;
        this.slotService = slotService;
        this.placementService = placementService;
        this.sampleService = sampleService;
        initUI();
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
        if (currentContainer != null) setContainer(currentContainer);
        else disableAllButtons(true);
    }

    private void disableAllButtons(boolean disable) {
        if (updateButton != null) updateButton.setDisable(disable);
        if (addSlotsButton != null) addSlotsButton.setDisable(disable);
        if (placeButton != null) placeButton.setDisable(disable);
        if (moveButton != null) moveButton.setDisable(disable);
        if (removeButton != null) removeButton.setDisable(disable);
    }

    private void initUI() {
        typeBox.getItems().addAll("FREEZER", "FRIDGE", "BOX");
        GridPane grid = new GridPane();
        grid.setHgap(10);
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
        updateButton = new Button("Update");
        updateButton.setOnAction(e -> updateContainer());
        grid.add(updateButton, 1, 5);

        TableColumn<Slot, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getCode()));
        TableColumn<Slot, String> occCol = new TableColumn<>("Occupied");
        occCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().isOccupied() ? "YES" : "NO"));
        slotTable.getColumns().addAll(codeCol, occCol);
        slotTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Placement, Long> sidCol = new TableColumn<>("Sample ID");
        sidCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleLongProperty(cell.getValue().getSampleId()).asObject());
        TableColumn<Placement, String> scCol = new TableColumn<>("Slot");
        scCol.setCellValueFactory(cell -> {
            Slot s = slotService.get(cell.getValue().getSlotId()).orElse(null);
            return new javafx.beans.property.SimpleStringProperty(s == null ? "?" : s.getCode());
        });
        placementTable.getColumns().addAll(sidCol, scCol);
        placementTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        placeButton = new Button("Place sample");
        placeButton.setOnAction(e -> placeSample());
        moveButton = new Button("Move sample");
        moveButton.setOnAction(e -> moveSample());
        removeButton = new Button("Remove sample");
        removeButton.setOnAction(e -> removeSelectedSample());
        addSlotsButton = new Button("Add slots");
        addSlotsButton.setOnAction(e -> showAddSlotsDialog());

        VBox btns = new VBox(5, placeButton, moveButton, removeButton, addSlotsButton);
        this.getChildren().addAll(new Label("Container details:"), grid,
                new Label("Slots:"), slotTable,
                new Label("Placements:"), placementTable, btns);
        this.setSpacing(10);
        this.setPadding(new javafx.geometry.Insets(10));
        setContainer(null);
    }

    public void setContainer(Container container) {
        this.currentContainer = container;
        if (container == null) {
            nameField.clear();
            typeBox.getSelectionModel().clearSelection();
            ownerLabel.setText("");
            createdLabel.setText("");
            updatedLabel.setText("");
            slotTable.getItems().clear();
            placementTable.getItems().clear();
            disableAllButtons(true);
            return;
        }
        nameField.setText(container.getName());
        typeBox.setValue(container.getType().name());
        ownerLabel.setText(container.getOwnerUsername());
        createdLabel.setText(container.getCreatedAt().toString());
        updatedLabel.setText(container.getUpdatedAt().toString());

        slotTable.getItems().setAll(FXCollections.observableArrayList(slotService.getByContainer(container.getId())));
        refreshPlacements();

        boolean canEdit = (currentUser != null && currentUser.equals(container.getOwnerUsername()));
        updateButton.setDisable(!canEdit);
        addSlotsButton.setDisable(!canEdit);
        placeButton.setDisable(!canEdit);
        moveButton.setDisable(!canEdit);
        removeButton.setDisable(!canEdit);
    }

    private void refreshPlacements() {
        if (currentContainer == null) return;
        List<Placement> list = placementService.getAll().stream()
                .filter(p -> p.getContainerId() == currentContainer.getId()).collect(Collectors.toList());
        placementTable.getItems().setAll(FXCollections.observableArrayList(list));
    }

    private void updateContainer() {
        if (currentContainer == null || currentUser == null) return;
        if (!currentUser.equals(currentContainer.getOwnerUsername())) {
            showAlert("Not owner");
            return;
        }
        String newName = nameField.getText().trim();
        if (newName.isEmpty()) {
            showAlert("Name empty");
            return;
        }
        String typeStr = typeBox.getValue();
        if (typeStr == null) {
            showAlert("Select type");
            return;
        }
        try {
            ContainerType type = ContainerType.valueOf(typeStr);
            containerService.update(currentContainer.getId(), newName, type, currentUser);
            Container updated = containerService.get(currentContainer.getId()).orElse(null);
            setContainer(updated);
            showInfo("Container updated");
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    private void showAddSlotsDialog() {
        if (currentContainer == null || currentUser == null) return;
        if (!currentUser.equals(currentContainer.getOwnerUsername())) {
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
                slotService.createSlots(currentContainer.getId(), rows, cols, currentUser);
                setContainer(currentContainer);
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

    private void placeSample() {
        if (currentContainer == null || currentUser == null) {
            showAlert("No container selected");
            return;
        }
        if (!currentUser.equals(currentContainer.getOwnerUsername())) {
            showAlert("Not owner");
            return;
        }
        List<Long> sampleIds = sampleService.getAll().stream().map(Sample::getId).collect(Collectors.toList());
        if (sampleIds.isEmpty()) {
            showAlert("No samples available");
            return;
        }
        ChoiceDialog<Long> sd = new ChoiceDialog<>(sampleIds.get(0), sampleIds);
        sd.setTitle("Select sample");
        Optional<Long> sid = sd.showAndWait();
        if (!sid.isPresent()) return;
        long sampleId = sid.get();
        List<Slot> freeSlots = slotService.getByContainer(currentContainer.getId()).stream()
                .filter(s -> !s.isOccupied()).collect(Collectors.toList());
        if (freeSlots.isEmpty()) {
            showAlert("No free slots");
            return;
        }
        ChoiceDialog<Slot> sl = new ChoiceDialog<>(freeSlots.get(0), freeSlots);
        sl.setTitle("Select slot");
        Optional<Slot> slotOpt = sl.showAndWait();
        if (!slotOpt.isPresent()) return;
        Slot slot = slotOpt.get();
        try {
            placementService.add(sampleId, currentContainer.getId(), slot.getCode(), currentUser);
            refreshPlacements();
            slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample placed");
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
        }
    }

    private void moveSample() {
        if (currentContainer == null || currentUser == null) {
            showAlert("No container selected");
            return;
        }
        if (!currentUser.equals(currentContainer.getOwnerUsername())) {
            showAlert("Not owner");
            return;
        }
        Placement selected = placementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a placement to move");
            return;
        }
        long sampleId = selected.getSampleId();
        Dialog<AbstractMap.SimpleEntry<Long, String>> d = new Dialog<>();
        d.setTitle("Move sample");
        ComboBox<Container> contCombo = new ComboBox<>();
        contCombo.getItems().addAll(containerService.getAll());
        contCombo.setConverter(new javafx.util.StringConverter<Container>() {
            @Override
            public String toString(Container c) {
                return c == null ? "" : "#" + c.getId() + " " + c.getName();
            }

            @Override
            public Container fromString(String s) {
                return null;
            }
        });
        if (!contCombo.getItems().isEmpty()) contCombo.getSelectionModel().selectFirst();
        ComboBox<String> slotCombo = new ComboBox<>();
        slotCombo.setDisable(true);
        contCombo.valueProperty().addListener((obs, old, nc) -> {
            if (nc != null) {
                List<String> free = slotService.getByContainer(nc.getId()).stream()
                        .filter(s -> !s.isOccupied()).map(Slot::getCode).collect(Collectors.toList());
                slotCombo.getItems().setAll(free);
                slotCombo.setDisable(free.isEmpty());
                if (!free.isEmpty()) slotCombo.getSelectionModel().selectFirst();
            } else {
                slotCombo.getItems().clear();
                slotCombo.setDisable(true);
            }
        });
        GridPane grid = new GridPane();
        grid.add(new Label("New container:"), 0, 0);
        grid.add(contCombo, 1, 0);
        grid.add(new Label("New slot:"), 0, 1);
        grid.add(slotCombo, 1, 1);
        d.getDialogPane().setContent(grid);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Container c = contCombo.getValue();
                String s = slotCombo.getValue();
                if (c != null && s != null) return new AbstractMap.SimpleEntry<>(c.getId(), s);
            }
            return null;
        });
        Optional<AbstractMap.SimpleEntry<Long, String>> res = d.showAndWait();
        res.ifPresent(pair -> {
            try {
                placementService.move(sampleId, pair.getKey(), pair.getValue(), currentUser);
                refreshPlacements();
                slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
                showInfo("Sample moved");
            } catch (IllegalArgumentException e) {
                showAlert(e.getMessage());
            }
        });
    }

    private void removeSelectedSample() {
        if (currentContainer == null || currentUser == null) return;
        if (!currentUser.equals(currentContainer.getOwnerUsername())) {
            showAlert("Not owner");
            return;
        }
        Placement selected = placementTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Select a placement to remove");
            return;
        }
        try {
            placementService.removeBySample(selected.getSampleId(), currentUser);
            refreshPlacements();
            slotTable.setItems(FXCollections.observableArrayList(slotService.getByContainer(currentContainer.getId())));
            showInfo("Sample removed");
        } catch (IllegalArgumentException e) {
            showAlert(e.getMessage());
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
}