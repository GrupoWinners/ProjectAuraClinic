package br.com.clinica.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelamentoRequisicao {

    @NotBlank(message = "O motivo do cancelamento é obrigatório")
    private String motivo;

    @NotBlank(message = "É necessário informar quem solicitou o cancelamento")
    private String canceladoPor;
}