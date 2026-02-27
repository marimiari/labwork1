package ru.itmo.marimiari.domain;

import java.time.Instant;

public final class Placement {
    private final long id;
    private final long sampleId;
    private final long containerId;
    private final long slotId;
    private final Instant placedAt;
    private final String ownerUsername;

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
