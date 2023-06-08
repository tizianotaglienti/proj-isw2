package controller;

import entities.Version;
import entities.Bug;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public JiraHelper(String projectName) {
        this.projectName = projectName;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            System.out.println(jsonText.substring(0,20));
            System.out.println(jsonText.substring(jsonText.length()-20));
            return new JSONArray(jsonText);
        }
    }

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

    public List<Bug> getBugs(List<Version> versions) throws IOException {

        final int MAX_RESULTS = 1000;
        int total = 0;
        List<Bug> bugsList = new ArrayList<>();
        int upperBound = 0;
        int lowerBound = 0;

        JSONArray avJSON;

        ComputeVersions cv = new ComputeVersions();

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

                // WARNING CONTROLLARE COME SONO FATTI
                String key = issues.getJSONObject(lowerBound%MAX_RESULTS).get("key").toString();
                String version = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).get("versions").toString();
                String resolutionDate = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).get("resolutiondate").toString();
                String creationDate = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).get("created").toString();
                int id = Integer.valueOf(key.split("-")[1]);
                avJSON = issues.getJSONObject(lowerBound%MAX_RESULTS).getJSONObject(fields).getJSONArray("versions");

                Bug bug = cv.bugBuilder(versions, creationDate, resolutionDate, id, avJSON, key);
                bugsList.add(bug);
            }
        } while(lowerBound < total);

        return List<Bug> undiscardedBugs = cv.discardBugs(bugsList);
        return undiscardedBugs;
    }

}
