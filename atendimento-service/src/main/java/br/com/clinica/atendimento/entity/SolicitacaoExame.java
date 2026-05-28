package br.com.clinica.atendimento.entity;

import br.com.clinica.atendimento.enums.StatusExame;
import br.com.clinica.atendimento.enums.UrgenciaExame;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacoes_exame")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoExame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "atendimento_id", nullable = false)
    private Atendimento atendimento;

    @Column(name = "tipo_exame", nullable = false, length = 100)
    private String tipoExame;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UrgenciaExame urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusExame status;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();

        if (this.urgencia == null) {
            this.urgencia = UrgenciaExame.NORMAL;
        }

        if (this.status == null) {
            this.status = StatusExame.SOLICITADO;
        }
    }
}