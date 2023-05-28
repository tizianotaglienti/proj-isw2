package controller;

import java.io.FileWriter;
import java.io.IOException;

public class MLcsvController {
    public FileWriter createOutputCSV(String filePath) throws IOException {
        FileWriter outputCSV = new FileWriter(filePath + "_OUTPUT.csv");
        outputCSV.append(""); // AGGIUNGERE COSE DA SCRIVERE NEL .CSV

        return outputCSV;
    }
}
