package br.com.clinica.atendimento.dto.requisicao;

import br.com.clinica.atendimento.enums.UrgenciaExame;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoExameRequisicao {

    @NotBlank(message = "Tipo do exame é obrigatório")
    private String tipoExame;

    @NotBlank(message = "Descrição do exame é obrigatória")
    private String descricao;

    private UrgenciaExame urgencia;
}