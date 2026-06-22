package br.com.clinica.atendimento.dto.requisicao;

import br.com.clinica.atendimento.enums.StatusEncaminhamento;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarStatusEncaminhamentoRequisicao {

    @NotNull(message = "Status do encaminhamento é obrigatório")
    private StatusEncaminhamento status;
}