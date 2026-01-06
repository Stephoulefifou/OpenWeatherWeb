package ch.hearc.heg.scl.business;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.java.Log;

import java.io.Serializable;

@Entity
@Table(name = "PAYS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Log
public class Pays implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_PAYS")
    @SequenceGenerator(name = "SEQ_PAYS", sequenceName = "SEQ_PAYS", allocationSize = 1)
    @Column(name = "NUMERO")
    private Integer numero;

    @Column(name = "CODE", nullable = false, unique = true)
    private String code;

    @Column(name = "NOM", nullable = false)
    private String nom;
}
