package org.example.command;

/**
 * Klasse zur Implementierung des Befehls "Fake" zum Testzwecken
 */
public class FakeCommand extends Command{
    public FakeCommand(String command) {
        super(command);
    }

    @Override
    public String execute() {
        return "Fake command does nothing";
    }
}
