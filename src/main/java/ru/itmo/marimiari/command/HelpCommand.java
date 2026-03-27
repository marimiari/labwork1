package ru.itmo.marimiari.command;

import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.CommandInterpreter;
import ru.itmo.marimiari.interpreter.Environment;

public class HelpCommand extends Command {
    private final CommandInterpreter interpreter;

    public HelpCommand(CommandInterpreter interpreter) {
        super("help", "show command list", false);
        this.interpreter = interpreter;
    }

    @Override
    public void checkArgs(String[] args) throws CommandException{
        if (args.length != 0){
            throw new CommandException("Command help does not accept arguments");
        }
    }

    @Override
    public void execute(Environment env, String[] args){
        System.out.println("Available commands:");
        for (Command cmd : interpreter.getCommands().values()){
            System.out.printf("  %-15s - %s%n", cmd.getName(), cmd.getHelp());
        }
    }
}
