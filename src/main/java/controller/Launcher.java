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
        MetricsController prop = new MetricsController();

        System.out.println(bugList);
        Project project = new Project();

        for (Version v : versionList) {
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
        for (Bug b : bugList){

            //System.out.println(b.getOvIndex()); // 0
            // System.out.println("CIAO: OV");
            // System.out.println(b.getOv().getIndex()); // 11
            //System.out.println(b.getFvIndex()); // 0
            // System.out.println("CIAO: FV");
            // System.out.println(b.getFv().getIndex()); // 11
            // System.out.println("\n");


            // problema: tutte le OV e FV sono o la prima o l'ultima versione (11 bk, 29 storm).


            //System.out.println(b.getResolutionDate());
            //System.out.println(b);

            System.out.println("key " + b.getKey() + " OV: " + b.getOv().getIndex() + " FV: " + b.getFv().getIndex());
            if(b.getIv() != null){
                System.out.println(" IV: " + b.getIv().getIndex());
            }



            // se queste due righe funzionano posso togliere l'if precedente.
            // per vedere se funzionano, provo a printare ora le versioni con iv null

            // errore perché questo estimateproportion non crea la iv che dovrebbe creare.
                    // in realtà aggiunge solo L'ivIndex (cosa che non mi piace)
                    // --> in estimateProportion vorrei creare una NUOVA IV con certe caratteristiche!!!!!
             // dovrebbe funzionare
// A QUESTO PUNTO, NELLA BUGLIST TUTTI I BUG AVRANNO LA IV FINALMENTE
            // PROBLEMA: ANCHE QUELLI CHE CE L'HANNO SUBISCONO UN CAMBIAMENTO! NO!
            // proviamo così:
            else if(b.getIv() == null){
                prop.proportion(b, project);
                prop.estimateProportion(project, versionList);
            }
        }
        // ok dovrebbe funzionare: ora che tutti i bug hanno le tre v
            // devo vedere se la classe è buggy confrontando le v

        // for(File fileBuggyOrNot : FileList) ... oppure iterare sulle Entry




        // il proportion serve a trovare l'IV per quelle (molteplici) classi con iv = null.
        // IDEA: iterando i bug che hanno iv == null devo creare a ciascuno un iv
            // poi un metodo (o anche codice) per fare un "isBuggy"


        // poi devo calcolare metriche
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
