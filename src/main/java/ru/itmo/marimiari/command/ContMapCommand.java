package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.*;

public class ContMapCommand extends Command {
    public ContMapCommand() {
        super("cont_map", "show a container occupancy map: cont_map <container_id>", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("Usage: cont_map <container_id>");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("Некорректный container_id");
        }
    }

    @Override
    public void execute(Environment environment, String[] args) throws CommandException {
        long containerId = Long.parseLong(args[0]);
        if (!environment.getContainerService().exists(containerId)) {
            throw new CommandException("Container not found");
        }
        List<Slot> slots = environment.getSlotService().getByContainer(containerId);
        if (slots.isEmpty()) {
            System.out.println("There are no slots in the container.");
            return;
        }
        Map<Character, List<Slot>> rows = new TreeMap<>();
        for (Slot s : slots) {
            char row = s.getCode().charAt(0);
            rows.computeIfAbsent(row, k -> new ArrayList<>()).add(s);
        }
        for (List<Slot> rowSlots : rows.values()) {
            rowSlots.sort(Comparator.comparing(Slot::getCode));
        }
        System.out.println("Container occupancy map " + containerId + ":");
        for (Map.Entry<Character, List<Slot>> entry : rows.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (Slot s : entry.getValue()) {
                System.out.print(s.isOccupied() ? "[X]" : "[ ]");
            }
            System.out.println();
        }
    }
}

