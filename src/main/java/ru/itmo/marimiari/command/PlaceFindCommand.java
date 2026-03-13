package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.Placement;
import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.Optional;

public class PlaceFindCommand extends Command {
    public PlaceFindCommand() {
        super("place_find", "find where the sample is located: place_find <sample_id>", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("Usage: place_find <sample_id>");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("Incorrect sample_id");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        long sampleId = Long.parseLong(args[0]);
        Optional<Placement> opt = env.getPlacementService().findBySample(sampleId);
        if (opt.isPresent()) {
            Placement p = opt.get();
            Container c = env.getContainerService().get(p.getContainerId()).orElse(null);
            String containerName = (c != null) ? c.getName() : "unknown";
            Slot s = env.getSlotService().get(p.getSlotId()).orElse(null);
            String slotCode = (s != null) ? s.getCode() : "unknown";
            System.out.println("Sample " + sampleId + " placed at " + containerName + " / " + slotCode);
        } else {
            System.out.println("Sample " + sampleId + " not placed");
        }
    }
}

