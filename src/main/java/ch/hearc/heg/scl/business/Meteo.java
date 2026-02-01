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
@ToString(exclude = "station")
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

    // --- Températures ---
    @Column(name = "temperature")
    private Double temperature;
    @Column(name = "ressenti")
    private Double ressenti;
    @Column(name = "temp_min")
    private Double tempMin;
    @Column(name = "temp_max")
    private Double tempMax;

    // --- Atmosphère ---
    @Column(name = "pression")
    private Integer pression;
    @Column(name = "humidite")
    private Integer humidite;
    @Column(name = "visibilite")
    private Integer visibilite;
    @Column(name = "precipitation")
    private Double precipitation;

    // --- Vent ---
    @Column(name = "vent_vitesse")
    private Double ventVitesse;
    @Column(name = "vent_direction")
    private Integer ventDirection;
    @Column(name = "vent_rafales")
    private Double ventRafales;

    // --- Éphéméride ---
    @Column(name = "lever_soleil")
    private LocalDateTime leverSoleil;
    @Column(name = "coucher_soleil")
    private LocalDateTime coucherSoleil;

    @ElementCollection
    @CollectionTable(
            name = "METEO_DESCRIPTION",
            joinColumns = @JoinColumn(name = "METEO_NUMERO")
    )
    @Column(name = "TEXTE", nullable = false)
    private List<String> texte = new ArrayList<>();

    public String getDateMesureStr() {
        return dateMesure != null ? dateMesure.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "";
    }

    public String getPrettyDate() {
        if (this.dateMesure == null) return "Date inconnue";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return this.dateMesure.format(formatter);
    }
}