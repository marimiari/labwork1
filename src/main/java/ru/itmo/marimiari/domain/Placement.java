package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.itmo.marimiari.storage.InstantAdapter;
import java.time.Instant;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public final class Placement {
    private long id;
    private long sampleId;
    private long containerId;
    private long slotId;
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant placedAt;
    private String ownerUsername;

    public Placement() {}

    public Placement(long id, long sampleId, long containerId, long slotId, String ownerUsername) {
        this.id = id;
        this.sampleId = sampleId;
        this.containerId = containerId;
        this.slotId = slotId;
        this.ownerUsername = ownerUsername;
        this.placedAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public long getSampleId() {
        return sampleId;
    }

    public long getContainerId() {
        return containerId;
    }

    public long getSlotId() {
        return slotId;
    }

    public Instant getPlacedAt() {
        return placedAt;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    @Override
    public String toString(){
        return "Sample " + sampleId;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Placement placement = (Placement) object;
        return id == placement.id && sampleId == placement.sampleId && containerId == placement.containerId && slotId == placement.slotId && Objects.equals(placedAt, placement.placedAt) && Objects.equals(ownerUsername, placement.ownerUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sampleId, containerId, slotId, placedAt, ownerUsername);
    }
}
