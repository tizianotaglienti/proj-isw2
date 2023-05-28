package controller;

import entities.ProjectToAnalize;
import entities.VariableModel;

import java.io.IOException;
import java.util.ArrayList;

public class Launcher2 {

    private static final String PROJECT_NAME = "STORM";

    public static void main(String[] args) throws IOException {
        String pathToFile = System.getProperty("user.dir");
        ProjectToAnalize selectedProject = new ProjectToAnalize();

        if(PROJECT_NAME == "BOOKKEEPER"){
            selectedProject.setPath(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER");
            selectedProject.setArffFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER.arff");
            selectedProject.setCsvFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER.csv");
            selectedProject.setProjectName("BOOKKEEPER");
            // ...
        } else if(PROJECT_NAME == "STORM"){
            selectedProject.setPath(pathToFile + "\\stormFiles\\STORM");
            selectedProject.setArffFile(pathToFile + "\\stormFiles\\STORM.arff");
            selectedProject.setCsvFile(pathToFile + "\\stormFiles\\STORM.csv");
            selectedProject.setProjectName("STORM");
            // ...
        }

        // preparo il csv di output con i risultati finali
            // manca la scrittura dei nomi delle colonne nel metodo createOutputCSV
        MLcsvController mlCsvController = new MLcsvController();
        mlCsvController.createOutputCSV(selectedProject.getPath());

        VariableModel metric = new VariableModel();

        ArrayList<String> arffFileList = new ArrayList<>();
        ArrayList<String> csvFileList = new ArrayList<>();

        // ARFF CONVERTER
        csvToArff csvConverter = new csvToArff();
        csvConverter.csvToArff(selectedProject.getPath(), selectedProject.getProjectName());
        // converted!


        // split di csv in base alla versione (può essere utile farlo) - primo step prossima volta

        // lo converte in file .arff

        // chiama un controller weka

        /**
         * SCOPO: fornire un .csv che contengatutte le combinazioni per le variabili sotto.
         *
         * NECESSARIO USARE:
         *  Walk forward come tecnica di valutazione
         *  RandomForest ,NaiveBayes eIBKcome classificatori
         * POSSIBILI VARIABILI DA VALIDARE EMPIRICAMENTE:
         *  No selection / best first - (come feature selection)
         *  No sampling / oversampling / undersampling / SMOTE - (come balancing)
         *  No cost sensitive / sensitive threshold / sensitive learning (CFN = 10*CFP)
         *
         * Rispondere a SE e QUALI tecniche di FS, balancing o sensitivity
         * aumentano l'accuratezza dei classificatori, per quali classificatori e per quali dataset.
         **/

    }
}
