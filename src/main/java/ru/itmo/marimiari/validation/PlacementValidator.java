package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Placement;
import ru.itmo.marimiari.exception.ValidationException;

public class PlacementValidator {
    public static void validate(Placement placement) {
        checkNotNull(placement);
        checkSampleId(placement.getSampleId());
        checkContainerId(placement.getContainerId());
        checkSlotId(placement.getSlotId());
        checkOwner(placement.getOwnerUsername());
    }

    private static void checkNotNull(Placement placement) {
        if (placement == null) {
            throw new ValidationException("Placement cannot be null");
        }
    }

    private static void checkSampleId(long sampleId) {
        if (sampleId <= 0) {
            throw new ValidationException("Sample id must be positive");
        }
    }

    private static void checkContainerId(long containerId) {
        if (containerId <= 0) {
            throw new ValidationException("Container id must be positive");
        }
    }

    private static void checkSlotId(long slotId) {
        if (slotId <= 0) {
            throw new ValidationException("Slot id must be positive");
        }
    }

    private static void checkOwner(String owner) {
        if (owner == null || owner.isEmpty()) {
            throw new ValidationException("Owner cannot be empty");
        }
    }
}
