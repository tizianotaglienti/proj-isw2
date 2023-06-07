package controller2;

import entities2.VariableModel;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

/**
 * TECNICHE DI FEATURE SELECTION:
 *  No selection / best first - (come feature selection)
 **/

public class FeatureSelectionController {

    private VariableModel metric;
    private Instances trainingSet;
    private Instances testingSet;

    public List<Instances> startFeatureSelection(Instances trainingSet, Instances testingSet, int indexForFsSwitch, VariableModel metric){
        this.metric = metric;
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;

        if(indexForFsSwitch == 0) return noSelection();
        if(indexForFsSwitch == 1) return bestFirstSelection();

        return new ArrayList<>();
    }

    private ArrayList<Instances> noSelection() {
        this.metric.setFeatureSelection("No selection");

        ArrayList<Instances> newDataset = new ArrayList<>();
        newDataset.add(0, trainingSet);
        //newDataset.add(0, this.trainingSet);
        newDataset.add(1, testingSet);
        //newDataset.add(1, this.testingSet);

        return newDataset;
    }

    private ArrayList<Instances> bestFirstSelection(){
        this.metric.setFeatureSelection("Best first selection");

        AttributeSelection attributeSelectionFilter = new AttributeSelection();

        CfsSubsetEval evaluator = new CfsSubsetEval();
        BestFirst searchAlgorithm = new BestFirst();

        // Imposta il filtro per utilizzare l'evaluator e il search algorithm
        attributeSelectionFilter.setEvaluator(evaluator);
        attributeSelectionFilter.setSearch(searchAlgorithm);

        try{
            attributeSelectionFilter.setInputFormat(this.trainingSet);
            //attributeSelectionFilter.setInputFormat(this.trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances trainingSetWithFeatureSelection = null;
        try {
            //trainingSetWithFeatureSelection = AttributeSelection.useFilter(this.trainingSet, attributeSelectionFilter);
            trainingSetWithFeatureSelection = AttributeSelection.useFilter(this.trainingSet, attributeSelectionFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances testingSetWithFeatureSelection = null;
        try {
            //testingSetWithFeatureSelection = AttributeSelection.useFilter(this.testingSet, attributeSelectionFilter);
            testingSetWithFeatureSelection = AttributeSelection.useFilter(testingSet, attributeSelectionFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int numAttributes = trainingSetWithFeatureSelection.numAttributes();

        trainingSetWithFeatureSelection.setClassIndex(numAttributes - 1);
        //System.out.println(trainingSetWithFeatureSelection.classIndex());
        //System.out.println(trainingSetWithFeatureSelection.attribute(4));
        testingSetWithFeatureSelection.setClassIndex(numAttributes - 1);
        //System.out.println(testingSetWithFeatureSelection.classIndex());
        //System.out.println(testingSetWithFeatureSelection.attribute(4));

        ArrayList<Instances> newDataset = new ArrayList<>();
        newDataset.add(0, trainingSetWithFeatureSelection);
        newDataset.add(1, testingSetWithFeatureSelection);

        return newDataset;
    }


}
