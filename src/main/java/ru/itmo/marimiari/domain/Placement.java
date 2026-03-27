package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.itmo.marimiari.storage.InstantAdapter;
import java.time.Instant;

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
}
