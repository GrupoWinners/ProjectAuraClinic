package br.com.clinica.agendamento.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultas") // Ajustado para 'consultas' removendo o prefixo tb_
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status; // Enum que gerencia a máquina de estados (AGENDADA, CANCELADA, REMARCADA...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoConsulta tipo; // Adicionado conforme solicitação do revisor

    @Column(name = "consulta_original_id")
    private Long consultaOriginalId; // Vínculo exigido pela Regra 10 para identificar a consulta de origem

    // Colunas de auditoria exigidas no DDL do banco de dados
    @Column(name = "criado_em", updatable = false, nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    // Métodos de ciclo de vida do JPA para preenchimento automático da auditoria local
    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}