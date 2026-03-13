package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.Comparator;
import java.util.List;

public class SlotListCommand extends Command {
    public SlotListCommand() {
        super("slot_list", "list of container slots [--free-only]", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length < 1 || args.length > 2) {
            throw new CommandException("Usage: slot_list <container_id> [--free-only]");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("Incorrect container_id: " + args[0]);
        }
        if (args.length == 2 && !"--free-only".equals(args[1])) {
            throw new CommandException("Invalid option. Use --free-only");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        long containerId = Long.parseLong(args[0]);
        boolean freeOnly = args.length == 2 && "--free-only".equals(args[1]);

        if (!env.getContainerService().exists(containerId)) {
            throw new CommandException("Container not found");
        }

        List<Slot> slots = env.getSlotService().getByContainer(containerId);
        if (slots.isEmpty()) {
            System.out.println("There are no slots in the container");
            return;
        }

        slots.sort(Comparator.comparing(Slot::getCode));
        System.out.println("Slots in the container " + containerId + ":");
        for (Slot s : slots) {
            if (freeOnly && s.isOccupied()) continue;
            System.out.println("  " + s.getCode() + (s.isOccupied() ? " [OCCUPIED]" : " [FREE]"));
        }
    }
}

