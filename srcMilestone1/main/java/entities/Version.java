package entities;

import java.time.LocalDateTime;

public class Version {
    private String name;
    private LocalDateTime releaseDate;
    private int index;

    public Version(String name, LocalDateTime releaseDate, String versionId, int index){
        this.name = name;
        this.releaseDate = releaseDate;
        this.versionId = versionId;
        this.index = index;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public LocalDateTime getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDateTime releaseDate){
        this.releaseDate = releaseDate;
    }

    public int getIndex(){
        return index;
    }

    public void setIndex(int index){
        this.index = index;
    }
}
