package controller;

import entities.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.json.JSONArray;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComputeVersions {
    public Bug bugBuilder(List<Version> versions, String openingDate, String fixDate, int id, JSONArray avJSON, String key){
        Bug bug = null;
        Version ov = versionComputer(versions, openingDate);
        Version fv = versionComputer(versions, fixDate);
        Version iv = null;
        List<Version> av = affectedVersions(avJSON, versions);

        if(!av.isEmpty()){
            av.sort(Comparator.comparing(Version::getReleaseDate));
            //iv = av.get(av.size()-1);
            iv = av.get(0);
            bug = new Bug(key, fv, ov, iv, id, av);
        }
        else {
            bug = new Bug(key, fv, ov, id, av);
        }
        return bug;
    }

    private List<Version> affectedVersions(JSONArray avJSON, List<Version> versions) {
        List<Version> av = new ArrayList<>();
        int length = avJSON.length();
        boolean released;
        for(int i = 0; i < length; i++){
            released = avJSON.getJSONObject(i).getBoolean("released");
            if(released) {
                String releaseName = avJSON.getJSONObject(i).get("name").toString();
                LocalDateTime releaseDate = null;
                int index = 0;
                for (Version v : versions) {
                    if (v.getName().equals(releaseName)) {
                        releaseDate = v.getReleaseDate();
                        index = v.getIndex();
                    }

                    if (releaseDate != null) {
                        String releaseId = avJSON.getJSONObject(i).get("id").toString();
                        Version newAffectedVersion = new Version(releaseName, releaseDate, releaseId, index);
                        av.add(newAffectedVersion);
                    }
                }
            }
        }
        return av;
    }

    public Version versionComputer(List<Version> versions, String date){
        Version computedVersion = null;
        String newDate = date.substring(0, date.length() - 9); //discard last 9 characters
        LocalDateTime datetime = LocalDateTime.parse(newDate);
        for(Version v : versions) {
            if (datetime.compareTo(v.getReleaseDate()) < 0) {
                computedVersion = v;
                break;
            }
        }

        return computedVersion;

    }

    public List<Bug> discardBugs(List<Bug> bugList){
        List<Bug> discardBugsList = new ArrayList<>();
        for(Bug b : bugList){
            // esistono bug che non hanno FV o OV
            // bug che non rispettano FV > OV e OV > IV
            // e bug che hanno IV = OV = FV
            boolean discard = false;
            if(b.getFv() == null || b.getOv() == null){
                discard = true;
            }
            else if(b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) > 0){
                discard = true;
            }
            else if(b.getIv() != null && b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) == 0 && b.getIv().getReleaseDate().compareTo(b.getOv().getReleaseDate()) == 0){
                discard = true;
            }
            else if(b.getIv() != null && b.getIv().getReleaseDate().compareTo(b.getOv().getReleaseDate()) >= 0){
                discard = true;
            }
            if(!discard){
                discardBugsList.add(b);
            }
        }
        return discardBugsList;
    }

    public int getCommitVersion(LocalDate commitLocalDate, Project project) {
        int index = 0;
        LocalDate currentDate = null;
        for(int k = 0; k < project.getVersionList().size(); k ++){
            Version version = project.getVersionList().get(k);
            index = version.getIndex();
            currentDate = version.getReleaseDate().toLocalDate();

            if(currentDate.isAfter(commitLocalDate)){
                index--;
                break;
            }
        }
        if(index < 0){
            index = 0;
        }
        return index;
    }

    public List<Bug> getBugsForCommit(String fullCommitMessage, Project project) {
        List<Bug> resultList = new ArrayList<>();
        Pattern pattern = null;
        Matcher matcher = null;

        for(int k = 0; k < project.getBugList().size(); k ++){
            Bug currentBug = project.getBugList().get(k);
            // pattern controlla se il commit message contiene "*nomeProgetto*-*bugId*"
            pattern = Pattern.compile("\\b"+ project.getName() + "-" + currentBug.getId() + "\\b", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(fullCommitMessage);
            // controlla se il commit message contiene il bug id e il bug è "not checked"
            if(matcher.find() && !resultList.contains(currentBug.getId())){
                Bug bugToResultList = new Bug();
                bugToResultList.setIv(currentBug.getIv());
                bugToResultList.setFv(currentBug.getFv());
                bugToResultList.setId(currentBug.getId());
                resultList.add(bugToResultList);
            }
        }
        return resultList;
    }

    public void getMetrics(Commit validCommit, DiffEntry diffEntry, DiffFormatter diffFormatter, Project project) throws IOException {
        // se la versione è oltre la metà, ignora il file
        if(validCommit.getBelongingVersion() > project.getHalfVersion()){
            return;
        }
        // aggiorna i valori di version e file
        FileEntity file = removeFile(validCommit.getBelongingVersion(), diffEntry.getNewPath(), project);

        // se è la prima volta si crea una nuova istanza di version e file
        if(file == null){
            file = new FileEntity();
            file.setFileName(diffEntry.getNewPath());
            file.setVersionIndex(validCommit.getBelongingVersion());
        }

        // calcolo le metriche
        int locTouched = 0;
        int locAdded = 0;
        int chgSetSize = 0;

        // iterazione su tutte le modifiche possibili sul file
        for(Edit edit : diffFormatter.toFileHeader(diffEntry).toEditList()){
            if(edit.getType() == Edit.Type.INSERT){
                locAdded += edit.getEndB() - edit.getBeginB();
                locTouched += edit.getEndB() - edit.getBeginB();
            } else if(edit.getType() == Edit.Type.DELETE || edit.getType() == Edit.Type.REPLACE){
                locTouched += edit.getEndA() - edit.getBeginA();
            }
        }
        // file committati insieme
        chgSetSize = validCommit.getFilesChanged().size();

        // vengono aggiornate le metriche
        int locTouchedPreviously = file.getLocTouched();
        file.setLocTouched(locTouchedPreviously + locTouched);

        int numberRevisionsPreviously = file.getNumberRevisions();
        file.setNumberRevisions(numberRevisionsPreviously + 1);

        if(!validCommit.getBugList().isEmpty()){
            int numberBugFixPreviously = file.getNumberBugFix();
            file.setNumberBugFix(numberBugFixPreviously + validCommit.getBugList().size());
            file.setBuggy(true);
        }

        int locAddedPreviously = file.getLocAdded();
        file.setLocAdded(locAddedPreviously + locAdded);

        if(locAdded > file.getMaxLocAdded()){
            file.setMaxLocAdded(locAdded);
        }

        int chgSetSizePreviously = file.getChgSetSize();
        file.setChgSetSize(chgSetSizePreviously + chgSetSize);

        if(chgSetSize > file.getMaxChgSet()){
            file.setMaxChgSet(chgSetSize);
        }

        file.setAvgLocAdded((float)(locAddedPreviously + locAdded) / (float)(numberRevisionsPreviously + 1));
        file.setAvgChgSet((float)(chgSetSizePreviously + chgSetSize) / (float)(numberRevisionsPreviously + 1));

        project.addFileToList(file);


    }

    public Project setBuggy(Commit validCommit, DiffEntry diffEntry, Project project) {
        if(validCommit.getBugList().isEmpty()){
            return project;
        }
        for(int k = 0; k < validCommit.getBugList().size(); k ++){
            int iv = validCommit.getBugList().get(k).getIv().getIndex();
            int fv = validCommit.getBugList().get(k).getFv().getIndex();

            for(int v = iv; v < fv; v ++){
                FileEntity file = removeFile(v, diffEntry.getNewPath(), project);
                if(file == null){
                    file = new FileEntity();
                    file.setVersionIndex(v);
                    file.setFileName(diffEntry.getNewPath());
                }

                file.setBuggy(true);
                project.addFileToList(file);
            }
        }
        return project;
    }

    private FileEntity removeFile(int version, String fileName, Project project) {
        for(int k = 0; k < project.getFileList().size(); k ++){
            FileEntity file = project.getFileList().get(k);

            if(file.getVersionIndex() == version && file.getFileName().equals(fileName)){
                project.getFileList().remove(k);
                return file;
            }
        }
        return null;
    }
}