package ru.itmo.marimiari.storage;

import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.validation.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileValidator {
    public static void validate(StorageData data) throws StorageException{
        checkUniqueIds(data.getSamples(), "sample");
        checkUniqueIds(data.getContainers(), "container");
        checkUniqueIds(data.getSlots(), "slot");
        checkUniqueIds(data.getPlacements(), "placement");

        Set<Long> sampleIds = collectIds(data.getSamples());
        Set<Long> containerIds = collectIds(data.getContainers());
        Set<Long> slotIds = collectIds(data.getSlots());

        for (Placement p : data.getPlacements()){
            if (!sampleIds.contains(p.getSampleId()))
                throw new StorageException("Placement refers to an unknown Sample id=" + p.getSampleId());
            if (!containerIds.contains(p.getContainerId()))
                throw new StorageException("Placement refers to an unknown Container id=" + p.getContainerId());
            if (!slotIds.contains(p.getSlotId()))
                throw new StorageException("Placement refers to an unknown Slot id=" + p.getSlotId());
        }

        for (Slot s : data.getSlots()) {
            if (!containerIds.contains(s.getContainerId()))
                throw new StorageException("Slot refers to an unknown Container id=" + s.getContainerId());
        }

        for (Sample s : data.getSamples()) SampleValidator.validate(s);
        for (Container c : data.getContainers()) ContainerValidator.validate(c);
        for (Slot slot : data.getSlots()) SlotValidator.validate(slot);
        for (Placement p : data.getPlacements()) PlacementValidator.validate(p);
    }

    private static <T> void checkUniqueIds(List<T> list, String entityName) throws StorageException {
        Set<Long> ids = new HashSet<>();
        for (T obj : list) {
            long id = getId(obj);
            if (!ids.add(id)) {
                throw new StorageException("Duplicate id=" + id + " in the collection " + entityName);
            }
        }
    }

    private static long getId(Object object){
        if (object instanceof Sample) return ((Sample) object).getId();
        if (object instanceof Container) return ((Container) object).getId();
        if (object instanceof Slot) return ((Slot) object).getId();
        if (object instanceof Placement) return ((Placement) object).getId();
        throw new IllegalArgumentException("Unknown type");
    }

    private static Set<Long> collectIds(List<?> list){
        Set<Long> ids = new HashSet<>();
        for (Object obj : list) {
            ids.add(getId(obj));
        }
        return ids;
    }
}
