package controller;

import entities.FileEntity;
import entities.Project;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvController {
    private FileWriter csv;

    public CsvController(Project project){
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

            // currentFile non ha la version inizializzata, devo farlo
            int index = currentFile.getVersionIndex();

            metrics.add(project.getVersionList().get(index).getName());

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

            boolean hasBuggyTrue = false;
            boolean hasBuggyFalse = false;

            for (FileEntity file : project.getFileList()) {
                // controllo perché la release 1.0.2 ottenuta da milestone1 aveva poche entry e tutte true
                    // e questo genera un problema nello svolgimento della milestone2
                if (file.getVersionIndex() == index) {
                    if (file.isBuggy()) {
                        hasBuggyTrue = true;
                    } else {
                        hasBuggyFalse = true;
                    }
                }
                if(hasBuggyFalse && hasBuggyTrue){
                    break;
                }
            }

            try{
                if(currentFile.getLocTouched() != 0 && (hasBuggyTrue && hasBuggyFalse)) {
                    // + controllo che: for currentFile con lo stesso versionIndex
                        // if buggyFalseCount > 0 && buggyTrueCount > 0 ---> addrowtocsv

                    addRowToCSV(metrics, csv);
                }
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
        FileWriter csvResult;
        String filePath = PROJECT_NAME.toLowerCase() + "Files/"+ project.getName() + "Metrics.csv";
        try(csvResult = new FileWriter(filePath)){
            csvResult.append("Version Number," + "File Name," + "LOC Touched," + "Number of Revisions," + "Number of Bug Fixed," + "LOC Added," + "Max LOC Added," + "Chg Set Size," + "Max Chg Set," + "Avg Chg Set," + "Avg LOC Added," + "Buggy" + "\n");
            return csvResult;
        }
    }

}
