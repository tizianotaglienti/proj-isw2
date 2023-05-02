package entities;

import java.util.List;

public class Bug {
    private String key;
    private Version fv;
    private Version ov;
    private Version iv;
    private List<Version> av;

    public Bug(String key, Version fv, Version ov, Version iv, List<Version> av){
        this.key = key;
        this.fv = fv;
        this.ov = ov;
        this.iv = iv;
        this.av = av;
    }
}
