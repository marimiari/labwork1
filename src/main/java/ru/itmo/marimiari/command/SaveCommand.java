package ru.itmo.marimiari.command;

import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;
import ru.itmo.marimiari.storage.StorageException;
import ru.itmo.marimiari.storage.FileStorage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SaveCommand extends Command{
    public SaveCommand(){
        super("save", "save data to XML file: save <path>", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException{
        if (args.length != 1){
            throw new CommandException("Usage: save <path>");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException{
        Path path = Paths.get(args[0]);
        try{
            FileStorage.save(path, env.getSampleService(), env.getContainerService(), env.getSlotService(), env.getPlacementService());
            System.out.println("Data saved in " + path.toAbsolutePath());
        }
        catch (StorageException e){
            throw new CommandException("Saving error" + e.getMessage());
        }
    }
}

