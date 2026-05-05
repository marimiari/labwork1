package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.ContainerType;
import ru.itmo.marimiari.repository.ContainerRepository;

import java.time.Instant;
import java.util.*;

public class ContainerService {
    private final Map<Long, Container> cache = new LinkedHashMap<>();
    private final ContainerRepository repository;
    private long currentUserId;

    public ContainerService(ContainerRepository repository) {
        this.repository = repository;
        loadAll();
    }

    private void loadAll() {
        List<Container> list = repository.findALL();
        cache.clear();
        for (Container container : list) cache.put(container.getId(), container);
    }

    public void setCurrentUserId(long userId) {
        this.currentUserId = userId;
    }

    public Container add(String name, ContainerType type) {
        Container container = repository.insert(name, type, currentUserId);
        if (container != null) {
            cache.put(container.getId(), container);
        }
        return container;
    }

    public Optional<Container> get(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public Collection<Container> getAll() {
        return Collections.unmodifiableCollection(cache.values());
    }

    public boolean exists(long id) {
        return cache.containsKey(id);
    }

    public void update(long id, String name, ContainerType type) {
        if (!cache.containsKey(id)) throw new IllegalArgumentException("Container not found");
        boolean ok = repository.update(id, name, type, currentUserId);
        if (!ok) throw new IllegalArgumentException("You are not the owner");
        Container c = cache.get(id);
        c.setName(name);
        c.setType(type);
        c.setUpdatedAt(Instant.now());
    }

    public void remove(long id) {
        if (!cache.containsKey(id)) throw new IllegalArgumentException("Container not found");
        repository.delete(id, currentUserId);
        cache.remove(id);
    }

}
