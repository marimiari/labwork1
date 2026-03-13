package ru.itmo.marimiari.service;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.ContainerType;
import ru.itmo.marimiari.validation.ContainerValidator;
import java.time.Instant;
import java.util.*;

public class ContainerService {
    private final Map<Long, Container> containers = new LinkedHashMap<>(); //сохраняет порядок добавления (ссылка не меняется из-за final)
    private long nextId = 1; //счетчик для генерации уникальных айди

    public Container add(String name, ContainerType type, String owner) { //создаёт новый контейнер и добавляет его в хранилище
        Container container = new Container(nextId++, name, type, owner);
        ContainerValidator.validate(container); //проверка корректности полей
        containers.put(container.getId(), container); //сохраняет в мап
        return container;
    }

    public Optional<Container> get(long id) { //получить контейнер по айди
        return Optional.ofNullable(containers.get(id));
    }

    public Collection<Container> getAll() { //возврат всей коллекции значений
        return Collections.unmodifiableCollection(containers.values()); //тут любая попытка изменить-исключение
    }

    public boolean exists(long id) { //проверяет существование контейнера с таким айди
        return containers.containsKey(id);
    }

    public void update(long id, String name, ContainerType type) { //обновление полей
        Container container = containers.get(id);
        if (container == null)
            throw new IllegalArgumentException("Container not found");
        if (name != null) container.setName(name);
        if (type != null) container.setType(type);
        container.setUpdatedAt(Instant.now()); //обновляется время последнего изменения
        ContainerValidator.validate(container); //повторная валидация
    }

    public void remove(long id) { //удалить по айди
        if (!exists(id)) {
            throw new IllegalArgumentException("Container not found");
        }
        containers.remove(id);
    }
}
