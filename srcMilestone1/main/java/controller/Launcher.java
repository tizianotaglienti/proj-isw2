package controller;

import entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

public class Launcher {
    private static final String PROJECT_NAME = "STORM";
    private static final Logger logger = LogManager.getLogger(Launcher.class);

    /**
     * Main method per l'avvio del programma.
     *
     * @param args gli argomenti della riga di comando
     * @throws IOException se si verifica un errore di I/O
     * @throws GitAPIException se si verifica un errore nell'utilizzo delle API di JGit
     */

    public static void main(String[] args) throws IOException, GitAPIException {
        JiraHelper helper = new JiraHelper(PROJECT_NAME);
        List<Version> versionList = helper.getAllVersions();
        List<Bug> bugList = helper.getBugs(versionList);
        BugController prop = new BugController();

        logger.debug(bugList);
        Project project = new Project();

        project.setName(PROJECT_NAME);

        // scorro la lista delle versioni
        for (Version v : versionList) {
            logger.debug("%d %s date: %tF", v.getIndex(), v.getName(), v.getReleaseDate());
        }

        // scorro la lista dei bug
        for (Bug b : bugList){

            logger.debug("Key: %s OV: %d FV: %d", b.getKey(), b.getOv().getIndex(), b.getFv().getIndex());

            // controllo se il bug possiede una injected version
            if(b.getIv() != null){
                logger.debug("IV: %d", b.getIv().getIndex());
            }

            else if(b.getIv() == null){
                // se non ce l'ha, calcolo proportion
                prop.proportion(b, project);
                prop.estimateProportion(project, versionList);
            }
        }

        // imposto bugList e versionList per il progetto
        project.setBugList(bugList);
        project.setVersionList(versionList);

        // calcolo half version che servirà per filtrare i dati
        int releaseNumber = project.getVersionList().size();
        project.setHalfVersion(releaseNumber/2);

        // chiamata al metodo createData
        createData(project, prop);

        // generazione del file csv utilizzando i dati del progetto
        new CsvController(project);
    }

    /**
     * Crea i dati per il progetto, inclusi i commit e le metriche associate.
     *
     * @param project       Progetto
     * @param prop          BugController
     * @throws IOException se si verifica un errore di I/O
     * @throws GitAPIException se si verifica un errore nell'utilizzo delle API di JGit
     */

    private static void createData(Project project, BugController prop) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        String gitRepository = System.getProperty("user.dir") + "\\" + PROJECT_NAME.toLowerCase() + "/.git";

        File file = new File(gitRepository);
        Repository repo = builder.setGitDir(file).readEnvironment().findGitDir().build();

        try(Git git = new Git(repo)){

            Iterable<RevCommit> commits = null;
            commits = git.log().all().call();   // prendo tutte le informazioni sui commit
                                                // da cui poi calcolo le metriche

            iterateOnCommit(commits, repo, project, prop);     // studio i commit
        }
    }

    /**
     * Itera sui commit del repository git e analizza le modifiche associate.
     *
     * @param commits       Iterable dei commit
     * @param repo          Repository git
     * @param project       Progetto
     * @param prop          BugController
     * @throws IOException se si verifica un errore di I/O
     */

    private static void iterateOnCommit(Iterable<RevCommit> commits, Repository repo, Project project, BugController prop) throws IOException {
        for(RevCommit commit : commits){
            LocalDate commitLocalDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            int belongingVersion = prop.getCommitVersion(commitLocalDate, project);

            // ignora i bug risalenti alla seconda metà delle release
            if(commit.getParentCount() == 0 || belongingVersion >= project.getHalfVersion() + 1){
                continue;
            }

            Commit validCommit = new Commit();
            validCommit.setMessage(commit.getFullMessage());
            validCommit.setDate(commitLocalDate);
            validCommit.setBelongingVersion(belongingVersion);

            List<Bug> bugsForCommit = prop.getBugsForCommit(commit.getFullMessage(), project);
            validCommit.setBugList(bugsForCommit);

            iterateOnChange(repo, commit, validCommit, project, prop);
        }
    }

    /**
     * Itera sulle modifiche di un commit e calcola le metriche associate ai file Java modificati.
     *
     * @param repo          Repository git
     * @param commit        Commit corrente
     * @param validCommit   Commit valido
     * @param project       Progetto
     * @param prop          BugController
     * @throws IOException se si verifica un errore di I/O
     */

    private static void iterateOnChange(Repository repo, RevCommit commit, Commit validCommit, Project project, BugController prop) throws IOException {
        List<DiffEntry> filesChanged;
        try(DiffFormatter differenceBetweenCommits = new DiffFormatter(NullOutputStream.INSTANCE)){
            differenceBetweenCommits.setRepository(repo);
            filesChanged = differenceBetweenCommits.scan(commit.getParent(0), commit);
            validCommit.setFilesChanged(filesChanged);

            for(DiffEntry singleFileChanged : filesChanged) {
                if (singleFileChanged.getNewPath().endsWith(".java")) {
                    prop.getMetrics(validCommit, singleFileChanged, differenceBetweenCommits, project);
                    project = prop.setBuggy(validCommit, singleFileChanged, project);
                }
            }
        }
    }
}
