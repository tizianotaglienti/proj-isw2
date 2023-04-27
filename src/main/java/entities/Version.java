package main.java.entities;

import java.time.LocalDateTime;

public class Version {
    private String name;
    private LocalDateTime releaseDate;

    public Version(String name, LocalDateTime releaseDate){
        this.name = name;
        this.releaseDate = releaseDate;
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
}
