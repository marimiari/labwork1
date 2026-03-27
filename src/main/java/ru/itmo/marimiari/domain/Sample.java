package ru.itmo.marimiari.domain;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.FIELD)
public final class Sample {
    private long id;

    public Sample(){}

    public Sample(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
