package entities;

import org.eclipse.jgit.diff.DiffEntry;

import java.time.LocalDate;
import java.util.List;

public class Commit {
    private LocalDate date;
    private int belongingVersion;
    private List<Bug> bugList;
    private String message;
    private List<DiffEntry> filesChanged;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getBelongingVersion() {
        return belongingVersion;
    }

    public void setBelongingVersion(int belongingVersion) {
        this.belongingVersion = belongingVersion;
    }

    public List<Bug> getBugList() {
        return bugList;
    }

    public void setBugList(List<Bug> bugList) {
        this.bugList = bugList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<DiffEntry> getFilesChanged() {
        return filesChanged;
    }

    public void setFilesChanged(List<DiffEntry> filesChanged) {
        this.filesChanged = filesChanged;
    }
}
