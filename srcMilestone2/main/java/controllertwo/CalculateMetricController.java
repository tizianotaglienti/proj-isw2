package controllertwo;

import entitiestwo.ProjectToAnalyze;

import entitiestwo.VariableModel;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;

public class CalculateMetricController {

    /**
     * Calcola le metriche e scrive i risultati nel file di output CSV.
     *
     * @param eval             L'oggetto Evaluation contenente i risultati dell'analisi.
     * @param project          Il progetto analizzato.
     * @param releaseNumber    Il numero della release.
     * @param classifier       Il nome del classificatore utilizzato.
     * @param csvOutput        Il FileWriter per il file di output CSV.
     */

    public void metricCalculator(Evaluation eval, ProjectToAnalyze project, int releaseNumber, String classifier, FileWriter csvOutput){

        // calcolo percentuale di bugginess del training set e del testing set
        int[] buggyTrainingSet = VariableModel.getBuggyTrainingSetToWrite();
        int[] buggyTestingSet = VariableModel.getBuggyTestingSet();

        float totalTrainingSetInstances = (buggyTrainingSet[0] + buggyTrainingSet[1]);
        float buggyTrainingSetPercentage = (buggyTrainingSet[0] / totalTrainingSetInstances) * 100;

        float totalTestingSetInstances = (buggyTestingSet[0] + buggyTestingSet[1]);
        float buggyTestingSetPercentage = (buggyTestingSet[0] / totalTestingSetInstances) * 100;

        float trainingSetPercentage = (totalTrainingSetInstances / (totalTrainingSetInstances + totalTestingSetInstances)) * 100;

        // scrittura del file di output
        try{
            csvOutput.append(project.getProjectName() + ","
                                + releaseNumber + ","
                                + trainingSetPercentage + ","
                                + buggyTrainingSetPercentage + ","
                                + buggyTestingSetPercentage + ","
                                + classifier + ","
                                + VariableModel.getBalancing() + ","
                                + VariableModel.getFeatureSelection() + ","
                                + VariableModel.getSensitivity() + ","
                                + eval.numTruePositives(0) + ","
                                + eval.numFalsePositives(0) + ","
                                + eval.numTrueNegatives(0) + ","
                                + eval.numFalseNegatives(0) + ","
                                + eval.precision(0) + ","
                                + eval.recall(0) + ","
                                + eval.areaUnderROC(0) + ","
                                + eval.kappa() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            csvOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calcola il numero di classi buggy e non buggy per l'insieme di istanze del progetto.
     *
     * @param projectSetOfInstances L'insieme di istanze del progetto.
     * @return Un array di interi contenente il numero di classi difettose e il numero di classi non difettose.
     */

    public int[] calculateDefectStatistics(Instances projectSetOfInstances) {
        int numBuggyClasses = 0;
        int numNonBuggyClasses = 0;
        int[] defectStatistics = {numBuggyClasses, numNonBuggyClasses};
        int totalInstances = projectSetOfInstances.numInstances();
        int bugginessAttributeIndex = 10;

        for (int i = 1; i < totalInstances; i++) {
            Instance currentInstance = projectSetOfInstances.instance(i);

            if (currentInstance.stringValue(bugginessAttributeIndex).equals("true")) {
                numBuggyClasses++;
            } else {
                numNonBuggyClasses++;
            }
        }

        defectStatistics[0] = numBuggyClasses;
        defectStatistics[1] = numNonBuggyClasses;

        return defectStatistics;
    }
}
