package main.java.controller2;

import entities2.ProjectToAnalyze;

import entities2.VariableModel;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileWriter;
import java.io.IOException;

public class CalculateMetricController {

    public void metricCalculator(Evaluation eval, ProjectToAnalyze project, int releaseNumber, String classifier, VariableModel metric, FileWriter csvOutput){

        // calcolo percentuale di bugginess del training set e del testing set
        int[] buggyTrainingSet = metric.getBuggyTrainingSetToWrite();
        int[] buggyTestingSet = metric.getBuggyTestingSet();

        float totalTrainingSetInstances = float(buggyTrainingSet[0]) + float(buggyTrainingSet[1]);
        float buggyTrainingSetPercentage = (buggyTrainingSet[0] / totalTrainingSetInstances) * 100;

        float totalTestingSetInstances = float(buggyTestingSet[0]) + float(buggyTestingSet[1]);
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
                                + metric.getBalancing() + ","
                                + metric.getFeatureSelection() + ","
                                + metric.getSensitivity() + ","
                                //+ truePositives + ","
                                + eval.numTruePositives(0) + ","
                                //+ falsePositives + ","
                                + eval.numFalsePositives(0) + ","
                                //+ trueNegatives + ","
                                + eval.numTrueNegatives(0) + ","
                                //+ falseNegatives + ","
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
