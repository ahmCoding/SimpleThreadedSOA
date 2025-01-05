package org.example.dataLoader;

import org.example.wdi.WDI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Klasse zum Laden von Datensätzen
 */
public class DatasetLoader {


    /**
     * Funktion zum Laden von einem Datensatz
     *
     * @param path Pfad zu der CSV-Datei
     * @return Liste von WDI-Objekten
     */
    public List<WDI> loadDataset(String path) {
        List<WDI> dataSet = new ArrayList<>();
        Path file = Paths.get(path);

        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(path);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inStream))) {
            String line = null;
            //skip first line with column names
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = parse(line);
                WDI dataObject = new WDI();
                dataObject.setData(data);
                dataSet.add(dataObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    /**
     * Funktion zum Parsen einer Zeile von der CSV-Datei. Die CSV-Datei ist so formatiert " "A","B","C","
     *
     * @param line Zeile von der CSV-Datei als String
     * @return Array von Strings
     */
    private String[] parse(String line) {
        StringBuilder sb = new StringBuilder();
        List<String> data = new ArrayList<>();
        // ',' innerhalb des Zellenwertes wird ignoriert
        boolean endOfCell = false; // toggled zwischen true und false
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '"') {
                endOfCell = !endOfCell;
                continue;
            }
            if (line.charAt(i) == ',' && !endOfCell) {
                data.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(line.charAt(i));
            }

        }
        return data.toArray(String[]::new);
    }
}
