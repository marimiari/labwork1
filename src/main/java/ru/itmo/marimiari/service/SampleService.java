package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Sample;
import ru.itmo.marimiari.repository.SampleRepository;

import java.util.*;

public class SampleService {
    private final Map<Long, Sample> cache = new LinkedHashMap<>();
    private final SampleRepository repository;
    private long currentUserId;

    public SampleService(SampleRepository repository) {
        this.repository = repository;
        loadAll();
    }

    public void loadAll() {
        List<Sample> list = repository.findAll();
        cache.clear();
        for (Sample sample : list) cache.put(sample.getId(), sample);
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public Sample add() {
        Sample sample = repository.insert(currentUserId);
        if (sample != null) cache.put(sample.getId(), sample);
        return sample;
    }

    public Optional<Sample> get(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public Collection<Sample> getAll() {
        return cache.values();
    }

    public boolean exists(long id) {
        return cache.containsKey(id);
    }

    public void remove(long id) {
        if (!cache.containsKey(id)) throw new IllegalArgumentException("Sample not found");
        repository.delete(id);
        cache.remove(id);
    }

    public Map<Long, Sample> getCache(){
        return cache;
    }
}
