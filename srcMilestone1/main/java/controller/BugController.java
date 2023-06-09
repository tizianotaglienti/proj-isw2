package controller;

import entities.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.json.JSONArray;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BugController {

    private static final Logger logger = LogManager.getLogger(BugController.class);


    /**
     * Costruisce un oggetto Bug a partire dai dati forniti.
     *
     * @param versions      Lista delle versioni del progetto
     * @param openingDate   Data di apertura del bug
     * @param fixDate       Data di risoluzione del bug
     * @param id            ID del bug
     * @param avJSON        Array JSON delle versioni interessate dal bug
     * @param key           Chiave del bug
     * @return Oggetto Bug creato
     */

    public Bug bugBuilder(List<Version> versions, String openingDate, String fixDate, int id, JSONArray avJSON, String key){
        Bug bug = null;
        Version ov = versionComputer(versions, openingDate);
        Version fv = versionComputer(versions, fixDate);
        Version iv = null;
        List<Version> av = affectedVersions(avJSON, versions);

        if(!av.isEmpty()){
            av.sort(Comparator.comparing(Version::getReleaseDate));
            iv = av.get(0);
            bug = new Bug(key, fv, ov, iv, id, av);
        }
        else {
            bug = new Bug(key, fv, ov, id, av);
        }
        return bug;
    }

    /**
     * Restituisce la lista delle versioni interessate dal bug.
     *
     * @param avJSON    Array JSON delle versioni interessate dal bug
     * @param versions  Lista delle versioni del progetto
     * @return Lista delle versioni interessate dal bug
     */

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

    /**
     * Calcola la versione a cui appartiene la data fornita.
     *
     * @param versions  Lista delle versioni del progetto
     * @param date      Data da confrontare
     * @return Versione a cui appartiene la data
     */

    public Version versionComputer(List<Version> versions, String date){
        Version computedVersion = null;
        String newDate = date.substring(0, date.length() - 9); // scarto gli ultimi nove caratteri di date
        LocalDateTime datetime = LocalDateTime.parse(newDate);
        for(Version v : versions) {
            if (datetime.compareTo(v.getReleaseDate()) < 0) {
                computedVersion = v;
                break;
            }
        }
        return computedVersion;
    }

    /**
     * Filtra i bug non validi in base a determinate condizioni.
     *
     * @param bugList      Lista dei bug
     * @return Lista dei bug validi
     */

    public List<Bug> discardBugs(List<Bug> bugList){
        List<Bug> discardBugsList = new ArrayList<>();
        for(Bug b : bugList){
            // esistono bug che non hanno FV o OV
            // bug che non rispettano FV > OV e OV > IV
            // e bug che hanno IV = OV = FV
            boolean discard = false;
            if (b.getFv() == null || b.getOv() == null
                    || b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) > 0
                    || (b.getIv() != null && b.getIv().getReleaseDate().compareTo(b.getOv().getReleaseDate()) >= 0)
                    || (b.getIv() != null && b.getOv().getReleaseDate().compareTo(b.getFv().getReleaseDate()) == 0 && b.getIv().getReleaseDate().compareTo(b.getOv().getReleaseDate()) == 0)) {
                discard = true;
            }
            if(!discard){
                discardBugsList.add(b);
            }
        }
        return discardBugsList;
    }

    /**
     * Calcola l'indice della versione di commit in base alla data locale e alla lista delle versioni del progetto.
     *
     * @param commitLocalDate    Data di commit
     * @param project            Progetto
     * @return Indice della versione di commit
     */

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

    /**
     * Recupera la lista dei bug associati a un messaggio di commit e a un progetto.
     *
     * @param fullCommitMessage Messaggio di commit
     * @param project           Progetto
     * @return Lista dei bug associati
     */

    public List<Bug> getBugsForCommit(String fullCommitMessage, Project project) {
        List<Bug> resultList = new ArrayList<>();
        Pattern pattern = null;
        Matcher matcher = null;

        for(int k = 0; k < project.getBugList().size(); k ++){
            // problema: getBugList restituisce niente perché è vuota
            Bug currentBug = project.getBugList().get(k);
            // pattern controlla se il commit message contiene "*nomeProgetto*-*bugId*"

                    // id di currentbug è 0 ... controllare se è normale
            pattern = Pattern.compile("\\b" + project.getName() + "-" + currentBug.getId() + "\\b", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(fullCommitMessage);
            // controlla se il commit message contiene il bug id e il bug è "not checked"
            if(matcher.find() && !resultList.contains(currentBug.getId())){

                // questo if potrebbe essere causa dell'errore
                Bug bugToResultList = new Bug();
                bugToResultList.setIv(currentBug.getIv());
                bugToResultList.setFv(currentBug.getFv());
                bugToResultList.setId(currentBug.getId());
                resultList.add(bugToResultList);
            }
        }
        return resultList;
    }

    /**
     * Calcola le metriche per un commit e aggiorna le metriche dei file corrispondenti nel progetto.
     *
     * @param validCommit    Commit
     * @param diffEntry
     * @param diffFormatter
     * @param project        Progetto
     * @throws IOException Eccezione di I/O
     */

    public void getMetrics(Commit validCommit, DiffEntry diffEntry, DiffFormatter diffFormatter, Project project) throws IOException {
        // se la versione è oltre la metà, ignoro il file
        if(validCommit.getBelongingVersion() > project.getHalfVersion()){
            return;
        }
        // aggiorno i valori di version e file
        FileEntity file = removeFile(validCommit.getBelongingVersion(), diffEntry.getNewPath(), project);

        // se è la prima volta, creo una nuova istanza di version e file
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

        // aggiorno le metriche
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

    /**
     * Segna i file come "buggy" in base ai bug associati a un commit e aggiorna la lista dei file del progetto.
     *
     * @param validCommit    Commit
     * @param diffEntry
     * @param project        Progetto
     */

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

    /**
     * Rimuove il file corrispondente alla versione e al nome del file forniti dal progetto.
     *
     * @param version   Indice della versione
     * @param fileName  Nome del file
     * @param project   Progetto
     * @return FileEntity rimosso, null se non trovato
     */

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

    /**
     * Calcola il valore di proportion per il bug e aggiorna il progetto.
     *
     * @param bug       Bug
     * @param project   Progetto
     */

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

            logger.info("Proportion value: {}", proportion);

            if(proportion > 0){
                bug.setProportion(proportion);
                project.addBugWithAV(bug);
            }
        }
    }

    /**
     * Stima il valore di proportion per il progetto e aggiorna i bug senza versioni interessate.
     *
     * @param project       Progetto
     * @param versionList   Lista delle versioni del progetto
     */

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

                project.getBugWithoutAV().remove(k);
                project.getBugWithoutAV().add(k, bug);
            }
        }
    }

    /**
     * Calcola il valore di proportion stimato per il progetto.
     *
     * @param project   Progetto
     * @return Valore di proportion stimato
     */

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


    /**
     * Restituisce l'indice della fixed version in base alla resolution date e al progetto.
     *
     * @param resolutionDate    Data di risoluzione
     * @param project           Progetto
     * @return Indice della fixed version
     */

    public int getFixedVersion(LocalDateTime resolutionDate, Project project){
        int fvIndex = 0;

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

    /**
     * Restituisce l'indice della opening version in base alla creation date del bug e al progetto.
     *
     * @param creationDate  Data di creazione del bug
     * @param project       Progetto
     * @return Indice della opening version
     */

    public int getOpeningVersion(LocalDateTime creationDate, Project project){
        int ovIndex = 0;

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

    /**
     * Restituisce l'indice della injected version in base alla lista delle versioni specificate,
     * la creation date del bug e il progetto.
     *
     * @param versionStringList Lista delle versioni
     * @param creationDate      Data di creazione del bug
     * @param project           Progetto
     * @return Indice della injected version
     */

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

    /**
     * Calcola la injected version in base all'indice della versione corrente, l'indice della opening version,
     * il valore di proportion e la lista delle versioni del progetto.
     *
     * @param fvIndex       Indice della fixed version
     * @param ovIndex       Indice della opening version
     * @param proportion    Valore di proportion
     * @param versionList   Lista delle versioni del progetto
     * @return Injected version
     */

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

