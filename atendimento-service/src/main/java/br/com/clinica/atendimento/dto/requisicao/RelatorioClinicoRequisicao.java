package br.com.clinica.atendimento.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioClinicoRequisicao {

    @NotNull(message = "ID do atendimento é obrigatório")
    private Long atendimentoId;

    @NotBlank(message = "Conteúdo do relatório é obrigatório")
    private String conteudo;
}