package ru.itmo.marimiari.command;

import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

public class ExitCommand extends Command {
    public ExitCommand(){
        super("exit", "exit the program", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException{
        if (args.length != 0){
            throw new CommandException("Command exit does not accept arguments");
        }
    }

    @Override
    public void execute(Environment env, String[] args){
        System.out.println("Goodbye!");
        System.exit(0);
    }
}
