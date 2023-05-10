package entities;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private String name;


    private List<File> fileList = new ArrayList<>();
    private List<String> version = new ArrayList<>();

    public List<String> getVersion() {
        return version;
    }

    public void setVersion(List<String> version) {
        this.version = version;
    }

    public List<File> getFileList() {
        return fileList;
    }

    public void setFileList(List<File> fileList) {
        this.fileList = fileList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
