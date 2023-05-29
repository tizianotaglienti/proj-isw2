package controller;

import entities.Bug;
import entities.Project;
import entities.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class MetricsController {


    public void getMetrics(){

    }


    public void proportion(Bug bug, Project project){
        bug.setOvIndex(getOpeningVersion(bug.getOv().getReleaseDate(), project));
        bug.setIvIndex(getInjectedVersion(Collections.singletonList(bug.getAv().toString()), bug.getCreationDate(), project));
        bug.setFvIndex(getFixedVersion(bug.getFv().getReleaseDate(), project));

        if(bug.getIvIndex() == 0){
            project.addBugWithoutAV(bug);
            return;
        }
        if(bug.getFvIndex() > bug.getOvIndex() &&
                bug.getFvIndex() > bug.getIvIndex() &&
                bug.getOvIndex() >= bug.getIvIndex()){
            double fvIv = (double)bug.getFvIndex() - bug.getIvIndex();
            double fvOv = (double)bug.getFvIndex() - bug.getOvIndex();
            double proportion = fvIv / fvOv;

            if(proportion > 0){
                bug.setProportion(proportion);
                project.addBugWithAV(bug);
            }
        }
    }

    public void estimateProportion(Project project, List<Version> versionList){
        int proportion = getEstimateProportion(project);
        for(int k = 0; k < project.getBugWithoutAV().size(); k++){
            Bug bug = project.getBugWithoutAV().get(k);
            int fvIndex = bug.getFv().getIndex();
            int ovIndex = bug.getOv().getIndex();
            int ivIndex = 0;

            if(proportion >= 0){
                ivIndex = fvIndex - proportion*(fvIndex-ovIndex);
                bug.setIvIndex(ivIndex);

                bug.setIv(calculateIv(fvIndex, ovIndex, proportion, versionList));

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
    public int getFixedVersion(LocalDateTime resolutionDate, Project project){
        int fvIndex = 0;
        //LocalDate resolutionLocalDate = LocalDate.parse(resolutionDate);

        for(int k = 0; k < project.getVersionList().size(); k++){
            Version currentVersion = project.getVersionList().get(k);
            LocalDateTime localDateTime = currentVersion.getReleaseDate();
            fvIndex = currentVersion.getIndex();

            if(localDateTime.isAfter(resolutionDate)){
                return fvIndex - 1;
            }
        }
        return fvIndex;
    }

    // data la creationDate del bug, ritorna indice della versione
    public int getOpeningVersion(LocalDateTime creationDate, Project project){
        int ovIndex = 0;
        //LocalDateTime creationLocalDate = LocalDateTime.parse(creationDate);
        // storm dà errore nullpointer per una versione senza creation date e si interrompe qua
        // mannaggia anche bookkeeper

        for(int k = 0; k < project.getVersionList().size(); k++){
            Version currentVersion = project.getVersionList().get(k);
            LocalDateTime localDateTime = currentVersion.getReleaseDate();
            ovIndex = currentVersion.getIndex();

            if(localDateTime.isAfter(creationDate)) {
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

    public Version calculateIv(int fvIndex, int ovIndex, float proportion, List<Version> versionList){
        Version nullIv = null;
        int denominator = fvIndex - ovIndex;
        if(denominator == 0){
            denominator = 1;
        }
        int ivResult = fvIndex - Math.round(denominator * proportion);
        for(Version ivToBeSet : versionList){
            if(ivToBeSet.getIndex() == ivResult){
                nullIv = ivToBeSet;
            }
        }

        if(ivResult < 1){
            nullIv = versionList.get(0);
        }
        return nullIv;
    }
}












