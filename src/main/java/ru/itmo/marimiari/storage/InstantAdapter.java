package ru.itmo.marimiari.storage;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import java.time.Instant;

public class InstantAdapter extends XmlAdapter<String, Instant> {
    @Override
    public Instant unmarshal(String v) {
        return v == null ? null : Instant.parse(v);
    }

    @Override
    public String marshal(Instant v) {
        return v == null ? null : v.toString();
    }
}
