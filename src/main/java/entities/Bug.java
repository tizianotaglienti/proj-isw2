package entities;

import java.util.List;

public class Bug {
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;
    private List<Version> av;

    private double proportion;
    private String resolutionDate;
    private String creationDate;
    private int id;

    public Bug(String key, Version fv, Version ov, Version iv, List<Version> av){
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
        this.av = av;
    }

    public Bug(String key, Version fv, Version ov, List<Version> av) {
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.av = av;
    }

    public String getKey(){
        return key;
    }

    public void setKey(String key){
        this.key = key;
    }

    public Version getFv(){
        return fv;
    }

    public void setFv(Version fv){
        this.fv = fv;
    }

    public Version getOv(){
        return ov;
    }

    public void setOv(Version ov){
        this.ov = ov;
    }


    public Version getIv() {
        return iv;
    }

    public void setIv(Version iv) {
        this.iv = iv;
    }

    public List<Version> getAv() {
        return av;
    }

    public void setAv(List<Version> av) {
        this.av = av;
    }

    public double getProportion() {
        return proportion;
    }

    public void setProportion(double proportion) {
        this.proportion = proportion;
    }

    public String getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(String resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
