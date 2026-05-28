package br.com.clinica.atendimento.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceitaRequisicao {

    @NotBlank(message = "Medicamento é obrigatório")
    private String medicamento;

    @NotBlank(message = "Dosagem é obrigatória")
    private String dosagem;

    @NotBlank(message = "Frequência é obrigatória")
    private String frequencia;

    @NotBlank(message = "Duração é obrigatória")
    private String duracao;

    private String observacoes;
}
