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
public class CancelamentoRequest {

    @NotBlank(message = "O motivo do cancelamento é obrigatório")
    private String motivo;

    @NotBlank(message = "O autor do cancelamento é obrigatório")
    private String canceladoPor; // Adicionado para salvar localmente no banco
}