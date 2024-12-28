package org.example.datastructure.command;

/**
 * Klasse zur Implementierung des Befehls "Error"
 */
public class ErrorCommand extends Command {
    public ErrorCommand(String[] argument) {
        super(argument);
    }

    @Override
    public String execute() {
        return "Error";
    }
}
