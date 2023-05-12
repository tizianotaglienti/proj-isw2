package controller;

import entities.Bug;
import entities.File;
import entities.Version;
import controller.JiraHelper;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Launcher {
    private static final String PROJECT_NAME = "BOOKKEEPER";

    public static List<File> halfData(List<Version> versions, List<File> files){
        versions = versions.subList(0, versions.size()/2);
        List<File> entries = new ArrayList<>();
        for(Version v : versions){
            for(File f : files){
                if(v.getName().equals(f.getVersion())){
                    entries.add(f);
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) throws IOException {
        JiraHelper helper = new JiraHelper(PROJECT_NAME);
        List<Version> versions = helper.getAllVersions();
        List<Bug> bugList = helper.getBugs(versions);
        for (Version v : versions) {
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
        for (Bug b : bugList){
            System.out.println("key " + b.getKey() + " OV: " + b.getOv().getIndex() + " FV: " + b.getFv().getIndex());
            if(b.getIv() != null){
                System.out.println(" IV: " + b.getIv().getIndex());
            }
        }



    }
}
