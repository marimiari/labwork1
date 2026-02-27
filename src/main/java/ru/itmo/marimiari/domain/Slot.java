package ru.itmo.marimiari.domain;

import java.time.Instant;

public final class Slot {
    private long id;
    private long containerId;
    private String code;
    private boolean occupied;
    private Instant createdAt;

    public Slot(long id, long containerId, String code, boolean occupied, Instant createdAt) {
        this.id = id;
        this.containerId = containerId;
        this.code = code;
        this.occupied = false;
        this.createdAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(long containerId) {
        this.containerId = containerId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}