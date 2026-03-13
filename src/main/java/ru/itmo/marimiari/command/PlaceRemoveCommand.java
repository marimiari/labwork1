package ru.itmo.marimiari.command;

import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

public class PlaceRemoveCommand extends Command {
    public PlaceRemoveCommand() {
        super("place_remove", "remove sample placement: place_remove <sample_id>", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("Usage: place_remove <sample_id>");
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
        env.getPlacementService().removeBySample(sampleId);
        System.out.println("OK sample " + sampleId + " removed from the storage");
    }
}

