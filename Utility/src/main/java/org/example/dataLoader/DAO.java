package org.example.dataLoader;

import org.example.wdi.WDI;

import java.io.StringWriter;
import java.util.List;

/**
 * Klasse zur Verwaltung von Daten. Diese Klasse ist als Singleton implementiert
 */
public class DAO {

    private static DAO dao;
    private List<WDI> dataset;

    private DAO(String path) {
        DatasetLoader datasetLoader = new DatasetLoader();
        dataset = datasetLoader.loadDataset(path);
    }

    /**
     * Funktion zur Rückgabe des DAO-Objekts
     *
     * @return DAO-Objekt
     */
    public static DAO getDao() {
        if (dao == null) {
            dao = new DAO(Config.DATASET_PATH);
        }
        return dao;
    }

    /**
     * Funktion zur Rückgabe des Datensatzes
     *
     * @return Liste von WDI-Objekten
     */
    public List<WDI> getDataset() {
        return dataset;
    }

    /**
     * Funktion zur Abfrage von allen vorhandenen Daten zu einem Land und einem Indikator
     *
     * @param codCountry
     * @param codIndicator
     * @return String mit allen Daten in Form von "Land;Indikator;Wert1;Wert2;...;Wert63"
     */
    public String query(String codCountry, String codIndicator) {
        WDI wdi = null;
        for (WDI sample : dataset) {
            if (sample == null) continue;
            wdi = sample;
            if ((wdi.getCountryCode().equals(codCountry)) && (wdi.getIndicatorCode().equals(codIndicator)))
                break;
        }
        StringWriter writer = new StringWriter();
        writer.write(codCountry);
        writer.write(";");
        writer.write(codIndicator);
        writer.write(";");
        Double[] years = wdi.getValues();
        for (int i = 0; i < years.length; i++) {
            writer.write(years[i].toString());
            if (i < years.length - 1) {
                writer.write(";");
            }
        }
        return writer.toString();
    }

    /**
     * Funktion zur Abfrage von einem Indikator-Wert zu einem Land für ein bestimmtes Jahr
     *
     * @param codCountry
     * @param codIndicator
     * @param year
     * @return String mit den Daten in Form von "Land;Indikator;Jahr;Wert"
     * @throws Exception
     */
    public String query(String codCountry, String codIndicator, short year) throws IllegalArgumentException {
        WDI wdi = null;
        for (WDI sample : dataset) {
            if (sample == null) continue;
            wdi = sample;
            if ((wdi.getCountryCode().equals(codCountry)) && (wdi.getIndicatorCode().equals(codIndicator))) {
                break;
            }
        }
        StringWriter writer = new StringWriter();
        writer.write(codCountry);
        writer.write(";");
        writer.write(codIndicator);
        writer.write(";");
        writer.write("" + year);
        writer.write(";");
        writer.write(wdi.getValue(year).toString());
        return writer.toString();
    }

    /**
     * Funktion zur Erstellung eines Berichts für mit dem Mittelwert eines Indikators für alle Länder über alle Jahre
     *
     * @param codIndicator
     * @return String mit den Daten in Form von "codeIndicator; countryCode; meanValue"
     */
    public String report(String codIndicator) {

        StringWriter writer = new StringWriter();
        writer.write(codIndicator);
        writer.write(";");
        for (WDI wdi : dataset) {
            if (wdi == null) continue;
            if (wdi.getIndicatorCode().equals(codIndicator)) {
                Double[] ValueOfyears = wdi.getValues();
                // ohne stream API und mit for-loop, ist es bei serial version effizienter
                double sum = 0;
                int nonZeroEntry = 0;
                for (Double value : ValueOfyears) {
                    sum += value;
                    if (value != 0.0d) nonZeroEntry++;
                }
                double mean = sum / nonZeroEntry;
                writer.write(wdi.getCountryCode());
                writer.write(";");
                writer.write("" + mean);
                writer.write(";");
            }
        }
        return writer.toString();
    }

}
