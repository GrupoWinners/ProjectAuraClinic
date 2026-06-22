package br.com.clinica.atendimento.dto.requisicao;

import br.com.clinica.atendimento.enums.NivelUrgencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtendimentoRequisicao {

    @NotNull(message = "ID da consulta é obrigatório")
    private Long consultaId;

    @NotNull(message = "ID do médico é obrigatório")
    private Long medicoId;

    @NotNull(message = "ID do paciente é obrigatório")
    private Long pacienteId;

    private String sintomas;

    private String diagnostico;

    @NotBlank(message = "Escopo médico é obrigatório")
    private String escopoMedico;

    private String observacoes;

    private NivelUrgencia nivelUrgencia;

    @Valid
    private List<ReceitaRequisicao> receitas;

    @Valid
    private List<SolicitacaoExameRequisicao> solicitacoesExame;
}