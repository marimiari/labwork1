package ru.itmo.marimiari.domain;

import ru.itmo.marimiari.service.*;


public class AppData {
    public static void loadDemoData(SampleService sampleService, ContainerService containerService, SlotService slotService, PlacementService placementService) {
        sampleService.add();
        sampleService.add();
        sampleService.add();
        sampleService.add();
        sampleService.add();

        long cont1 = containerService.add("freezer-1", ContainerType.FREEZER, "masha").getId();
        long cont2 = containerService.add("fridge-2", ContainerType.FRIDGE, "vaso").getId();
        long cont3 = containerService.add("box-3", ContainerType.BOX, "dasha").getId();

        slotService.createSlots(cont1, 3, 4);
        slotService.createSlots(cont2, 2, 3);
        slotService.createSlots(cont3, 4, 6);

        placementService.add(1, cont1, "A1", "masha");
        placementService.add(2, cont2, "A2", "vaso");
        placementService.add(3, cont3, "A3", "dasha");
    }
}
