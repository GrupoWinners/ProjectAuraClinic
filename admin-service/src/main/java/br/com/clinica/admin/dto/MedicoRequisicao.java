package br.com.clinica.admin.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MedicoRequisicao {

    @NotBlank(message = "O Nome completo e obrigatorio, tipo obrigatorio e O B R I G A T O R I O")
    @Size(max = 150, message = " o nome completo deve ter no maximo 150 caracteres")

    private String nomeCompleto;

    @NotBlank(message = "o CRM e obrigatorio")
    @Size(max = 20, message = "o CRM Deve ter no maximo 20 caracteres")
    private String crm;

    @NotNull(message = "A especialidade e obrigatoria")
    private Long especialidadeId;

}