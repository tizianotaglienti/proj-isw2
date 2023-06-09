package controller;

import entities.Version;
import entities.Bug;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JiraHelper {
    private String projectName;

    /**
     * Costruttore per inizializzare l'istanza di `JiraHelper` con il nome del progetto specificato.
     *
     * @param projectName il nome del progetto Jira
     */

    public JiraHelper(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Legge e restituisce una stringa che rappresenta l'intero contenuto di un oggetto `Reader`.
     *
     * @param rd l'oggetto `Reader` da cui leggere
     * @return una stringa che rappresenta l'intero contenuto del `Reader`
     * @throws IOException se si verifica un errore durante la lettura del `Reader`
     */

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Legge un oggetto JSON da una specifica URL e restituisce un oggetto `JSONObject` che rappresenta il JSON.
     *
     * @param url l'URL da cui leggere l'oggetto JSON
     * @return un oggetto `JSONObject` che rappresenta il JSON letto
     * @throws IOException se si verifica un errore durante la lettura dell'URL
     * @throws JSONException se si verifica un errore nella manipolazione del JSON
     */

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    /**
     * Legge un array di oggetti JSON da una specifica URL e restituisce un oggetto `JSONArray` che rappresenta il JSON.
     *
     * @param url l'URL da cui leggere l'array di oggetti JSON
     * @return un oggetto `JSONArray` che rappresenta l'array di oggetti JSON letto
     * @throws IOException se si verifica un errore durante la lettura dell'URL
     * @throws JSONException se si verifica un errore nella manipolazione del JSON
     */

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONArray(jsonText);
        }
    }

    /**
     * Recupera tutte le versioni rilasciate di un progetto da Jira.
     *
     * @return una lista di oggetti `Version` che rappresentano le versioni rilasciate del progetto
     * @throws IOException se si verifica un errore durante la lettura delle versioni da Jira
     */

    public List<Version> getAllVersions() throws IOException {

        ArrayList<Version> versions = new ArrayList<>();

        String url = "https://issues.apache.org/jira/rest/api/2/project/"+this.projectName+"/versions";
        JSONArray json = readJsonArrayFromUrl(url);
        int tot = json.length();
        int i;
        int count = 0;
        for (i = 0; i < tot; i++){
            String nameRelease = json.getJSONObject(i).get("name").toString();
            String released = json.getJSONObject(i).get("released").toString();
            String releaseId = json.getJSONObject(i).get("id").toString();
            if(released.equalsIgnoreCase("true")){
                try{
                    LocalDateTime dateRelease;
                    String dateReleaseStr = json.getJSONObject(i).get("releaseDate").toString();
                    dateRelease = LocalDateTime.parse(dateReleaseStr + "T00:00:00");
                    count++;
                    Version element = new Version(nameRelease, dateRelease, releaseId, count);
                    versions.add(element);
                } catch (JSONException e){
                    Logger logger = Logger.getLogger(JiraHelper.class.getName());
                    String out ="Exception: versione di ["+this.projectName+"] senza releaseDate.";
                    logger.log(Level.INFO, out);
                }
            }
        }
        versions.sort(Comparator.comparing(Version::getReleaseDate));
        return versions;
    }

    /**
     * Recupera tutti i bug associati alle versioni di un progetto da Jira.
     *
     * @param versions una lista di oggetti `Version` che rappresentano le versioni del progetto
     * @return una lista di oggetti `Bug` che rappresentano i bug associati alle versioni
     * @throws IOException se si verifica un errore durante la lettura dei bug da Jira
     */

    public List<Bug> getBugs(List<Version> versions) throws IOException {

        final int MAX_RESULTS = 1000;
        int total = 0;
        List<Bug> bugsList = new ArrayList<>();
        int upperBound = 0;
        int lowerBound = 0;

        JSONArray avJSON;

        BugController bc = new BugController();

        do {
            upperBound = lowerBound + MAX_RESULTS;

            String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20%3D%20" + this.projectName +
                    "%20AND%20issuetype%20%3D%20Bug%20AND%20(%22status%22%20%3D%22resolved%22%20OR%20%22status" +
                    "%22%20%3D%20%22closed%22)%20AND%20%20%22resolution%22%20%3D%20%22fixed%22%20&fields=key," +
                    "resolutiondate,versions,created,fixVersions&startAt=" + lowerBound + "&maxResults=" + upperBound;
            JSONObject json = readJsonFromUrl(url);
            JSONArray issues = json.getJSONArray("issues");
            total = json.getInt("total");
            String fields = "fields";

            for (; lowerBound < total && lowerBound < upperBound; lowerBound++) {

                String key = issues.getJSONObject(lowerBound%MAX_RESULTS).get("key").toString();
                String resolutionDate = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).get("resolutiondate").toString();
                String creationDate = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).get("created").toString();
                int id = Integer.parseInt(key.split("-")[1]);
                avJSON = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).getJSONArray("versions");

                Bug bug = bc.bugBuilder(versions, creationDate, resolutionDate, id, avJSON, key);
                bugsList.add(bug);
            }
        } while(lowerBound < total);

        return bc.discardBugs(bugsList);
    }

}
