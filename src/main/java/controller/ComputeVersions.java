package controller;

import entities.Bug;
import entities.Version;
import org.json.JSONArray;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComputeVersions {
    public Bug bugBuilder(List<Version> versions, String openingDate, String fixDate, JSONArray avJSON, String key){
        Bug bug = null;
        Version ov = versionComputer(versions, openingDate);
        Version fv = versionComputer(versions, fixDate);
        Version iv = null;
        List<Version> av = affectedVersions(avJSON, versions);

        if(!av.isEmpty()){
            av.sort(Comparator.comparing(Version::getReleaseDate));
            //iv = av.get(av.size()-1);
            iv = av.get(0);
            bug = new Bug(key, fv, ov, iv, av);
        }
        else {
            bug = new Bug(key, fv, ov, av);
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
}