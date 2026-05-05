package ru.itmo.marimiari.domain;

import java.util.Objects;

public final class Sample {
    private long id;
    private long ownerId;

    public Sample() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Sample sample = (Sample) object;
        return id == sample.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
