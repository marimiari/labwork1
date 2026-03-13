package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.exception.ValidationException;

public class SlotValidator {
    public static void validate(Slot slot) {
        checkNotNull(slot);
        checkContainerId(slot.getContainerId());
        checkCode(slot.getCode());
    }

    private static void checkNotNull(Slot slot) {
        if (slot == null) {
            throw new ValidationException("Slot cannot be null");
        }
    }

    private static void checkContainerId(long containerId) {
        if (containerId <= 0) {
            throw new ValidationException("Container Id must be positive");
        }
    }

    private static void checkCode(String code) {
        if (code == null || code.isEmpty()) {
            throw new ValidationException("Slot code cannot be empty");
        }
        if (code.length() > 8) {
            throw new ValidationException("Slot code too long (max 8)");
        }
    }
}
