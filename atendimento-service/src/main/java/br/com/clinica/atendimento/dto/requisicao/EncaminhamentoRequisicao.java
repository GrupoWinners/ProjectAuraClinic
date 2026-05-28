package br.com.clinica.atendimento.dto.requisicao;

import br.com.clinica.atendimento.enums.PrioridadeEncaminhamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EncaminhamentoRequisicao {

    @NotNull(message = "ID do atendimento é obrigatório")
    private Long atendimentoId;

    @NotNull(message = "ID do médico de origem é obrigatório")
    private Long medicoOrigemId;

    @NotNull(message = "ID do médico de destino é obrigatório")
    private Long medicoDestinoId;

    @NotBlank(message = "Motivo do encaminhamento é obrigatório")
    private String motivo;

    @NotBlank(message = "Especialidade de destino é obrigatória")
    private String especialidadeDestino;

    private PrioridadeEncaminhamento prioridade;
}