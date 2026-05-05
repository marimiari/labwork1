package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.repository.PlacementRepository;
import java.util.*;

public class PlacementService {
    private final Map<Long, Placement> cache = new LinkedHashMap<>();
    private final PlacementRepository repository;
    private final SampleService sampleService;
    private final ContainerService containerService;
    private final SlotService slotService;
    private long currentUserId;

    public PlacementService(PlacementRepository repository, SampleService sampleService,
                            ContainerService containerService, SlotService slotService) {
        this.repository = repository;
        this.sampleService = sampleService;
        this.containerService = containerService;
        this.slotService = slotService;
        loadAll();
    }

    private void loadAll() {
        List<Placement> list = repository.findAll();
        cache.clear();
        for (Placement placement : list) cache.put(placement.getId(), placement);
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public Placement add(long sampleId, long containerId, String slotCode) {
        if (!sampleService.exists(sampleId))
            throw new IllegalArgumentException("Sample not found");
        if (!containerService.exists(containerId))
            throw new IllegalArgumentException("Container not found");
        Slot slot = slotService.findByCode(containerId, slotCode)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        if (slot.isOccupied())
            throw new IllegalArgumentException("Slot already occupied");
        if (findBySample(sampleId).isPresent())
            throw new IllegalArgumentException("Sample already placed");

        Placement placement = repository.insert(sampleId, containerId, slot.getId(), currentUserId);
        if (placement != null) {
            cache.put(placement.getId(), placement);
            slotService.setOccupied(slot.getId(), true);
        }
        return placement;
    }

    public Placement move(long sampleId, long newContainerId, String newSlotCode) {
        Placement old = findBySample(sampleId).orElseThrow(() -> new IllegalArgumentException("Sample not placed"));
        slotService.setOccupied(old.getSlotId(), false);
        cache.remove(old.getId());
        return add(sampleId, newContainerId, newSlotCode);
    }

    public void removeBySample(long sampleId) {
        Placement placement = findBySample(sampleId).orElseThrow(() -> new IllegalArgumentException("Sample not placed"));
        boolean ok = repository.deleteBySampleId(sampleId, currentUserId);
        if (!ok)
            throw new IllegalArgumentException("Ypu are not the owner");
        slotService.setOccupied(placement.getSlotId(), false);
        cache.remove(placement.getId());
    }

    public Optional<Placement> findBySample(long sampleId) {
        for (Placement placement : cache.values()) {
            if (placement.getSampleId() == sampleId)
                return Optional.of(placement);
        }
        return Optional.empty();
    }

    public Collection<Placement> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }
}
