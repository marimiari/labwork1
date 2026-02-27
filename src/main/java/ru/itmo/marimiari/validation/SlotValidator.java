package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.exception.ValidationException;

public class SlotValidator {
    public static void validate(Slot slot) {
        if (slot == null) {
            throw new ValidationException("Slot cannot be null");
        }
        if (slot.getContainerId() <= 0) {
            throw new ValidationException("Container Id must be positive");
        }
        if (slot.getCode() == null || slot.getCode().isEmpty()) {
            throw new ValidationException("Slot code cannot be empty");
        }
        if (slot.getCode().length() > 8) {
            throw new ValidationException("Slot code too long (max 8)");
        }
    }
}
