package ru.itmo.marimiari.command;

import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;
import ru.itmo.marimiari.storage.FileStorage;
import ru.itmo.marimiari.storage.FileValidator;
import ru.itmo.marimiari.storage.StorageData;
import ru.itmo.marimiari.storage.StorageException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadCommand extends Command {
    public LoadCommand(){
        super("load","load data from XML file: load <path>",false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException{
        if (args.length != 1){
            throw new CommandException("Usage: load <path>");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException{
        Path path = Paths.get(args[0]);
        try {
            StorageData data = FileStorage.load(path);
            FileValidator.validate(data);

            env.getSampleService().clear();
            env.getSampleService().addAll(data.getSamples());
            env.getContainerService().clear();
            env.getContainerService().addAll(data.getContainers());
            env.getSlotService().clear();
            env.getSlotService().addAll(data.getSlots());
            env.getPlacementService().clear();
            env.getPlacementService().addAll(data.getPlacements());
            System.out.println("Data loaded from " + path.toAbsolutePath());
        } catch (StorageException e) {
            throw new CommandException("Loading error " + e.getMessage());
        }
    }
}
