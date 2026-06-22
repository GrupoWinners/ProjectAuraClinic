package br.com.clinica.atendimento.entity;

import br.com.clinica.atendimento.enums.PrioridadeEncaminhamento;
import br.com.clinica.atendimento.enums.StatusEncaminhamento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "encaminhamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encaminhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @Column(name = "medico_origem_id", nullable = false)
    private Long medicoOrigemId;

    @Column(name = "medico_destino_id", nullable = false)
    private Long medicoDestinoId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "especialidade_destino", nullable = false, length = 100)
    private String especialidadeDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeEncaminhamento prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEncaminhamento status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();

        if (this.prioridade == null) {
            this.prioridade = PrioridadeEncaminhamento.MEDIA;
        }

        if (this.status == null) {
            this.status = StatusEncaminhamento.PENDENTE;
        }
    }
}
