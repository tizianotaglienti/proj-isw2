package controller;

import entities.FileEntity;
import entities.Project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class csvController {
    private FileWriter csv;
    //private Project project;

    public csvController(Project project){

        try {
            csv = initializeCSVResult(project);
        } catch (IOException e){
            e.printStackTrace();
        }
        createCSV(project);
    }

    public void createCSV(Project project) {
        for(int k = project.getFileList().size() - 1; k >= 0; k--){
            List<String> metrics = new ArrayList<>();
            FileEntity currentFile = project.getFileList().get(k);
            //int index = currentFile.getVersionIndex();

            // currentFile non ha la version inizializzata, devo farlo
            int index = currentFile.getVersionIndex();

            metrics.add(String.valueOf(index));
            metrics.add(currentFile.getFileName());
            metrics.add(Integer.toString(currentFile.getLocTouched()));
            metrics.add(Integer.toString(currentFile.getNumberRevisions()));
            metrics.add(Integer.toString(currentFile.getNumberBugFix()));
            metrics.add(Integer.toString(currentFile.getLocAdded()));
            metrics.add(Integer.toString(currentFile.getMaxLocAdded()));
            metrics.add(Integer.toString(currentFile.getChgSetSize()));
            metrics.add(Integer.toString(currentFile.getMaxChgSet()));
            metrics.add(Float.toString(currentFile.getAvgChgSet()));
            metrics.add(Float.toString(currentFile.getAvgLocAdded()));
            metrics.add(Boolean.toString(currentFile.isBuggy()));

            try{
                addRowToCSV(metrics, csv);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static void addRowToCSV(List<String> nextLine, FileWriter csv) throws IOException {
        StringBuilder stringToAppend = new StringBuilder();
        int c = 0;
        for(String token : nextLine){
            if(c != 0) stringToAppend.append(",");
            stringToAppend.append(token);
            c++;
        }
        // add to file
        csv.append(stringToAppend + "\n");
    }

    public FileWriter initializeCSVResult(Project project) throws IOException {
        FileWriter csvResult = new FileWriter(project.getName() + "Metrics.csv");

        csvResult.append("Version Number," + "File Name," + "LOC Touched," + "Number of Revisions," + "Number of Bug Fixed," + "LOC Added," + "Max LOC Added," + "Chg Set Size," + "Max Chg Set," + "Avg Chg Set," + "Avg LOC Added," + "Buggy" + "\n");
        return csvResult;
    }

}
