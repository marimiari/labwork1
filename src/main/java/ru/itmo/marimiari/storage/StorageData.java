package ru.itmo.marimiari.storage;

import jakarta.xml.bind.annotation.*;
import ru.itmo.marimiari.domain.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "storage")
@XmlAccessorType(XmlAccessType.FIELD) //
public class StorageData {

    @XmlElementWrapper(name = "samples")
    @XmlElement(name = "sample")
    private List<Sample> samples = new ArrayList<>();

    @XmlElementWrapper(name = "containers")
    @XmlElement(name = "container")
    private List<Container> containers = new ArrayList<>();

    @XmlElementWrapper(name = "slots")
    @XmlElement(name = "slot")
    private List<Slot> slots = new ArrayList<>();

    @XmlElementWrapper(name = "placements")
    @XmlElement(name = "placement")
    private List<Placement> placements = new ArrayList<>();

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainers(List<Container> containers) {
        this.containers = containers;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    public List<Placement> getPlacements() {
        return placements;
    }

    public void setPlacements(List<Placement> placements) {
        this.placements = placements;
    }
}