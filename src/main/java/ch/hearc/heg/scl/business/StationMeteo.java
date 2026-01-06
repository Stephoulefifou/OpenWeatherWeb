package ch.hearc.heg.scl.business;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.java.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "STATION_METEO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "donneesMeteo")
@Log
public class StationMeteo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_STATION_METEO")
    @SequenceGenerator(name = "SEQ_STATION_METEO", sequenceName = "SEQ_STATION_METEO", allocationSize = 1)
    @Column(name = "NUMERO")
    private Integer numero;

    @Column(name = "NOM", nullable = false)
    private String nom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "PAYS_NUMERO", nullable = false) // FK vers PAYS.NUMERO
    private Pays pays;

    @Column(name = "LATITUDE")
    private Double latitude;

    @Column(name = "LONGITUDE")
    private Double longitude;

    @Column(name = "OPENWEATHERMAP_ID")
    private Integer openWeatherMapId;

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "STATION_NUMERO") // FK dans METEO vers STATION_METEO.NUMERO
    private List<Meteo> donneesMeteo = new ArrayList<>();
}
