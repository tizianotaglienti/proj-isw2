package controller;

import entities.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

public class Launcher {
    private static final String PROJECT_NAME = "STORM";

    public static List<FileEntity> halfData(List<Version> versions, List<FileEntity> files){
        versions = versions.subList(0, versions.size()/2);
        List<FileEntity> entries = new ArrayList<>();
        for(Version v : versions){
            for(FileEntity f : files){
                if(v.getName().equals(f.getVersion())){
                    entries.add(f);
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        JiraHelper helper = new JiraHelper(PROJECT_NAME);
        List<Version> versionList = helper.getAllVersions();
        List<Bug> bugList = helper.getBugs(versionList);
        BugController prop = new BugController();

        logger.log(bugList);
        Project project = new Project();

        project.setName(PROJECT_NAME);

        for (Version v : versionList) {
            logger.log(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
        for (Bug b : bugList){

            logger.log("key " + b.getKey() + " OV: " + b.getOv().getIndex() + " FV: " + b.getFv().getIndex());
            if(b.getIv() != null){
                logger.log(" IV: " + b.getIv().getIndex());
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
        project.setBugList(bugList);
        project.setVersionList(versionList);


        int releaseNumber = project.getVersionList().size();
        project.setHalfVersion(releaseNumber/2);

        createData(project, prop);

        new csvController(project);

    }

    private static void createData(Project project, BugController prop) throws IOException, GitAPIException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        // nullpointerexception perché project non c'è (giustamente)... modificare questa cosa
        String gitRepository = System.getProperty("user.dir") + "\\" + PROJECT_NAME.toLowerCase() + "/.git";


        File file = new File(gitRepository);
        Repository repo = builder.setGitDir(file).readEnvironment().findGitDir().build();

        try(Git git = new Git(repo)){

            Iterable<RevCommit> commits = null;
            commits = git.log().all().call();   // prendo tutte le informazioni sui commit
                                                // da cui poi calcolo le metriche

            iterateOnCommit(commits, repo, project, prop);     // vado a studiare i commit
        }
    }

    private static void iterateOnCommit(Iterable<RevCommit> commits, Repository repo, Project project, BugController prop) throws IOException {
        for(RevCommit commit : commits){
            LocalDate commitLocalDate = commit.getCommitterIdent().getWhen().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // attenzione: cv = null !!!
                // possibile soluzione far diventare prop cv e lo chiamo in create data e tutti i successivi.
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
