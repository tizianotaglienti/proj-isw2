package main.java.controller2;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class CsvToArff {

    public void csvToArffConverter(String[] args) {
        CSVLoader loader = new CSVLoader();
        try{
            loader.setSource(new File(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Instances data = null;
        try{
            data = loader.getDataSet();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Remove removeFilter = new Remove();

        int[] indices = {1};
        removeFilter.setAttributeIndicesArray(indices);
        removeFilter.setInvertSelection(false);

        try{
            removeFilter.setInputFormat(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            data = Filter.useFilter(data, removeFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ARFF saver
        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        try {
            saver.setFile(new File(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            saver.setDestination(new File(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            saver.writeBatch();
        } catch (IOException e) {
            e.printStackTrace();
        }


        List<String> lines;
        try {
            lines = Files.readAllLines(new File(args[1]).toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String line : lines) {
            if (line.contains("@attribute Buggy {false,true}") || line.contains("@attribute Buggy {true,false,fals}")){
                lines.set(lines.indexOf(line), "@attribute Buggy {true,false}");
            }
        }

        try {
            Files.write(new File(args[1]).toPath(), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
