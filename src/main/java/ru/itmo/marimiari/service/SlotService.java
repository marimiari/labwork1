package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.repository.SlotRepository;

import java.time.Instant;
import java.util.*;

public class SlotService {
    private final Map<Long, Slot> cache = new LinkedHashMap<>();
    private final SlotRepository repository;
    private final ContainerService containerService;
    private long currentUserId;

    public SlotService(SlotRepository repository, ContainerService containerService) {
        this.repository = repository;
        this.containerService = containerService;
        loadAll();
    }

    private void loadAll() {
        List<Slot> slots = repository.findAll();
        cache.clear();
        for (Slot slot : slots) cache.put(slot.getId(), slot);
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public List<Slot> createSlots(long containerId, int rows, int cols) {
        if (!containerService.exists(containerId)) throw new IllegalArgumentException("Container not found");
        List<Slot> created = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 1; c <= cols; c++) {
                String code = "" + (char) ('A' + r) + c;
                Slot slot = repository.insert(containerId, code, false, currentUserId);
                if (slot != null) {
                    cache.put(slot.getId(), slot);
                    created.add(slot);
                }
            }
        }
        return created;
    }

    public Optional<Slot> get(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public List<Slot> getByContainer(long containerId) {
        List<Slot> result = new ArrayList<>();
        for (Slot slot : cache.values()) {
            if (slot.getContainerId() == containerId) result.add(slot);
        }
        result.sort(Comparator.comparing(Slot::getCode));
        return result;
    }

    public Optional<Slot> findByCode(long containerId, String code) {
        for (Slot slot : cache.values()) {
            if (slot.getContainerId() == containerId && slot.getCode().equalsIgnoreCase(code)) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    public void setOccupied(long slotId, boolean occupied) {
        Slot slot = cache.get(slotId);
        if (slot == null) throw new IllegalArgumentException("Slot not found");
        boolean ok = repository.updateOccupied(slotId, occupied, currentUserId);
        if (!ok) throw new IllegalArgumentException("You are not the owner");
        slot.setOccupied(occupied);
    }

    public boolean isOccupied(long slotId) {
        Slot slot = cache.get(slotId);
        return slot != null && slot.isOccupied();
    }
}
