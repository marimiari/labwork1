package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.validation.SlotValidator;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SlotService {
    private final Map<Long, Slot> slots = new LinkedHashMap<>(); //сохраняет порядок добавления (ссылка не меняется из-за final)
    private long nextId = 1;
    private final ContainerService containerService;

    public SlotService(ContainerService containerService) {
        this.containerService = containerService;
    }

    public Slot add(long containerId, String code, String owner) { //добавляем слот с валидацией
        if (!containerService.exists(containerId)) {
            throw new IllegalArgumentException("Container not found");
        }
        if (findByCode(containerId, code).isPresent()) { //проверяем нет ли уже слота с таким кодом
            throw new IllegalArgumentException("Slot with code " + code + " already exists in this container");
        }
        Slot slot = new Slot(nextId++, containerId, code, false, Instant.now(), owner);
        SlotValidator.validate(slot);
        slots.put(slot.getId(), slot);
        return slot;
    }

    public Optional<Slot> get(long id) { //получение слота по айди
        return Optional.ofNullable(slots.get(id));
    }

    public Collection<Slot> getAll() { //получение всех слотов
        return Collections.unmodifiableCollection(slots.values());
    }

    public void update(long id, String newCode) { //обновление с валидацией
        Slot slot = slots.get(id);
        if (slot == null) {
            throw new IllegalArgumentException("Slot not found");
        }
        Optional<Slot> existing = findByCode(slot.getContainerId(), newCode); //проверяем айди
        if (existing.isPresent() && existing.get().getId() != id) {
            throw new IllegalArgumentException("Slot with code " + newCode + " already exists");
        }
        slot.setCode(newCode);
        SlotValidator.validate(slot);
    }

    public void remove(long id) { //удаляем слот (только существующий и свободный)
        Slot slot = slots.get(id);
        if (slot == null) {
            throw new IllegalArgumentException("Slot not found");
        }
        if (slot.isOccupied()) {
            throw new IllegalArgumentException("Cannot remove occupied slot");
        }
        slots.remove(id);
    }

    public List<Slot> createSlots(long containerId, int rows, int cols, String owner) { //создание сетки ячеек для указанного контейнера
        if (!containerService.exists(containerId))
            throw new IllegalArgumentException("Container not found");

        List<Slot> created = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String code = "" + (char)('A' + r) + c;
                Slot slot = new Slot(nextId++, containerId, code, false, Instant.now(), owner);
                SlotValidator.validate(slot);
                slots.put(slot.getId(), slot);
                created.add(slot);
            }
        }
        return created;
    }

    public List<Slot> getByContainer(long containerId) { //получить все слоты контейнера
        return slots.values().stream()
                .filter(s -> s.getContainerId() == containerId).collect(Collectors.toList());
    }

    public Optional<Slot> findByCode(long containerId, String code) { //найти слот по коду
        return slots.values().stream()
                .filter(s -> s.getContainerId() == containerId && s.getCode().equalsIgnoreCase(code)).findFirst();
    }

    public void setOccupied(long slotId, boolean occupied) {
        Slot slot = slots.get(slotId);
        if (slot == null){
            throw new IllegalArgumentException("Slot not found");
        }
        slot.setOccupied(occupied); //изменить занятость
    }

    public boolean isOccupied(long slotId) { //проверка на занятость слота
        Slot slot = slots.get(slotId);
        return slot != null && slot.isOccupied();
    }

    public void clear() {
        slots.clear();
        nextId = 1;
    }

    public void addAll(Collection<Slot> collection) {
        for (Slot s : collection) {
            slots.put(s.getId(), s);
            if (s.getId() >= nextId) nextId = s.getId() + 1;
        }
    }
}
