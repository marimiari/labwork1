package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.ContainerType;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.Scanner;

public class ContAddCommand extends Command {
    private String name;
    private ContainerType type;
    private String owner;

    public ContAddCommand() {
        super("cont_add", "create new container", true);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("cont_add does not accept arguments");
        }
    }

    @Override
    public void additionalInput(Environment env, Scanner scanner) throws CommandException {
        System.out.print("Name: ");
        name = scanner.nextLine().trim();
        if (name.isEmpty()) throw new CommandException("Name cannot be empty");

        System.out.print("Type (FREEZER/FRIDGE/BOX): ");
        String typeStr = scanner.nextLine().trim().toUpperCase();
        try {
            type = ContainerType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            throw new CommandException("Invalid type. Must be FREEZER, FRIDGE or BOX");
        }
        System.out.print("Owner: ");
        owner = scanner.nextLine().trim();
        if (owner.isEmpty()) throw new CommandException("Owner cannot be empty");
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        Container container = env.getContainerService().add(name, type, owner);
        System.out.println("OK container_id=" + container.getId());
    }
}
