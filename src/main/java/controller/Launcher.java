package controller;

import entities.Version;
import controller.JiraHelper;

import java.io.IOException;
import java.util.List;

public class Launcher {
    private static final String PROJECT_NAME = "BOOKKEEPER";

    public static void main(String[] args) throws IOException {
        JiraHelper helper = new JiraHelper(PROJECT_NAME);
        List<Version> versions = helper.getAllVersions();
        for (Version v : versions) {
            System.out.println(v.getIndex() + " " + v.getName() + " date:" + v.getReleaseDate());
        }
    }
}
