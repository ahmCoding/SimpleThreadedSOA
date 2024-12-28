package org.example.datastructure.command;

import org.example.helper.DAO;

/**
 * Klasse zur Implementierung des Befehls "Report"
 */
public class ReportCommand extends Command {
    public ReportCommand(String[] argument) {
        super(argument);
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
