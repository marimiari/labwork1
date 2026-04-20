package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import ru.itmo.marimiari.storage.InstantAdapter;
import java.time.Instant;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public final class Container {
    private long id;
    private String name;
    private ContainerType type;
    private String ownerUsername;
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant createdAt;
    @XmlJavaTypeAdapter(InstantAdapter.class)
    private Instant updatedAt;

    public Container() {}

    public Container(long id, String name, ContainerType type, String ownerUsername) {
        this.id = id;
        this.setName(name);
        this.type = type;
        this.ownerUsername = ownerUsername;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.isEmpty() && name.length() <= 64) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Invalid name: " + name);
        }
    }

    public ContainerType getType() {
        return type;
    }

    public void setType(ContainerType type) {
        this.type = type;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "#" + id + " " + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, ownerUsername, createdAt, updatedAt);
    }
}
