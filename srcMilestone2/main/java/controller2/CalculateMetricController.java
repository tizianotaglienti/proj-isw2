package controller2;

import entities2.ProjectToAnalyze;

import entities2.VariableModel;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;

import java.io.FileWriter;

public class CalculateMetricController {

    public void metricCalculator(Evaluation eval, ProjectToAnalyze project, int releaseNumber, String classifier, VariableModel metric, FileWriter csvOutput){
        double truePositives = eval.numTruePositives(0);
        double trueNegatives = eval.numTrueNegatives(0);
        double falsePositives = eval.numFalsePositives(0);
        double falseNegatives = eval.numFalseNegatives(0);

        // calcolo percentuale di bugginess del training set e del testing set
        // ...
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
