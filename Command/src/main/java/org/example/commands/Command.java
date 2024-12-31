package org.example.commands;

/**
 * Abstrakte Klasse für die Implementierung von Befehlen
 */
public abstract class Command {
    protected String[] arguments;
    protected boolean cacheable;

    public Command(String[] argument) {
        this.arguments = argument;cacheable = false;
    }
    /**
     * Konstruktor für Befehle ohne Argumente (Hauptsächlich für Tests)
     *
     * @param command Befehl
     */
    public Command(String command){
        this(new String[]{command});
    }
    /**
     * Führt den Befehl aus
     *
     * @return Rückgabe des Befehls
     */
    public abstract String execute();
}
