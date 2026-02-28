package ru.itmo.marimiari;

import ru.itmo.marimiari.domain.ContainerType;
import ru.itmo.marimiari.service.ContainerService;

public class Main {
    public static void main(String[] args) {
        ContainerService containerService = new ContainerService();

        var container = containerService.add("holodilnik", ContainerType.FREEZER, "yasss");
        System.out.println("Container created: " + container.getName() + " with id: " + container.getId());
    }
}
