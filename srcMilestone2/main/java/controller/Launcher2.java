package controller;

import entities2.ProjectToAnalyze;
import entities2.VariableModel;

import java.io.FileWriter;
import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.converters.ConverterUtils;

public class Launcher2 {

    private static final String PROJECT_NAME = "BOOKKEEPER";
    private static FileWriter outputCsv;
    private static int releaseNumber;

    public static void main(String[] args) throws Exception {
        String pathToFile = System.getProperty("user.dir");
        ProjectToAnalyze selectedProject = new ProjectToAnalyze();

        if(PROJECT_NAME == "BOOKKEEPER"){
            selectedProject.setPath(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER");
            selectedProject.setArffFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER.arff");
            selectedProject.setCsvFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER.csv");
            selectedProject.setProjectName("BOOKKEEPER");
            selectedProject.setFirstRelease("4.0.0");
        } else if(PROJECT_NAME == "STORM"){
            selectedProject.setPath(pathToFile + "\\stormFiles\\STORM");
            selectedProject.setArffFile(pathToFile + "\\stormFiles\\STORM.arff");
            selectedProject.setCsvFile(pathToFile + "\\stormFiles\\STORM.csv");
            selectedProject.setProjectName("STORM");
            selectedProject.setFirstRelease(""); // impostare la prima release di storm. qual è?
        }

        // preparo il csv di output con i risultati finali
            // manca la scrittura dei nomi delle colonne nel metodo createOutputCSV
        MLcsvController mlCsvController = new MLcsvController();
        outputCsv = mlCsvController.createOutputCSV(selectedProject.getPath());

        VariableModel metric = new VariableModel();

        ArrayList<String> arffFileList = new ArrayList<>();
        ArrayList<String> csvFileList = new ArrayList<>();

        // ARFF CONVERTER
        csvToArff csvConverter = new csvToArff();
        String[] csvFilesToConvert = {selectedProject.getCsvFile(), selectedProject.getArffFile()};
        csvConverter.csvToArff(csvFilesToConvert);
        //csvToArff csvConverter = new csvToArff();
        //csvConverter.csvToArff(selectedProject.getPath(), selectedProject.getProjectName());
        // converted!


        ConverterUtils.DataSource source = new ConverterUtils.DataSource(selectedProject.getArffFile());
        int featureNumber = source.getDataSet().numAttributes();
        Attribute versionFeature = source.getDataSet().attribute(0);
        releaseNumber = versionFeature.numValues();

        for(int versionToSplitOver = 0; versionToSplitOver < releaseNumber; versionToSplitOver++){
            String currentCsvFile = selectedProject.getPath();
            currentCsvFile += ("_" + String.valueOf(versionToSplitOver + 1) + ".csv");
            csvFileList.add(currentCsvFile);

            String currentArffFile = selectedProject.getPath();
            currentArffFile += ("_" + String.valueOf(versionToSplitOver + 1) + ".arff");
            arffFileList.add(currentArffFile);
        }
        // split di csv in base alla versione (può essere utile farlo)
        mlCsvController.split(selectedProject.getCsvFile(), csvFileList, selectedProject.getFirstRelease(), featureNumber);

        for (int k = 0; k < releaseNumber; k++){
            csvFilesToConvert[0] = csvFileList.get(k);
            csvFilesToConvert[1] = arffFileList.get(k);

            csvConverter.csvToArff(csvFilesToConvert);
        }
        walkForward(arffFileList, selectedProject, metric);

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

    // implementazione walk forward
    private static void walkForward(ArrayList<String> arffFileList, ProjectToAnalyze selectedProject, VariableModel metric) {
    //...
    }
}
