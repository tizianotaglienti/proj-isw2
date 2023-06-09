package controllertwo;

import entitiestwo.VariableModel;
import weka.filters.Filter;
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

    private Instances trainingSet;
    private Instances testingSet;

    /**
     * Avvia il processo di feature selection in base all'opzione selezionata.
     *
     * @param trainingSet       Dati di addestramento.
     * @param testingSet        Dati di test.
     * @param indexForFsSwitch  Indice per l'opzione di feature selection scelta.
     * @return Lista contenente i nuovi dataset di addestramento e test con le feature selezionate.
     */

    public List<Instances> startFeatureSelection(Instances trainingSet, Instances testingSet, int indexForFsSwitch){
        this.trainingSet = trainingSet;
        this.testingSet = testingSet;

        if(indexForFsSwitch == 0) return noSelection();
        if(indexForFsSwitch == 1) return bestFirstSelection();

        return new ArrayList<>();
    }

    /**
     * No selection: restituisce i dataset di addestramento e test originali senza alcuna selezione.
     *
     * @return Lista contenente i dataset di addestramento e test originali.
     */

    private ArrayList<Instances> noSelection() {
        VariableModel.setFeatureSelection("No selection");

        ArrayList<Instances> newDataset = new ArrayList<>();
        newDataset.add(0, trainingSet);
        newDataset.add(1, testingSet);

        return newDataset;
    }

    /**
     * Best First selection: si utilizzano l'evaluator CfsSubsetEval e l'algoritmo di ricerca BestFirst.
     *
     * @return Lista contenente i nuovi dataset di addestramento e test con le feature selezionate.
     */

    private ArrayList<Instances> bestFirstSelection(){
        VariableModel.setFeatureSelection("Best first selection");

        AttributeSelection attributeSelectionFilter = new AttributeSelection();

        CfsSubsetEval evaluator = new CfsSubsetEval();
        BestFirst searchAlgorithm = new BestFirst();

        // Imposta il filtro per utilizzare l'evaluator e il search algorithm
        attributeSelectionFilter.setEvaluator(evaluator);
        attributeSelectionFilter.setSearch(searchAlgorithm);

        try{
            attributeSelectionFilter.setInputFormat(this.trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances trainingSetWithFeatureSelection = new Instances(trainingSet);
        try {
            trainingSetWithFeatureSelection = Filter.useFilter(this.trainingSet, attributeSelectionFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances testingSetWithFeatureSelection = new Instances(testingSet);
        try {
            testingSetWithFeatureSelection = Filter.useFilter(testingSet, attributeSelectionFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int numAttributes = trainingSetWithFeatureSelection.numAttributes();

        trainingSetWithFeatureSelection.setClassIndex(numAttributes - 1);
        testingSetWithFeatureSelection.setClassIndex(numAttributes - 1);

        ArrayList<Instances> newDataset = new ArrayList<>();
        newDataset.add(0, trainingSetWithFeatureSelection);
        newDataset.add(1, testingSetWithFeatureSelection);

        return newDataset;
    }


}
