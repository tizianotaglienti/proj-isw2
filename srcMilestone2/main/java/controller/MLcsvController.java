package controller;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;

public class MLcsvController {
    public FileWriter createOutputCSV(String filePath) throws IOException {
        FileWriter outputCSV = new FileWriter(filePath + "_OUTPUT.csv");
        outputCSV.append(""); // AGGIUNGERE COSE DA SCRIVERE NEL .CSV

        return outputCSV;
    }

    public void split(String csvFileName, List<String> csvFileList, String firstRelease, int featureNumber) throws IOException {
        CSVReader reader = null;

        try{
            reader = new CSVReader(new FileReader(csvFileName));

            String[] nextLine;
            int currentRow = 1;
            StringBuilder attributeList = new StringBuilder();
            int version = 1;

            FileWriter csv = new FileWriter(csvFileList.get(version - 1));
            String currentVersion = firstRelease;

            // legge una linea alla volta
            while((nextLine = reader.readNext()) != null){
                for(String token : nextLine){
                    if(currentRow >= featureNumber + 2){
                        if(token.equals(currentVersion)){
                            currentRow = addAttribute(currentRow, featureNumber, csv, attributeList);
                        } else {
                            version++;
                            csv.close();

                            csv = new FileWriter(csvFileList.get(version - 1));
                            csv.append(attributeList + "\n");
                            currentVersion = token;
                        }
                        addRow(nextLine, csv);
                        break;
                    } else {
                        attributeList.append(token);
                        currentRow = addFirstRow(currentRow, featureNumber, attributeList);
                    }
                }

            }
            csv.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int addFirstRow(int currentRow, int featureNumber, StringBuilder attributeList) {
        if(currentRow != featureNumber + 1){
            attributeList.append(",");
        }
        currentRow++;
        return currentRow;
    }

    private void addRow(String[] nextLine, FileWriter csv) throws IOException {
        StringBuilder stringToAppendToCsv = new StringBuilder();
        int c = 0;
        for(String token : nextLine){
            if(c != 0){
                stringToAppendToCsv.append(",");
            }
            stringToAppendToCsv.append(token);
            c++;
        }
        // append della stringa al file csv
        csv.append(stringToAppendToCsv + "\n");
    }

    private int addAttribute(int currentRow, int featureNumber, FileWriter csv, StringBuilder attributeList) {
        if(currentRow == featureNumber + 2){
            try{
                csv.append(attributeList + "\n");
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        currentRow++;
        return currentRow;
    }
}
