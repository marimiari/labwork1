package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

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
        try {
            containerId = Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid container_id: " + args[0]);
        }
    }

    @Override
    public void additionalInput(Environment env, Scanner scanner) throws CommandException {
        if (!env.getContainerService().exists(containerId)) {
            throw new CommandException("Container not found");
        }

        System.out.print("Rows (A..): ");
        try {
            rows = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new CommandException("Incorrect number of rows");
        }
        if (rows <= 0 || rows > 26) {
            throw new CommandException("The number of rows must be from 1 to 26");
        }

        System.out.print("Columns (1..): ");
        try {
            cols = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new CommandException("Incorrect number of columns");
        }
        if (cols <= 0) {
            throw new CommandException("The number of columns must be positive");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        List<Slot> created = env.getSlotService().createSlots(containerId, rows, cols);
        System.out.println("OK created " + created.size() + " slots");
    }
}

