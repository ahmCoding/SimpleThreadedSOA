package org.example.server;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerRemote extends Remote {
    // Server kann von verschiedenen Threads gestoppt werden, deswegen volatile

    /**
     * Startet den Server und nimmt die Clients anfragen entgegen.
     */
    public void run() throws RemoteException;

    /**
     * Stoppt den Server.
     *
     * @return Statusmeldung
     */
    public String shutdown() throws RemoteException;

    /**
     * Überprüft, ob der Server noch aktiv ist.
     *
     * @return true, wenn der Server noch aktiv ist, sonst false
     */
    public boolean isRunning() throws RemoteException;

    /**
     * Gibt den aktuellen Status des Servers zurück.
     *
     * @return Status des Servers
     */
    public String getState() throws RemoteException;

    /**
     * Gibt den Namen des Servers zurück
     *
     * @return String Name
     */
    public String getName() throws RemoteException;
}
