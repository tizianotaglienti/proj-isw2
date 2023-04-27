package main.java.entities;

public class Bug {
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;

    public Bug(String key, Version fv, Version ov, Version iv){
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
    }
}
