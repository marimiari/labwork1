package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.*;
import ru.itmo.marimiari.exception.ValidationException;

public class ContainerValidator {
    public static void validate(Container container) {
        checkNotNull(container);
        checkName(container.getName());
        checkType(container.getType());
        checkOwner(container.getOwnerLogin());
    }

    private static void checkNotNull(Container container) {
        if (container == null) {
            throw new ValidationException("Container cannot be null");
        }
    }

    private static void checkName(String name) {
        if (name == null || name.isEmpty()) {
            throw new ValidationException("Container name cannot be empty");
        }
        if (name.length() > 64) {
            throw new ValidationException("Container name too long (max 64)");
        }
    }

    private static void checkType(ContainerType type) {
        if (type == null) {
            throw new ValidationException("Container type cannot be null");
        }
    }

    private static void checkOwner(String owner) {
        if (owner == null || owner.isEmpty()) {
            throw new ValidationException("Owner cannot be empty");
        }
    }
}

