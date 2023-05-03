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
        List<Version> av = affectedVersions(avJSON);

        if(!av.isEmpty()){
            av.sort(Comparator.comparing(Version::getReleaseDate));
            iv = av.get(av.size()-1);
            bug = new Bug(key, fv, ov, iv, av);
        }
        else {
            bug = new Bug(key, fv, ov, av);
        }
        return bug;
    }

    private List<Version> affectedVersions(JSONArray avJSON){


    }

    public Version versionComputer(List<Version> versions, String date){
        Version version = null;
        String newDate = date.substring(0, date.length() - 9); //discard last 9 characters
        LocalDateTime datetime = LocalDateTime.parse(newDate);
        for(Version v : versions) {
            if (datetime.compareTo(v.getReleaseDate()) < 0) {
                version = v;
            }
            return version;
        }
    }
}
