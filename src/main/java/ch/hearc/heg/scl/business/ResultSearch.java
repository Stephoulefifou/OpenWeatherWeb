package ch.hearc.heg.scl.business;

import lombok.Getter;

import java.io.Serializable;
import java.rmi.Remote;

public class ResultSearch implements Remote, Serializable {
    private static final long serialVersionUID = 1L;
    @Getter
    private Pays pays;
    @Getter
    private Meteo meteo;
    @Getter
    private StationMeteo stationMeteo;

    public ResultSearch(Pays pays, Meteo meteo, StationMeteo stationMeteo) {
        this.pays = pays;
        this.meteo = meteo;
        this.stationMeteo = stationMeteo;
    }

    public String toString(){
        String s = pays.getNom();
        return s;
    }
}
