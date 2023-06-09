package controllertwo;

import entitiestwo.VariableModel;

import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;

/**
 * TECNICHE DI BALANCING:
 *  No sampling / oversampling / undersampling / SMOTE
 **/

public class BalancingController {

    private Instances trainingSet;


    /**
     * Avvia il processo di balancing dei dati di addestramento in base all'opzione selezionata.
     *
     * @param training                  Dati di addestramento.
     * @param indexForBalancingSwitch   Indice per l'opzione di bilanciamento selezionata.
     * @return Dati di addestramento bilanciati.
     */

    public Instances startBalancing(Instances training, int indexForBalancingSwitch) {
        this.trainingSet = training;

        if (indexForBalancingSwitch == 0) return noSampling();
        if (indexForBalancingSwitch == 1) return overSampling();
        if (indexForBalancingSwitch == 2) return underSampling();
        if (indexForBalancingSwitch == 3) return smote();

        return new Instances(training, 0); // restituisce una collezione vuota (smell)
    }

    /**
     * No sampling: restituisce i dati di addestramento originali senza alcun bilanciamento.
     *
     * @return Dati di addestramento originali.
     */

    private Instances noSampling() {
        VariableModel.setBalancing("No sampling");
        return trainingSet;
    }

    /**
     * Oversampling: utilizza il filtro di campionamento Resample per aumentare
     * il numero di istanze della classe minoritaria.
     *
     * @return Dati di addestramento con oversampling.
     */

    private Instances overSampling() {
        VariableModel.setBalancing("Oversampling");
        float majorityClassPercentage = VariableModel.getMajorityClassPercentage();
        String[] oversamplingOptions = new String[]{"-B", "1.0", "-Z", String.valueOf(2 * majorityClassPercentage)};

        Resample oversamplingFilter = new Resample();
        try {
            oversamplingFilter.setOptions(oversamplingOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            oversamplingFilter.setInputFormat(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FilteredClassifier classifierWithOversampling = new FilteredClassifier();
        classifierWithOversampling.setFilter(oversamplingFilter);

        Instances oversampledTrainingSet = null;
        try {
            return Filter.useFilter(trainingSet, oversamplingFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return oversampledTrainingSet;
    }

    /**
     * Undersampling: utilizza il filtro SpreadSubsample per ridurre il numero di
     * istanze della classe maggioritaria.
     *
     * @return Dati di addestramento con undersampling.
     */

    private Instances underSampling() {
        VariableModel.setBalancing("Undersampling");
        String[] undersamplingOptions = new String[]{"-M", "1.0"};

        SpreadSubsample undersamplingFilter = new SpreadSubsample();

        try {
            undersamplingFilter.setOptions(undersamplingOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            undersamplingFilter.setInputFormat(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FilteredClassifier classifierWithUndersampling = new FilteredClassifier();
        classifierWithUndersampling.setFilter(undersamplingFilter);

        Instances undersampledTrainingSet = null;
        try {
            return Filter.useFilter(trainingSet, undersamplingFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return undersampledTrainingSet;
    }

    /**
     * Applica la tecnica di SMOTE (Synthetic Minority Over-sampling Technique) per generare nuove istanze
     * sintetiche della classe minoritaria.
     *
     * @return Dati di addestramento con SMOTE.
     */

    private Instances smote() {
        VariableModel.setBalancing("Smote");

        SMOTE smoteObject = new SMOTE();

        try{
            smoteObject.setInputFormat(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instances smoteTrainingSet = null;
        try {
            smoteTrainingSet =  Filter.useFilter(trainingSet, smoteObject);
            return smoteTrainingSet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return smoteTrainingSet;
    }
}
