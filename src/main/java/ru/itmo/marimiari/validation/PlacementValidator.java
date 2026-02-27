package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Placement;
import ru.itmo.marimiari.exception.ValidationException;

public class PlacementValidator {
    public static void validate(Placement placement) {
        if (placement == null) { //проверка на существование в целом
            throw new ValidationException("Placement cannot be null");
        }
        if (placement.getSampleId() <= 0) {
            throw new ValidationException("Sample id must be positive");
        }
        if (placement.getContainerId() <= 0) {
            throw new ValidationException("Container id must be positive");
        }
        if (placement.getSlotId() <= 0) {
            throw new ValidationException("Slot id must be positive");
        }
        if (placement.getOwnerUsername() == null || placement.getOwnerUsername().isEmpty()) {
            throw new ValidationException("Owner cannot be empty");
        }
    }
}
