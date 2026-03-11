package ru.itmo.marimiari.validation;

import ru.itmo.marimiari.domain.Sample;
import ru.itmo.marimiari.exception.ValidationException;

public class SampleValidator {
    public static void validate(Sample sample) {
        checkNotNull(sample);
        checkId(sample.getId());
    }

    private static void checkNotNull(Sample sample) {
        if (sample == null) {
            throw new ValidationException("Sample cannot be null");
        }
    }

    private static void checkId(long id) {
        if (id <= 0) {
            throw new ValidationException("Sample id must be positive");
        }
    }
}
