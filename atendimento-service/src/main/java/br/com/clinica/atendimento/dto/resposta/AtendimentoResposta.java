package br.com.clinica.atendimento.dto.resposta;

import br.com.clinica.atendimento.enums.NivelUrgencia;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtendimentoResposta {

    private Long id;
    private Long prontuarioId;
    private Long consultaId;
    private Long medicoId;
    private Long pacienteId;
    private LocalDateTime dataAtendimento;
    private String sintomas;
    private String diagnostico;
    private String escopoMedico;
    private String observacoes;
    private NivelUrgencia nivelUrgencia;
    private List<ReceitaResposta> receitas;
    private List<SolicitacaoExameResposta> solicitacoesExame;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}