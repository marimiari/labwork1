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
        while (true) {
            System.out.print("Name: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                name = input;
                break;
            }
            System.out.println("Name cannot be empty. Try again.");
        }

        while (true) {
            System.out.print("Type (FREEZER/FRIDGE/BOX): ");
            String typeStr = scanner.nextLine().trim().toUpperCase();
            try {
                type = ContainerType.valueOf(typeStr);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid type. Must be FREEZER, FRIDGE or BOX. Try again.");
            }
        }

        while (true) {
            System.out.print("Owner: ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                owner = input;
                break;
            }
            System.out.println("Owner cannot be empty. Try again.");
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        Container container = env.getContainerService().add(name, type, owner);
        System.out.println("OK container_id=" + container.getId());
    }
}
