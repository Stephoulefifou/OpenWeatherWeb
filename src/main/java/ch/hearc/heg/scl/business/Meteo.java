package ch.hearc.heg.scl.business;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "METEO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "station") // ✅ Exclure station pour éviter boucle infinie
@Log
public class Meteo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_METEO")
    @SequenceGenerator(name = "SEQ_METEO", sequenceName = "SEQ_METEO", allocationSize = 1)
    @Column(name = "NUMERO")
    private Integer numero;

    @ManyToOne
    @JoinColumn(name = "STATION_NUMERO", nullable = false)
    private StationMeteo station;

    @Column(name = "date_mesure")
    private LocalDateTime dateMesure;
    @Column(name = "temperature")
    private Double temperature;
    @Column(name = "pression")
    private int pression;
    @Column(name = "humidite")
    private int humidite;
    @Column(name = "visibilite")
    private Integer visibilite;
    @Column(name = "precipitation")
    private Double precipitation;

    @ElementCollection
    @CollectionTable(
            name = "METEO_DESCRIPTION",
            joinColumns = @JoinColumn(name = "METEO_NUMERO")
    )
    @Column(name = "TEXTE", nullable = false) // correspond à la colonne TEXTE de ta table
    private List<String> texte = new ArrayList<>();

    public String getDateMesureStr() {
        return dateMesure.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public String getPrettyDate() {
        if (this.dateMesure == null) return "Date inconnue";
        // Format : Jour.Mois.Année Heure:Minute
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return this.dateMesure.format(formatter);
    }
}
