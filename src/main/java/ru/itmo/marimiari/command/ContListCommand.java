package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.Collection;

public class ContListCommand extends Command {
    public ContListCommand() {
        super("cont_list", "container list", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("cont_list does not accept arguments");
        }
    }

    @Override
    public void execute(Environment env, String[] args) {
        Collection<Container> containers = env.getContainerService().getAll();
        if (containers.isEmpty()) {
            System.out.println("No containers found");
            return;
        }
        System.out.printf("%-5s %-20s %-10s%n", "ID", "Name", "Type");
        for (Container c : containers) {
            System.out.printf("%-5d %-20s %-10s%n", c.getId(), c.getName(), c.getType());
        }
    }
}
