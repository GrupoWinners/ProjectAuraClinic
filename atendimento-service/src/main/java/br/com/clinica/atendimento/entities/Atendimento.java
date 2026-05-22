package br.com.clinica.atendimento.entities;

import br.com.clinica.atendimento.enums.NivelUrgencia;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "atendimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Atendimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "prontuario_id", nullable = false)
    private Prontuario prontuario;

    @Column(name = "consulta_id", nullable = false)
    private Long consultaId;

    @Column(name = "medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "data_atendimento", nullable = false)
    private LocalDateTime dataAtendimento;

    @Column(columnDefinition = "TEXT")
    private String sintomas;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "escopo_medico", nullable = false, columnDefinition = "TEXT")
    private String escopoMedico;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_urgencia", nullable = false)
    private NivelUrgencia nivelUrgencia;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receita> receitas = new ArrayList<>();

    @OneToMany(mappedBy = "atendimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SolicitacaoExame> solicitacoesExame = new ArrayList<>();

    @PrePersist
    public void prePersist(){
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();

        if (this.dataAtendimento == null) {
            this.dataAtendimento = LocalDateTime.now();
        }

        if (this.nivelUrgencia == null) {
            this.nivelUrgencia = NivelUrgencia.VERDE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
