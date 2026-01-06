package ch.hearc.heg.scl.persistance;

import ch.hearc.heg.scl.business.Meteo;
import ch.hearc.heg.scl.business.Pays;
import ch.hearc.heg.scl.business.StationMeteo;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class Stockage {
    private static final Stockage stockage = new Stockage();
    private final List<Meteo> listMeteo = new ArrayList<>();
    private final List<StationMeteo> listStationMeteo = new ArrayList<>();
    private final List<Pays> listPays = new ArrayList<>();

    private Stockage (){
    }
    //Je remercie le père Fowler pour ce sublime Singleton qui résout mon problème

    public void addMeteo(Meteo meteo){
        listMeteo.add(meteo);
    }
    public void addStationMeteo(StationMeteo stationMeteo){
        listStationMeteo.add(stationMeteo);
    }
    public void addPays(Pays pays){
        listPays.add(pays);
    }
    public static Stockage getInstance()
    {
        return stockage;
    }
}
