package controller;

import entities.Bug;
import entities.File;
import entities.Project;
import entities.Version;
import controller.JiraHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
    private static final String PROJECT_NAME = "BOOKKEEPER";

    public static List<File> halfData(List<Version> versions, List<File> files){
        versions = versions.subList(0, versions.size()/2);
        List<File> entries = new ArrayList<>();
        for(Version v : versions){
            for(File f : files){
                if(v.getName().equals(f.getVersion())){
                    entries.add(f);
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) throws IOException {
        JiraHelper helper = new JiraHelper(PROJECT_NAME);
        List<Version> versionList = helper.getAllVersions();
        //System.out.println(versionList);
        List<Bug> bugList = helper.getBugs(versionList);

        System.out.println(bugList);
        Project project = new Project();

        for (Version v : versionList) {
            //    System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
        for (Bug b : bugList){

            //System.out.println(b.getOvIndex()); // 0
            System.out.println("CIAO: OV");
            System.out.println(b.getOv().getIndex()); // 11
            //System.out.println(b.getFvIndex()); // 0
            System.out.println("\n");
            System.out.println("CIAO: FV");
            System.out.println(b.getFv().getIndex()); // 11

            // problema: tutte le OV e FV sono o la prima o l'ultima versione (11 bk, 29 storm).


            //System.out.println(b.getResolutionDate());
            //System.out.println(b);

            //     System.out.println("key " + b.getKey() + " OV: " + b.getOv().getIndex() + " FV: " + b.getFv().getIndex());
            if(b.getIv() != null){
                //       System.out.println(" IV: " + b.getIv().getIndex());
            }

        }
        MetricsController prop = new MetricsController();

        // il proportion serve a trovare l'IV per quelle (molteplici) classi con iv = null.

        // idea: calculateMetrics(project, bugList)
            // generateCSV(metrics)

        // prima devo tirare fuori un yes o no sulla buggyness, come?
        // se una classe ha iv, ov...
        // stabilisco buggy le classi tra iv e fv
        // e non buggy le classi preiv e postfv, in che modo?
            // difficile da rispondere...
        // sicuramente devo fare dei confronti sulle date


        //[.........]
        // qua dovrei calcolare tutte le metriche

        int releaseNumber = project.getVersionList().size();
        project.setHalfVersion(releaseNumber/2);

        System.out.println(project.getVersion());

        // prima di questo devo dimezzare il project
        csvController csvCtrl = new csvController(project);
        csvCtrl.createCSV();

    }
}
