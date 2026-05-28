package br.com.clinica.atendimento.dto.resposta;

import br.com.clinica.atendimento.enums.PrioridadeEncaminhamento;
import br.com.clinica.atendimento.enums.StatusEncaminhamento;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncaminhamentoResposta {

    private Long id;
    private Long atendimentoId;
    private Long medicoOrigemId;
    private Long medicoDestinoId;
    private String motivo;
    private String especialidadeDestino;
    private PrioridadeEncaminhamento prioridade;
    private StatusEncaminhamento status;
    private LocalDateTime criadoEm;
}