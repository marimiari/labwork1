package ru.itmo.marimiari.storage;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import ru.itmo.marimiari.service.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;


public class XmlStorage {
    public static void save(Path path, SampleService sampleService, ContainerService containerService,
                            SlotService slotService, PlacementService placementService) throws StorageException {
        StorageData data = new StorageData();
        data.getSamples().addAll(sampleService.getAll());
        data.getContainers().addAll(containerService.getAll());
        data.getSlots().addAll(slotService.getAll());
        data.getPlacements().addAll(placementService.getAll());

        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to create directories: " + path.getParent(), e);
        }

        try {
            JAXBContext context = JAXBContext.newInstance(StorageData.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(data, path.toFile());
        } catch (JAXBException e) {
            throw new StorageException("Error saving XML: " + e.getMessage(), e);
        }
    }

    public static StorageData load(Path path) throws StorageException {
        if (!Files.exists(path)) {
            throw new StorageException("File not found: " + path);
        }
        try {
            JAXBContext context = JAXBContext.newInstance(StorageData.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (StorageData) unmarshaller.unmarshal(path.toFile());
        } catch (JAXBException e) {
            throw new StorageException("Error loading XML: " + e.getMessage(), e);
        }
    }
}
