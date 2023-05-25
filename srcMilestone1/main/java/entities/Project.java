package entities;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;

    private Integer halfVersion;

    private List<FileEntity> fileList = new ArrayList<>();
    private List<String> version = new ArrayList<>();
    private List<Version> versionList = new ArrayList<>();
    private List<String> versionAVList = new ArrayList<>();
    private List<Bug> bugList = new ArrayList<>();

    private List<Integer> buggyCommit = new ArrayList<>();
    private List<Bug> bugWithAV = new ArrayList<>();
    private List<Bug> bugWithoutAV = new ArrayList<>();

    public List<String> getVersion() {
        return version;
    }

    public void setVersion(List<String> version) {
        this.version = version;
    }

    public List<FileEntity> getFileList() {
        return fileList;
    }

    public void setFileList(List<FileEntity> fileList) {
        this.fileList = fileList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Version> getVersionList() {
        return versionList;
    }

    public void setVersionList(List<Version> versionList) {
        this.versionList = versionList;
    }

    public List<String> getVersionAVList() {
        return versionAVList;
    }

    public void setVersionAVList(List<String> versionAVList) {
        this.versionAVList = versionAVList;
    }

    public List<Bug> getBugList() {
        return bugList;
    }

    public void setBugList(List<Bug> bugList) {
        this.bugList = bugList;
    }

    public List<Bug> getBugWithAV() {
        return bugWithAV;
    }

    public void setBugWithAV(List<Bug> bugWithAV) {
        this.bugWithAV = bugWithAV;
    }

    public List<Bug> getBugWithoutAV() {
        return bugWithoutAV;
    }

    public void setBugWithoutAV(List<Bug> bugWithoutAV) {
        this.bugWithoutAV = bugWithoutAV;
    }

    public void addVersion(Version version){
        this.versionList.add(version);
    }

    public void addBugList(Bug bug){
        this.bugList.add(bug);
    }

    public void addBugWithAV(Bug bug){
        this.bugWithAV.add(bug);
    }

    public void addBugWithoutAV(Bug bug){
        this.bugWithoutAV.add(bug);
    }

    public Integer getHalfVersion() {
        return halfVersion;
    }

    public void setHalfVersion(Integer halfVersion) {
        this.halfVersion = halfVersion;
    }

    public List<Integer> getBuggyCommit() {
        return buggyCommit;
    }

    public void setBuggyCommit(List<Integer> buggyCommit) {
        this.buggyCommit = buggyCommit;
    }

    public void addFileToList(FileEntity file) {
        this.fileList.add(file);
    }
}
