package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.validation.PlacementValidator;
import java.util.*;

public class PlacementService {
    private final Map<Long, Placement> placements = new LinkedHashMap<>(); //сохраняет порядок добавления (ссылка не меняется из-за final)
    private long nextId = 1; //счетчик для генерации уникальных айди
    private final SampleService sampleService;
    private final ContainerService containerService;
    private final SlotService slotService;

    public PlacementService(SampleService sampleService, ContainerService containerService, SlotService slotService){ //конструктор
        this.sampleService = sampleService;
        this.containerService = containerService;
        this.slotService = slotService;
    }

    public Placement add(long sampleId, long containerId, String slotCode, String owner) { //создать и проверить
        if (!sampleService.exists(sampleId)) {
            throw new IllegalArgumentException("Sample not found");
        }
        if (!containerService.exists(containerId)) {
            throw new IllegalArgumentException("Container not found");
        }
        Slot slot = slotService.findByCode(containerId, slotCode).orElse(null);
        if (slot == null) {
            throw new IllegalArgumentException("Slot not found");
        }
        if (slot.isOccupied()) {
            throw new IllegalArgumentException("Slot already occupied");
        }
        if (findBySample(sampleId).isPresent()) {
            throw new IllegalArgumentException("Sample already placed");
        }

        Placement placement = new Placement(nextId++, sampleId, containerId, slot.getId(), owner); //конструктор
        PlacementValidator.validate(placement); //проверка полей
        placements.put(placement.getId(), placement); //помещаем
        slotService.setOccupied(slot.getId(), true);
        return placement;
    }

    public Optional<Placement> get(long id) { //достать по айди
        return Optional.ofNullable(placements.get(id));
    }

    public Collection<Placement> getAll() { //вернуть все размещения в виде неизменяемой коллекции
        return Collections.unmodifiableCollection(placements.values());
    }

    public void update(long id) {
        Placement placement = placements.get(id);
        if (placement == null){
            throw new IllegalArgumentException("Placement not found");
        }
        PlacementValidator.validate(placement); //проверяет вновь поля, они не менялись
    }

    public void remove(long id) { //удаление существующего по айди размещения
        Placement placement = placements.get(id);
        if (placement == null) {
            throw new IllegalArgumentException("Placement not found");
        }
        slotService.setOccupied(placement.getSlotId(), false);
        placements.remove(id);
    }

    public Placement move(long sampleId, long newContainerId, String newSlotCode, String owner){
        //переместить уже размещенный образец в другой контейнер/ячейку
        Placement old = findBySample(sampleId) //находим объект или кидает исключение
                .orElseThrow(() -> new IllegalArgumentException("Sample not placed"));
        slotService.setOccupied(old.getSlotId(), false);
        placements.remove(old.getId());
        return add(sampleId, newContainerId, newSlotCode, owner); //убирает и помещает другой
    }

    public void removeBySample(long sampleId) { //удалить по айди образца
        Placement placement = findBySample(sampleId).orElseThrow(() -> new IllegalArgumentException("Sample not placed"));
        remove(placement.getId());
    }

    public Optional<Placement> findBySample(long sampleId) { //найти по айди образца
        return placements.values().stream().filter(p -> p.getSampleId() == sampleId).findFirst();
    }

    public void clear() {
        placements.clear();
        nextId = 1;
    }

    public void addAll(Collection<Placement> collection) {
        for (Placement p : collection) {
            placements.put(p.getId(), p);
            if (p.getId() >= nextId) nextId = p.getId() + 1;
        }
    }

}
