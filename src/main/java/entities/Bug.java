package entities;

import java.util.List;

public class Bug {
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;
    private List<Version> av;

    private int fvIndex;
    private int ovIndex;
    private int ivIndex;

    private double proportion;
    private String resolutionDate;
    private String creationDate;
    private int id;

    public Bug(String key, Version fv, Version ov, Version iv, int id, List<Version> av){
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
        this.id = id;
        this.av = av;
    }

    public Bug(String key, Version fv, Version ov, int id, List<Version> av) {
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.id = id;
        this.av = av;
    }

    public Bug() {

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

    public int getFvIndex() {
        return fvIndex;
    }

    public void setFvIndex(int fvIndex) {
        this.fvIndex = fvIndex;
    }

    public int getOvIndex() {
        return ovIndex;
    }

    public void setOvIndex(int ovIndex) {
        this.ovIndex = ovIndex;
    }

    public int getIvIndex() {
        return ivIndex;
    }

    public void setIvIndex(int ivIndex) {
        this.ivIndex = ivIndex;
    }
}
