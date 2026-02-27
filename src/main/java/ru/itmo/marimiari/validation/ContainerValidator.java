package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.exception.ValidationException;

public class ContainerValidator {
    public static void validate(Container container){
        if (container == null){ //проверка на существование в целом
            throw new ValidationException("Container cannot be null");
        }
        if (container.getName() == null || container.getName().isEmpty()) {
            throw new ValidationException("Container name cannot be empty");
        }
        if (container.getName().length() > 64) {
            throw new ValidationException("Container name too long (max 64)");
        }
        if (container.getType() == null) {
            throw new ValidationException("Container type cannot be null");
        }
        if (container.getOwnerUsername() == null || container.getOwnerUsername().isEmpty()) {
            throw new ValidationException("Owner cannot be empty");
        }
    }
}
