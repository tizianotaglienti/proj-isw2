package controller;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;

import java.io.File;
import java.io.IOException;

public class csvToArff {
    public void csvToArff(String pathToCsv, String projectName) throws IOException {
        // load CSV
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(pathToCsv));
        Instances data = loader.getDataSet();

        // save ARFF
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);//set the dataset we want to convert
        //and save as ARFF
        saver.setFile(new File("C:\\Users\\tagli\\Desktop\\proj-isw2\\" + projectName + ".arff"));
        saver.writeBatch();
    }
}
