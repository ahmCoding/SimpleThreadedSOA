package org.example.command;

import org.example.dataLoader.DAO;

/**
 * Klasse zur Implementierung des Befehls "Query"
 */
public class QueryCommand extends Command {
    public QueryCommand(String[] argument) {
        super(argument);
        cacheable = true;
    }

    @Override
    public String execute() {
        DAO dao = DAO.getDao();
        if (arguments.length == 3) {
            return dao.query(arguments[1], arguments[2]);
        }
        if (arguments.length == 4) {
            try {
                return dao.query(arguments[1], arguments[2], Short.parseShort(arguments[3]));
            } catch (IllegalArgumentException e) {
                return "Year must be a number " + e.getMessage();
            }
        }
        return " Bad arguments";
    }
}
