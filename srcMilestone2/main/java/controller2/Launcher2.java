package controller2;

import entities2.ProjectToAnalyze;
import entities2.VariableModel;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instances;
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
            selectedProject.setArffFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPERMetrics.arff");
            selectedProject.setCsvFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPERMetrics.csv");
            selectedProject.setProjectName("BOOKKEEPER");
            selectedProject.setFirstRelease("4.0.0");
        } else if(PROJECT_NAME == "STORM"){
            selectedProject.setPath(pathToFile + "\\stormFiles\\STORM");
            selectedProject.setArffFile(pathToFile + "\\stormFiles\\STORMMetrics.arff");
            selectedProject.setCsvFile(pathToFile + "\\stormFiles\\STORMMetrics.csv");
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
        Instances trainingSet = null;
        ConverterUtils.DataSource sourceTrainingSet = null;

        for(int release = 0; release < releaseNumber - 1; releaseNumber++){
            if(release != 0){
                trainingSet = loadAndPrepareTrainingData(arffFileList.get(release), selectedProject, trainingSet);
            } else {
                try{
                    sourceTrainingSet = new ConverterUtils.DataSource(arffFileList.get(release));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try{
                    trainingSet = sourceTrainingSet.getDataSet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ConverterUtils.DataSource sourceTestingSet = null;
            try{
                sourceTestingSet = new ConverterUtils.DataSource(arffFileList.get(release + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Instances testingSet = null;
            try{
                testingSet = sourceTestingSet.getDataSet();
            } catch (Exception e) {
                e.printStackTrace();
            }

            int attributeNumber = trainingSet.numAttributes();
            trainingSet.setClassIndex(attributeNumber - 1);
            testingSet.setClassIndex(attributeNumber - 1);

            calculateMetricsForCombination(testingSet, trainingSet, release, selectedProject, metric);
        }


    }

    private static Instances loadAndPrepareTrainingData(String arffFile, ProjectToAnalyze selectedProject, Instances trainingSet){
        ConverterUtils.DataSource newSourceTrainingSet = null;
        try{
            newSourceTrainingSet = new ConverterUtils.DataSource(arffFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Instances newTrainingSet = null;
        try{
            newTrainingSet = newSourceTrainingSet.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String releaseNewTrainingSet = null;
        try{
            releaseNewTrainingSet = newSourceTrainingSet.getDataSet().attribute(0).value(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // cambia il version number per fare il merge
        newTrainingSet.renameAttributeValue(newTrainingSet.attribute("Version Number"), releaseNewTrainingSet, selectedProject.getFirstRelease());
        return mergeDataInstances(trainingSet, newTrainingSet);

    }

    private static void calculateMetricsForCombination(Instances testingSet, Instances trainingSet, int lastRelease, ProjectToAnalyze selectedProject, VariableModel metric) {
        NaiveBayes naiveBayesClassifier = new NaiveBayes();
        IBk ibkClassifier = new IBk();
        RandomForest randomForestClassifier = new RandomForest();

        CalculateMetricController metricCalc = new CalculateMetricController();

        // calcolo percentuale di bugginess
        int[] buggyTrainingSet = metricCalc.calculateDefectStatistics(trainingSet);
        VariableModel.setBuggyTrainingSetToWrite(buggyTrainingSet);

        // applico balancing
        for(int indexForBalancingSwitch = 0; indexForBalancingSwitch < 4; indexForBalancingSwitch++){
            BalancingController balancing = new BalancingController();

            // calcolo la composizione defective del training set
            buggyTrainingSet = metricCalc.calculateDefectStatistics(trainingSet);
            float buggyTrainingSetPercentage = (buggyTrainingSet[0] / (float)trainingSet.numInstances()) * 100;

            if(buggyTrainingSet[0] > buggyTrainingSet[1]){
                // se ci sono più buggy che non buggy, la majority class sono i defective
                metric.setMajorityClassPercentage(buggyTrainingSetPercentage);
            } else {
                metric.setMajorityClassPercentage(100 - buggyTrainingSetPercentage);
            }

            Instances balancedTrainingSet = balancing.startBalancing(trainingSet, indexForBalancingSwitch, metric);
            // calcolo la composizione defective di training set e testing set
            buggyTrainingSet = metricCalc.calculateDefectStatistics(balancedTrainingSet);
            int[] buggyTestingSet = metricCalc.calculateDefectStatistics(testingSet);

            metric.setBuggyTestingSet(buggyTestingSet);
            metric.setBuggyTrainingSet(buggyTrainingSet);

            // applico fs




        }
        // uso sensitive cost classifier

        // infine uso i classificatori per calcolare le metriche
    }

    private static Instances mergeDataInstances(Instances trainingSet, Instances newTrainingSet) {

        // Instances dest = new Instances(data)....

        // return dest
    }

}