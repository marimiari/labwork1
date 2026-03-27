package ru.itmo.marimiari;

import ru.itmo.marimiari.command.*;
import ru.itmo.marimiari.domain.AppData;
import ru.itmo.marimiari.interpreter.CommandInterpreter;
import ru.itmo.marimiari.interpreter.Environment;
import ru.itmo.marimiari.service.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        SampleService sampleService = new SampleService();
        ContainerService containerService = new ContainerService();
        SlotService slotService = new SlotService(containerService);
        PlacementService placementService = new PlacementService(sampleService, containerService, slotService);

        AppData.loadDemoData(sampleService, containerService, slotService, placementService);

        Environment environment = new Environment(sampleService, containerService, slotService, placementService);

        Scanner scanner = new Scanner(System.in);
        CommandInterpreter interpreter = new CommandInterpreter(environment, scanner);

        interpreter.register(new HelpCommand(interpreter));
        interpreter.register(new ExitCommand());
        interpreter.register(new ContAddCommand());
        interpreter.register(new ContListCommand());
        interpreter.register(new ContShowCommand());
        interpreter.register(new SlotCreateCommand());
        interpreter.register(new SlotListCommand());
        interpreter.register(new PlacePutCommand());
        interpreter.register(new PlaceMoveCommand());
        interpreter.register(new PlaceRemoveCommand());
        interpreter.register(new PlaceFindCommand());
        interpreter.register(new ContMapCommand());
        interpreter.register(new LoadCommand());
        interpreter.register(new SaveCommand());

        interpreter.run();
    }
}
