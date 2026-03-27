package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Sample;
import ru.itmo.marimiari.validation.SampleValidator;
import java.util.*;

public class SampleService {
    private final Map<Long, Sample> samples = new LinkedHashMap<>(); //сохраняет порядок добавления (ссылка не меняется из-за final)
    private long nextId = 1; //счетчик для генерации уникальных айди

    public Sample add() { //создаем и добавляем в хранилище
        Sample sample = new Sample(nextId++);
        SampleValidator.validate(sample); //проверка полей
        samples.put(sample.getId(), sample); //кладем в мап
        return sample;
    }

    public Optional<Sample> get(long id) { //получить по айди
        return Optional.ofNullable(samples.get(id));
    }

    public Collection<Sample> getAll() { //вернуть все образцы в виде неизменной коллекции
        return Collections.unmodifiableCollection(samples.values());
    }

    public boolean exists(long id) { //проверить есть ли объект с указанным айди
        return samples.containsKey(id);
    }

    public void remove(long id) { //удалить по айди(если есть объект)
        if (!exists(id)){
            throw new IllegalArgumentException("Sample not found");
        }
        samples.remove(id);
    }

    public void clear() {
        samples.clear();
        nextId = 1;
    }

    public void addAll(Collection<Sample> collection) {
        for (Sample s : collection) {
            samples.put(s.getId(), s);
            if (s.getId() >= nextId) {
                nextId = s.getId() + 1;
            }
        }
    }
}
