package ru.itmo.marimiari.interpreter;

import ru.itmo.marimiari.exception.ValidationException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandInterpreter {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Environment environment;
    private final Scanner scanner;

    public CommandInterpreter(Environment environment, Scanner scanner){
        this.environment = environment;
        this.scanner = scanner;
    }

    public void register(Command command){
        commands.put(command.getName(), command);
    }

    public Map<String, Command> getCommands(){
        return commands;
    }

    public void run(){
        while (true){
            System.out.println("> ");
            if (!scanner.hasNext()){
                return;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()){
                continue;
            }
            String[] parts = line.split("\\s+");
            String commandName = parts[0];
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);

            Command command = commands.get(commandName);
            if (command == null){
                System.out.println("Unknown command. Use help");
                continue;
            }
            try {
                command.checkArgs(args);
                if (command.isReqAdditionalInput()){
                    command.additionalInput(environment, scanner);
                }
            command.execute(environment, args);
            }
            catch (CommandException exception){
                System.out.println("Argument error: " + exception.getMessage());
                System.out.println("Tip: " + command.getHelp());
            }
            catch (ValidationException exception){
                System.out.println("Validation error: " + exception.getMessage());
            }
            catch (Exception exception){
                System.out.println("Unknown error: " + exception.getMessage());
            }
        }
    }
}
