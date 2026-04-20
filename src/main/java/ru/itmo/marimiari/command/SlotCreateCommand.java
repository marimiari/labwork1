package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;
import ru.itmo.marimiari.storage.StorageException;
import ru.itmo.marimiari.storage.XmlStorage;

import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class SlotCreateCommand extends Command {
    private long containerId;
    private int rows;
    private int cols;

    public SlotCreateCommand() {
        super("slot_create", "create a grid of slots for the container", true);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("Use: slot_create <container_id>");
        }
    }

    @Override
    public void additionalInput(Environment env, Scanner scanner) throws CommandException {
        while (true) {
            System.out.print("Container id: ");
            String input = scanner.nextLine().trim();
            try {
                long id = Long.parseLong(input);
                if (env.getContainerService().exists(id)) {
                    containerId = id;
                    break;
                } else {
                    System.out.println("Container not found. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid container id. Try again.");
            }
        }

        while (true) {
            System.out.print("Rows (A.., max 26): ");
            String input = scanner.nextLine().trim();
            try {
                rows = Integer.parseInt(input);
                if (rows > 0 && rows <= 26) {
                    break;
                } else {
                    System.out.println("The number of rows must be from 1 to 26. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }

        while (true) {
            System.out.print("Columns (1..): ");
            String input = scanner.nextLine().trim();
            try {
                cols = Integer.parseInt(input);
                if (cols > 0) {
                    break;
                } else {
                    System.out.println("The number of columns must be positive. Try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        List<Slot> created = env.getSlotService().createSlots(containerId, rows, cols, "SYSTEM");
        System.out.println("OK created " + created.size() + " slots");
        try {
            XmlStorage.save(Paths.get("data.xml"),
                    env.getSampleService(),
                    env.getContainerService(),
                    env.getSlotService(),
                    env.getPlacementService());
        } catch (StorageException e) {
            System.out.println("Warning: auto-save failed - " + e.getMessage());
        }
    }
}

