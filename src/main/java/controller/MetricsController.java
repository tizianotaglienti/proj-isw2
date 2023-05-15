package controller;

import entities.Bug;
import entities.Project;
import entities.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MetricsController {


    public void getMetrics(){

    }


    public void proportion(Bug bug, Project project){
        if(bug.getIv().getIndex() == 0){
            project.addBugWithoutAV(bug);
            return;
        }
        if(bug.getFv().getIndex() > bug.getOv().getIndex() &&
                bug.getFv().getIndex() > bug.getIv().getIndex() &&
                bug.getOv().getIndex() >= bug.getIv().getIndex()){
            double fvIv = (double)bug.getFv().getIndex() - bug.getIv().getIndex();
            double fvOv = (double)bug.getFv().getIndex() - bug.getOv().getIndex();
            double proportion = fvIv / fvOv;

            if(proportion > 0){
                bug.setProportion(proportion);
                project.addBugWithAV(bug);
            }
        }
    }

    public void estimateProportion(Project project){
        int proportion = getEstimateProportion(project);
        for(int k = 0; k < project.getBugWithoutAV().size(); k++){
            Bug bug = project.getBugWithoutAV().get(k);
            int fvIndex = bug.getFv().getIndex();
            int ovIndex = bug.getOv().getIndex();
            int ivIndex = 0;

            if(proportion >= 0){
                ivIndex = fvIndex - proportion*(fvIndex-ovIndex);
                bug.setIvIndex(ivIndex);
                // cambiare iv, ov, fv in Bug in tutti index (int) con rispettivi get e set
                // e poi cambiare tutti i getV.getindex in getVindex...
                project.getBugWithoutAV().remove(k);
                project.getBugWithoutAV().add(k, bug);
            }
        }
    }

    private int getEstimateProportion(Project project) {
        double sumProportion = 0;
        int numberBugWithAV = project.getBugWithAV().size();

        for(int k = 0; k < numberBugWithAV; k++){
            Bug currentBug = project.getBugWithAV().get(k);
            int currentProportion = (int)currentBug.getProportion();
            sumProportion += currentProportion;
        }
        return (int)(sumProportion/numberBugWithAV);
    }

    // data una resolutionDate, il metodo ritorna l'indice della versione
    public int getFixedVersion(String resolutionDate, Project project){
        int fvIndex = 0;
        LocalDate resolutionLocalDate = LocalDate.parse(resolutionDate);

        for(int k = 0; k < project.getVersionList().size(); k++){
            Version currentVersion = project.getVersionList().get(k);
            LocalDate localDate = currentVersion.getReleaseDate().toLocalDate();
            fvIndex = currentVersion.getIndex();

            if(localDate.isAfter(resolutionLocalDate)){
                return fvIndex - 1;
            }
        }
        return fvIndex;
    }

    // data la creationDate del bug, ritorna indice della versione
    public int getOpeningVersion(String creationDate, Project project){
        int ovIndex = 0;
        LocalDate creationLocalDate = LocalDate.parse(creationDate);

        for(int k = 0; k < project.getVersionList().size(); k++){
            Version currentVersion = project.getVersionList().get(k);
            LocalDate localDate = currentVersion.getReleaseDate().toLocalDate();
            ovIndex = currentVersion.getIndex();

            if(localDate.isAfter(creationLocalDate)) {
                return ovIndex - 1;
            }
        }
        return ovIndex;
    }

    // data la creationDate e la lista di av del bug, il metodo ritorna l'indice della versione
    public int getInjectedVersion(List<String> versionStringList, String creationDate, Project project){
        int ivVersion = 0;

        for(int k = 0; k < project.getVersionList().size(); k++){
            Version currentVersion = project.getVersionList().get(k);
            LocalDate localDate = currentVersion.getReleaseDate().toLocalDate();

            for(int j = 0; j < versionStringList.size() - 1; j++){
                String currentVersionString = versionStringList.get(j);

                if(currentVersionString.equals(currentVersion.getName()) && localDate.isBefore(LocalDate.parse(creationDate))){
                    ivVersion = currentVersion.getIndex();
                    break;
                }
            }
        }
        return ivVersion;
    }

}












