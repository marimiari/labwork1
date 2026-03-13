package ru.itmo.marimiari.interpreter;

import ru.itmo.marimiari.service.*;

public class Environment {
    private final SampleService sampleService;
    private final ContainerService containerService;
    private final SlotService slotService;
    private final PlacementService placementService;

    public Environment(SampleService sampleService, ContainerService containerService, SlotService slotService, PlacementService placementService) {
        this.sampleService = sampleService;
        this.containerService = containerService;
        this.slotService = slotService;
        this.placementService = placementService;
    }

    public SampleService getSampleService() {
        return sampleService;
    }

    public ContainerService getContainerService() {
        return containerService;
    }

    public SlotService getSlotService() {
        return slotService;
    }

    public PlacementService getPlacementService() {
        return placementService;
    }
}
