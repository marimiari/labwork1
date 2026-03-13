package ru.itmo.marimiari.interpreter;

import java.util.Scanner;

public abstract class Command {
    private final String name;
    private final String help;
    private final boolean isReqAdditionalInput;

    public Command(String name, String help, boolean isReqAdditionalInput){
        this.name = name;
        this.help = help;
        this.isReqAdditionalInput = isReqAdditionalInput;
    }

    public String getName(){
        return name;
    }

    public final String getHelp(){
        return help;
    }

    public boolean isReqAdditionalInput(){
        return isReqAdditionalInput;
    }

    public abstract void execute(Environment env, String[] args) throws CommandException;

    public void checkArgs(String[] args) throws CommandException{}

    public void additionalInput(Environment environment, Scanner scanner) throws CommandException {}
}
