package ru.itmo.marimiari.command;

import ru.itmo.marimiari.domain.Container;
import ru.itmo.marimiari.domain.Slot;
import ru.itmo.marimiari.interpreter.Command;
import ru.itmo.marimiari.interpreter.CommandException;
import ru.itmo.marimiari.interpreter.Environment;

import java.util.List;

public class ContShowCommand extends Command {
    public ContShowCommand() {
        super("cont_show", "show container's data", false);
    }

    @Override
    public void checkArgs(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("Usage: cont_show <container_id>");
        }
        try {
            Long.parseLong(args[0]);
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid id: " + args[0]);
        }
    }

    @Override
    public void execute(Environment env, String[] args) throws CommandException {
        long id = Long.parseLong(args[0]);
        Container c = env.getContainerService().get(id)
                .orElseThrow(() -> new CommandException("Container not found"));
        System.out.println("Container #" + c.getId());
        System.out.println("name: " + c.getName());
        System.out.println("type: " + c.getType());
        System.out.println("owner: " + c.getOwnerUsername());
        System.out.println("created: " + c.getCreatedAt());
        System.out.println("updated: " + c.getUpdatedAt());
        List<Slot> slots = env.getSlotService().getByContainer(id);
        System.out.println("slots: " + slots.size());
    }
}
