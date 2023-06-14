package controllertwo;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class MLcsvController {

    /**
     * Modifica il file CSV di output inserendo un header.
     *
     * @param outputCsv File CSV per cui creare l'header.
     * @throws IOException In caso di errori di input/output.
     */

    public void createOutputCSV(FileWriter outputCsv) throws IOException {
            // intestazione del file di output
            outputCsv.append("Dataset," + "#TrainingRelease," + "%Training," + "%DefectiveTraining," + "%DefectiveTesting," + "Classifier," + "Balancing," + "FeatureSelection," + "Sensitivity," + "TP,FP,TN,FN," + "Precision," + "Recall," + "AUC," + "Kappa\n");
    }

    /**
     * Effettua la divisione del file CSV in base alla versione di rilascio specificata.
     *
     * @param csvFileName   Nome del file CSV di input.
     * @param csvFileList   Lista dei nomi dei file CSV di output divisi per versione.
     * @param firstRelease  Prima versione di rilascio.
     * @param featureNumber Numero di attributi/features.
     * @throws IOException In caso di errori di input/output.
     */

    public void split(String csvFileName, List<String> csvFileList, String firstRelease, int featureNumber) throws IOException {
        String currentVersion = firstRelease;
        int version = 1;

        try (CSVReader reader = new CSVReader(new FileReader(csvFileName))) {
            String[] nextLine;
            int currentRow = 1;
            StringBuilder attributeList = new StringBuilder();

            try (FileWriter csv = new FileWriter(csvFileList.get(version - 1))) {
                while ((nextLine = reader.readNext()) != null) {
                    for (String token : nextLine) {
                        if (currentRow >= featureNumber + 2) {
                            if (token.equals(currentVersion)) {
                                currentRow = addAttribute(currentRow, featureNumber, csv, attributeList);
                            } else {
                                version++;
                                currentVersion = token;
                            }
                            addRow(nextLine, csv);
                            break;
                        } else {
                            attributeList.append(token);
                            currentRow = addFirstRow(currentRow, featureNumber, attributeList);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Aggiunge la prima riga di attributi al file CSV.
     *
     * @param currentRow    Numero della riga corrente.
     * @param featureNumber Numero di attributi/features.
     * @param attributeList StringBuilder per l'elenco degli attributi.
     * @return Il numero della riga successiva.
     */

    private int addFirstRow(int currentRow, int featureNumber, StringBuilder attributeList) {
        if(currentRow != featureNumber + 1){
            attributeList.append(",");
        }
        currentRow++;
        return currentRow;
    }

    /**
     * Aggiunge una riga al file CSV.
     *
     * @param nextLine  Array di stringhe rappresentanti i valori della riga.
     * @param csv       FileWriter del file CSV.
     * @throws IOException In caso di errori di input/output.
     */

    private void addRow(String[] nextLine, FileWriter csv) throws IOException {
        StringBuilder stringToAppendToCsv = new StringBuilder();
        int c = 0;
        for(String token : nextLine){
            if(c != 0){
                stringToAppendToCsv.append(",");
            }
            stringToAppendToCsv.append(token);
            c++;
        }
        // append della stringa al file csv
        csv.append(stringToAppendToCsv + "\n");
    }

    /**
     * Aggiunge un attributo al file CSV.
     *
     * @param currentRow    Numero della riga corrente.
     * @param featureNumber Numero di attributi/features.
     * @param csv           FileWriter del file CSV.
     * @param attributeList StringBuilder per l'elenco degli attributi.
     * @return Il numero della riga successiva.
     */

    private int addAttribute(int currentRow, int featureNumber, FileWriter csv, StringBuilder attributeList) {
        if(currentRow == featureNumber + 2){
            try{
                csv.append(attributeList + "\n");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        currentRow++;
        return currentRow;
    }
}
