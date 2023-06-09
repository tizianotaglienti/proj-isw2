package controller;

import entities.FileEntity;
import entities.Project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvController {
    private FileWriter csv;

    /**
     * Costruttore della classe CsvController.
     * Inizializza il FileWriter per il file CSV dei risultati.
     * Crea il file CSV con le metriche del progetto.
     *
     * @param project Il progetto su cui basare il file CSV.
     */

    public CsvController(Project project){
        try {
            csv = initializeCSVResult(project);
        } catch (IOException e){
            e.printStackTrace();
        }
        createCSV(project);
    }

    /**
     * Crea il file CSV con le metriche del progetto.
     *
     * @param project Il progetto su cui basare il file CSV.
     */

    public void createCSV(Project project) {
        for(int k = project.getFileList().size() - 1; k >= 0; k--){
            List<String> metrics = new ArrayList<>();
            FileEntity currentFile = project.getFileList().get(k);

            int index = currentFile.getVersionIndex();

            metrics.add(project.getVersionList().get(index).getName());

            metrics.add(currentFile.getFileName());
            metrics.add(Integer.toString(currentFile.getLocTouched()));
            metrics.add(Integer.toString(currentFile.getNumberRevisions()));
            metrics.add(Integer.toString(currentFile.getNumberBugFix()));
            metrics.add(Integer.toString(currentFile.getLocAdded()));
            metrics.add(Integer.toString(currentFile.getMaxLocAdded()));
            metrics.add(Integer.toString(currentFile.getChgSetSize()));
            metrics.add(Integer.toString(currentFile.getMaxChgSet()));
            metrics.add(Float.toString(currentFile.getAvgChgSet()));
            metrics.add(Float.toString(currentFile.getAvgLocAdded()));
            metrics.add(Boolean.toString(currentFile.isBuggy()));

            boolean hasBuggyTrue = hasBuggyTrueForVersion(project.getFileList(), index);
            boolean hasBuggyFalse = hasBuggyFalseForVersion(project.getFileList(), index);
            // parametri utilizzati per gestire le versioni in cui compaiono solo entry con Buggy = true o Buggy = false

            try{
                if(currentFile.getLocTouched() != 0 && (hasBuggyTrue && hasBuggyFalse)) {
                    addRowToCSV(metrics, csv);
                }
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    /**
     * Verifica se esiste almeno un file buggy con l'indice di versione specificato.
     *
     * @param fileList          Lista dei file del progetto.
     * @param versionIndex      Indice della versione da controllare.
     * @return true se esiste almeno un file buggy con l'indice di versione specificato, false altrimenti.
     */
    private boolean hasBuggyTrueForVersion(List<FileEntity> fileList, int versionIndex) {
        for (FileEntity file : fileList) {
            if (file.getVersionIndex() == versionIndex && file.isBuggy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se esiste almeno un file non buggy con l'indice di versione specificato.
     *
     * @param fileList          Lista dei file del progetto.
     * @param versionIndex      Indice della versione da controllare.
     * @return true se esiste almeno un file non buggy con l'indice di versione specificato, false altrimenti.
     */

    private boolean hasBuggyFalseForVersion(List<FileEntity> fileList, int versionIndex) {
        for (FileEntity file : fileList) {
            if (file.getVersionIndex() == versionIndex && !file.isBuggy()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Aggiunge una riga al file CSV.
     *
     * @param nextLine La lista di stringhe che rappresentano i valori da aggiungere alla riga.
     * @param csv      Il FileWriter del file CSV.
     * @throws IOException Se si verifica un'eccezione durante l'aggiunta della riga al file.
     */

    private static void addRowToCSV(List<String> nextLine, FileWriter csv) throws IOException {
        StringBuilder stringToAppend = new StringBuilder();
        int c = 0;
        for(String token : nextLine){
            if(c != 0) stringToAppend.append(",");
            stringToAppend.append(token);
            c++;
        }
        // aggiungo la riga al file csv
        csv.append(stringToAppend + "\n");
    }

    /**
     * Inizializza il FileWriter per il file CSV dei risultati.
     *
     * @param project Il progetto su cui basare il file CSV.
     * @return Il FileWriter inizializzato per il file CSV.
     * @throws IOException Se si verifica un'eccezione durante l'inizializzazione del file CSV.
     */

    public FileWriter initializeCSVResult(Project project) throws IOException {
        String filePath = project.getName().toLowerCase() + "Files/"+ project.getName() + "Metrics.csv";
        try(FileWriter csvResult = new FileWriter(filePath)){
            csvResult.append("Version Number," + "File Name," + "LOC Touched," + "Number of Revisions," + "Number of Bug Fixed," + "LOC Added," + "Max LOC Added," + "Chg Set Size," + "Max Chg Set," + "Avg Chg Set," + "Avg LOC Added," + "Buggy" + "\n");
            return csvResult;
        }
    }

}
