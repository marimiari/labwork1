package ru.itmo.marimiari.domain;

import ru.itmo.marimiari.service.*;


public class AppData {
    public static void loadDemoData(SampleService sampleService, ContainerService containerService, SlotService slotService, PlacementService placementService) {
        for (int i = 0; i < 5; i++) {
            sampleService.add("SYSTEM");
        }

        long cont1 = containerService.add("freezer-1", ContainerType.FREEZER, "SYSTEM").getId();
        long cont2 = containerService.add("fridge-2", ContainerType.FRIDGE, "SYSTEM").getId();
        long cont3 = containerService.add("box-3", ContainerType.BOX, "SYSTEM").getId();

        slotService.createSlots(cont1, 3, 4,"SYSTEM" );
        slotService.createSlots(cont2, 2, 3,"SYSTEM");
        slotService.createSlots(cont3, 4, 6,"SYSTEM");

        placementService.add(1, cont1, "A1", "SYSTEM");
        placementService.add(2, cont2, "A2", "SYSTEM");
        placementService.add(3, cont3, "A3", "SYSTEM");
    }
}
