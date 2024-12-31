package org.example.command;

import java.rmi.RemoteException;

/**
 * Abstrakte Klasse für die Implementierung von Befehlen
 */
public abstract class Command {
    protected String[] arguments;
    protected boolean cacheable;

    public Command(String[] argument) {
        this.arguments = argument;
        cacheable = false;
    }

    /**
     * Konstruktor für Befehle ohne Argumente (Hauptsächlich für Tests)
     *
     * @param command Befehl
     */
    public Command(String command) {
        this(new String[]{command});
    }

    /**
     * Führt den Befehl aus
     *
     * @return Rückgabe des Befehls
     */
    public abstract String execute() throws RemoteException;

    /**
     * Gibt an, ob der Befehle geeignet zum Zwischenspeichern ist.
     *
     * @return true für geeignet.
     */
    public boolean isCacheable() {
        return cacheable;
    }
}
