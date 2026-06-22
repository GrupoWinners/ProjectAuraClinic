package br.com.clinica.agendamento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "cancelamentos") // Ajustado de tb_cancelamentos para cancelamentos conforme o DDL
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cancelamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "consulta_id", nullable = false)
    private Consulta consulta; // Vínculo com a consulta que foi cancelada

    @Column(nullable = false)
    private String motivo; // O motivo que o revisor relembrou ser obrigatório

    @Column(name = "cancelado_por", nullable = false)
    private String canceladoPor; // Adicionado conforme solicitação do revisor (ex: PACIENTE, MEDICO)

    @Column(name = "data_cancelamento", nullable = false)
    private LocalDateTime dataCancelamento;

    @PrePersist
    protected void onCreate() {
        this.dataCancelamento = LocalDateTime.now();
    }
}