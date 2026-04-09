package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.itmo.marimiari.storage.InstantAdapter;
import java.time.Instant;

@XmlAccessorType(XmlAccessType.FIELD)
public final class Slot {
    private long id;
    private long containerId;
    private String code;
    private boolean occupied;
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant createdAt;

    public Slot(){}

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

    @Override
    public String toString(){
        return code;
    }
}
