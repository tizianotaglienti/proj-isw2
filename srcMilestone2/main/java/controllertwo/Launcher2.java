package controllertwo;

import entitiestwo.ProjectToAnalyze;
import entitiestwo.VariableModel;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class Launcher2 {

    private static final String PROJECT_NAME = "STORM";
    private static final String MYSTRINGSTORM = "STORM";
    private static final String MYSTRINGBK = "BOOKKEEPER";
    private static FileWriter outputCsv;
    private static int releaseNumber;

    /**
     * Main method per l'avvio del programma.
     *
     * @param args gli argomenti della riga di comando.
     * @throws Exception se si verifica un errore durante l'esecuzione.
     * @return void.
     */

    public static void main(String[] args) throws Exception{
        String pathToFile = System.getProperty("user.dir");
        ProjectToAnalyze selectedProject = new ProjectToAnalyze();

        if(PROJECT_NAME.equals(MYSTRINGBK)){
            selectedProject.setPath(pathToFile + "\\bookkeeperFiles\\BOOKKEEPER");
            selectedProject.setArffFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPERMetrics.arff");
            selectedProject.setCsvFile(pathToFile + "\\bookkeeperFiles\\BOOKKEEPERMetrics.csv");
            selectedProject.setProjectName(MYSTRINGBK);
            selectedProject.setFirstRelease("4.0.0");
        } else if (PROJECT_NAME.equals(MYSTRINGSTORM)){
            selectedProject.setPath(pathToFile + "\\stormFiles\\STORM");
            selectedProject.setArffFile(pathToFile + "\\stormFiles\\STORMMetrics.arff");
            selectedProject.setCsvFile(pathToFile + "\\stormFiles\\STORMMetrics.csv");
            selectedProject.setProjectName(MYSTRINGSTORM);
            selectedProject.setFirstRelease("0.09.0.1");
        }

        // Preparazione del file CSV di output per i risultati finali
        MLcsvController mlCsvController = new MLcsvController();
        outputCsv = mlCsvController.createOutputCSV(selectedProject.getPath());

        ArrayList<String> arffFileList = new ArrayList<>();
        ArrayList<String> csvFileList = new ArrayList<>();

        // Conversione dei file CSV in file ARFF
        CsvToArff csvConverter = new CsvToArff();
        String[] csvFilesToConvert = {selectedProject.getCsvFile(), selectedProject.getArffFile()};
        csvConverter.csvToArffConverter(csvFilesToConvert);

        // Caricamento del dataset e suddivisione in base alle versioni
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(selectedProject.getArffFile());
        int featureNumber = source.getDataSet().numAttributes();
        Attribute versionFeature = source.getDataSet().attribute(0);
        releaseNumber = versionFeature.numValues(); // 6

        for(int versionToSplitOver = 0; versionToSplitOver < releaseNumber; versionToSplitOver++){
            String currentCsvFile = selectedProject.getPath();
            currentCsvFile += ("_" + (versionToSplitOver + 1) + ".csv");
            csvFileList.add(currentCsvFile);

            String currentArffFile = selectedProject.getPath();
            currentArffFile += ("_" + (versionToSplitOver + 1) + ".arff");
            arffFileList.add(currentArffFile);
        }

        // split dei csv in base alla versione
        mlCsvController.split(selectedProject.getCsvFile(), csvFileList, selectedProject.getFirstRelease(), featureNumber);

        for (int k = 0; k < releaseNumber; k++){
            csvFilesToConvert[0] = csvFileList.get(k);
            csvFilesToConvert[1] = arffFileList.get(k);

            csvConverter.csvToArffConverter(csvFilesToConvert);
        }
        walkForward(arffFileList, selectedProject);

    }

    /*********************************
     * SCOPO MILESTONE2 : fornire un .csv che contenga tutte le combinazioni per le variabili sotto.
     *
     * NECESSARIO USARE:
     *  Walk forward come tecnica di valutazione
     *  RandomForest, NaiveBayes e IBK come classificatori
     * POSSIBILI VARIABILI DA VALIDARE EMPIRICAMENTE:
     *  No selection / best first - (come feature selection)
     *  No sampling / oversampling / undersampling / SMOTE - (come balancing)
     *  No cost sensitive / sensitive threshold / sensitive learning (CFN = 10*CFP)
     *
     * Rispondere a SE e QUALI tecniche di FS, balancing o sensitivity
     * aumentano l'accuratezza dei classificatori, per quali classificatori e per quali dataset.
     ********************************/



    /**
     * Metodo che esegue il processo di "walk forward", addestrando e testando i modelli su diverse combinazioni di versioni.
     *
     * @param arffFileList    Elenco dei file ARFF.
     * @param selectedProject Progetto selezionato.
     * @throws Exception se si verifica un errore durante l'esecuzione.
     * @return void.
     */

    private static void walkForward(ArrayList<String> arffFileList, ProjectToAnalyze selectedProject) throws Exception {
        Instances trainingSet = null;
        ConverterUtils.DataSource sourceTrainingSet = new ConverterUtils.DataSource("");

        for(int release = 0; release < releaseNumber - 1; release++){
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
            ConverterUtils.DataSource sourceTestingSet = new ConverterUtils.DataSource("");
            try{
                sourceTestingSet = new ConverterUtils.DataSource(arffFileList.get(release + 1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            Instances testingSet = new Instances("testingSet", new ArrayList<>(), 0);
            try{
                testingSet = sourceTestingSet.getDataSet();
            } catch (Exception e) {
                e.printStackTrace();
            }

            int attributeNumber = trainingSet.numAttributes();
            trainingSet.setClassIndex(attributeNumber - 1);
            testingSet.setClassIndex(attributeNumber - 1);

            calculateMetricsForCombination(testingSet, trainingSet, release, selectedProject);
        }

    }

    /**
     * Metodo che carica e prepara i dati di addestramento.
     *
     * @param arffFile        File ARFF da caricare e preparare.
     * @param selectedProject Progetto selezionato.
     * @param trainingSet     Istanza di addestramento esistente.
     * @throws Exception se si verifica un errore durante il caricamento e la preparazione dei dati di addestramento.
     * @return Instances, istanza di addestramento aggiornata.
     */

    private static Instances loadAndPrepareTrainingData(String arffFile, ProjectToAnalyze selectedProject, Instances trainingSet) throws Exception {
        ConverterUtils.DataSource newSourceTrainingSet = new ConverterUtils.DataSource(arffFile);

        Instances newTrainingSet = new Instances("newTrainingSet", new ArrayList<>(), 0);
        try{
            newTrainingSet = newSourceTrainingSet.getDataSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String releaseNewTrainingSet = null;
        try{
            releaseNewTrainingSet = newSourceTrainingSet.getDataSet().attribute(0).value(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // cambia il version number per fare il merge
        newTrainingSet.renameAttributeValue(newTrainingSet.attribute("Version Number"), releaseNewTrainingSet, selectedProject.getFirstRelease());
        return mergeDataInstances(trainingSet, newTrainingSet);

    }

    /**
     * Metodo che calcola le metriche per una specifica combinazione di dati di test e addestramento.
     *
     * @param testingSet      Istanza di test.
     * @param trainingSet     Istanza di addestramento.
     * @param lastRelease     Numero dell'ultima release.
     * @param selectedProject Progetto selezionato.
     * @throws Exception se si verifica un errore durante il calcolo delle metriche.
     * @return void.
     */

    private static void calculateMetricsForCombination(Instances testingSet, Instances trainingSet, int lastRelease, ProjectToAnalyze selectedProject) throws Exception {
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
                VariableModel.setMajorityClassPercentage(buggyTrainingSetPercentage);
            } else {
                VariableModel.setMajorityClassPercentage(100 - buggyTrainingSetPercentage);
            }

            Instances balancedTrainingSet = balancing.startBalancing(trainingSet, indexForBalancingSwitch);
            // calcolo la composizione defective di training set e testing set
            buggyTrainingSet = metricCalc.calculateDefectStatistics(balancedTrainingSet);
            int[] buggyTestingSet = metricCalc.calculateDefectStatistics(testingSet);

            VariableModel.setBuggyTrainingSet(buggyTrainingSet);
            VariableModel.setBuggyTestingSet(buggyTestingSet);

            // applico feature selection
            for(int indexForFsSwitch = 0; indexForFsSwitch < 2; indexForFsSwitch++){
                FeatureSelectionController featureSelection = new FeatureSelectionController();
                List<Instances> dataset = new ArrayList<>();

                try{
                    dataset = featureSelection.startFeatureSelection(balancedTrainingSet, testingSet, indexForFsSwitch);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Instances trainingSetAfterFeatureSelection = dataset.get(0);
                Instances testingSetAfterFeatureSelection = dataset.get(1);


                // uso sensitive cost classifier
                for(int indexForSensitiveSelectionSwitch = 0; indexForSensitiveSelectionSwitch < 3; indexForSensitiveSelectionSwitch++){
                    SensitiveSelectionController sensitiveSelection = new SensitiveSelectionController();

                    Evaluation randomForestEvaluation = sensitiveSelection.startSensitiveSelection(trainingSetAfterFeatureSelection, testingSetAfterFeatureSelection, indexForSensitiveSelectionSwitch, randomForestClassifier);
                    Evaluation naiveBayesEvaluation = sensitiveSelection.startSensitiveSelection(trainingSetAfterFeatureSelection, testingSetAfterFeatureSelection, indexForSensitiveSelectionSwitch, naiveBayesClassifier);
                    Evaluation ibkEvaluation = sensitiveSelection.startSensitiveSelection(trainingSetAfterFeatureSelection, testingSetAfterFeatureSelection, indexForSensitiveSelectionSwitch, ibkClassifier);

                    // infine sfrutto i classificatori per calcolare le metriche
                    metricCalc.metricCalculator(randomForestEvaluation, selectedProject, lastRelease + 1, "RandomForest", outputCsv);
                    metricCalc.metricCalculator(naiveBayesEvaluation, selectedProject, lastRelease + 1, "NaiveBayes", outputCsv);
                    metricCalc.metricCalculator(ibkEvaluation, selectedProject, lastRelease + 1, "IBk", outputCsv);
                }
            }
        }
    }

    /**
     * Metodo che unisce due istanze di dati in una singola istanza.
     *
     * @param sourceData     Istanza di dati di origine.
     * @param additionalData Istanza di dati aggiuntivi.
     * @return Instances Istanza di dati risultante dalla fusione.
     */

    private static Instances mergeDataInstances(Instances sourceData, Instances additionalData) {
        int numAttributes = sourceData.numAttributes();
        boolean[] isStringAttribute = new boolean[numAttributes];

        for(int i = 0; i < numAttributes; i++){
            Attribute attribute = sourceData.attribute(i);
            isStringAttribute[i] = ((attribute.type() == Attribute.STRING) || (attribute.type() == Attribute.NOMINAL));
        }

        Instances mergedData = new Instances(sourceData);
        mergedData.setRelationName(sourceData.relationName() + "+" + additionalData.relationName());

        ConverterUtils.DataSource additionalDataSource = new ConverterUtils.DataSource(additionalData);
        Instances additionalInstances = null;
        try {
            additionalInstances = additionalDataSource.getStructure();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instance additionalInstance = null;
        while (additionalDataSource.hasMoreElements(additionalInstances)) {
            additionalInstance = additionalDataSource.nextElement(additionalInstances);
            mergedData.add(additionalInstance);

            for (int i = 0; i < numAttributes; i++) {
                if (isStringAttribute[i]) {
                    mergedData.instance(mergedData.numInstances() - 1).setValue(i, additionalInstance.stringValue(i));
                }
            }
        }

        return mergedData;
    }

}
