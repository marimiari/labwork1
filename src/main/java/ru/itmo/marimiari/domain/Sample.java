package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public final class Sample {
    private long id;
    private String ownerUsername;

    public Sample(){}

    public Sample(long id, String ownerUsername) {
        this.id = id;
        this.ownerUsername = ownerUsername;
    }

    public long getId() {
        return id;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
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
