package org.example.command;

import org.example.dataLoader.DAO;

/**
 * Klasse zur Implementierung des Befehls "Report"
 */
public class ReportCommand extends Command {
    public ReportCommand(String[] argument) {
        super(argument);
        cacheable = true;
    }

    @Override
    public String execute() {
        DAO dao = DAO.getDao();
        if (arguments.length == 2) {
            return dao.report(arguments[1]);
        }
        return " Bad arguments";
    }
}
