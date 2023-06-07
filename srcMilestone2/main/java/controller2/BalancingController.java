package controller2;

import entities2.VariableModel;

import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
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

    private static VariableModel metric;
    private static Instances trainingSet;

    public Instances startBalancing(Instances training, int indexForBalancingSwitch, VariableModel metric) {
        this.metric = metric;
        this.trainingSet = training;

        if (indexForBalancingSwitch == 0) return noSampling();
        if (indexForBalancingSwitch == 1) return overSampling();
        if (indexForBalancingSwitch == 2) return underSampling();
        if (indexForBalancingSwitch == 3) return smote();

        return null;
    }


    private Instances noSampling() {
        metric.setBalancing("No sampling");
        return trainingSet;
    }

    private Instances overSampling() {
        metric.setBalancing("Oversampling");
        float majorityClassPercentage = metric.getMajorityClassPercentage();
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

    private Instances underSampling() {
        metric.setBalancing("Undersampling");
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

    private static Instances smote() {
        metric.setBalancing("Smote");

        SMOTE smoteObject = new SMOTE();

        //FilteredClassifier classifierWithSmote = new FilteredClassifier();
        try{
            smoteObject.setInputFormat(trainingSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //classifierWithSmote.setFilter(smoteObject);

        Instances smoteTrainingSet = null;
        //System.out.println(trainingSet.numInstances());
        try {
            smoteTrainingSet =  Filter.useFilter(trainingSet, smoteObject);
            return smoteTrainingSet;
            //smoteTrainingSet = Filter.useFilter(trainingSet, classifierWithSmote.getFilter()); // cannot use 0 neighbors!
            // THIS IS THE PROBLEM!!!
            //return smoteTrainingSet;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return smoteTrainingSet;
    }
}
