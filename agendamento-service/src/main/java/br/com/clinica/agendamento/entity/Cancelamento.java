package br.com.clinica.agendamento.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cancelamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cancelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "consulta_id", nullable = false, unique = true)
    private Consulta consulta;

    @Column(name = "motivo", nullable = false, length = 500)
    private String motivo;

    @Column(name = "data_hora_cancelamento", nullable = false)
    private LocalDateTime dataHoraCancelamento;
}