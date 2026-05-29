package br.com.clinica.atendimento.dto.requisicao;

import br.com.clinica.atendimento.enums.NivelUrgencia;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarUrgenciaRequisicao {

    @NotNull(message = "Nível de urgência é obrigatório")
    private NivelUrgencia nivelUrgencia;
}