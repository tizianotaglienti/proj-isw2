package controller2;

import entities2.VariableModel;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.core.Instances;

/**
 * TECNICHE DI SENSITIVE SELECTION:
 *  No cost sensitive / sensitive threshold / sensitive learning (CFN = 10*CFP)
 **/

public class SensitiveSelectionController {
    private VariableModel metric;
    private Classifier classifier;
    private Instances trainingSet;
    private Instances testingSet;

    public Evaluation startSensitiveSelection(Instances trainingSet, Instances testingSet, int indexForSensitiveSelection, VariableModel metric, Classifier classifier){
        this.metric = metric;
        this.classifier = classifier;
        this.testingSet = testingSet;
        this.trainingSet = trainingSet;

        if(indexForSensitiveSelection == 0) return noCostSensitive();
        if(indexForSensitiveSelection == 1) return sensitiveThreshold();
        if(indexForSensitiveSelection == 2) return sensitiveLearning();

        return null;
    }

    private Evaluation noCostSensitive(){
        metric.setSensitivity("No cost sensitive");

        try{
            classifier.buildClassifier(trainingSet);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        Evaluation evaluation = null;
        try{
            evaluation = new Evaluation(testingSet);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        try{
            evaluation.evaluateModel(classifier, testingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return evaluation;
    }

    private Evaluation sensitiveThreshold(){
        metric.setSensitivity("Sensitive threshold");

        CostMatrix costMatrix = this.calculateCostMatrix();
        CostSensitiveClassifier costSensitiveClassifier = createCostSensitiveClassifier(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(true);

        return applyEvaluation(costSensitiveClassifier, costMatrix);
    }

    private Evaluation sensitiveLearning() {
        metric.setSensitivity("Sensitive learning");

        CostMatrix costMatrix = this.calculateCostMatrix();
        CostSensitiveClassifier costSensitiveClassifier = createCostSensitiveClassifier(costMatrix);
        costSensitiveClassifier.setMinimizeExpectedCost(false);

        return applyEvaluation(costSensitiveClassifier, costMatrix);
    }

    private CostMatrix calculateCostMatrix() {
        CostMatrix costMatrix = new CostMatrix(2);

        double falsePositiveCost = 1;
        double falseNegativeCost = 10 * falsePositiveCost;

        costMatrix.setCell(0, 0, 0.0);
        costMatrix.setCell(0, 1, falseNegativeCost);
        costMatrix.setCell(1, 0, falsePositiveCost);
        costMatrix.setCell(1, 1, 0.0);

        return costMatrix;
    }

    private Evaluation applyEvaluation(CostSensitiveClassifier costSensitiveClassifier, CostMatrix costMatrix) {
        try{
            costSensitiveClassifier.buildClassifier(trainingSet);
        } catch (Exception e2) {
            e2.printStackTrace();
        }

        Evaluation evaluation = null;
        try{
            evaluation = new Evaluation(trainingSet, costMatrix);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        try{
            evaluation.evaluateModel(costSensitiveClassifier, testingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return evaluation;
    }

    private CostSensitiveClassifier createCostSensitiveClassifier(CostMatrix costMatrix) {
        CostSensitiveClassifier costSensitiveClassifier = new CostSensitiveClassifier();
        costSensitiveClassifier.setClassifier(classifier);
        costSensitiveClassifier.setCostMatrix(costMatrix);

        return costSensitiveClassifier;
    }


}
