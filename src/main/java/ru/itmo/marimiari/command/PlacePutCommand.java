package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Placement;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

public class PlacePutCommand extends Command {
    public PlacePutCommand() {
        super("place_put", "place the sample in the container: place_put <sample_id> <container_id> <slot_code>", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 3) {
            throw new CommandException("Usage: place_put <sample_id> <container_id> <slot_code>");
        }
        try {
            Long.parseLong(args[0]);
            Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new CommandException("Incorrect id");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        long sampleId = Long.parseLong(args[0]);
        long containerId = Long.parseLong(args[1]);
        String slotCode = args[2];
        Placement placement = env.getPlacementService().add(sampleId, containerId, slotCode, "me");
        System.out.println("OK sample " + sampleId + " placed in " + slotCode + " placement_id=" + placement.getId());
    }
}