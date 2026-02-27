package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Sample;
import ru.itmo.marimiari.exception.ValidationException;

public class SampleValidator {
    public static void validate(Sample sample) {
        if (sample == null){
            throw new ValidationException("Sample cannot be null");
        }
        if (sample.getId() <= 0){
            throw new ValidationException("Sample id must be positive");
        }
    }
}
