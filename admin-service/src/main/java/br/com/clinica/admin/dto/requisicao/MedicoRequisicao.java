package br.com.clinica.admin.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// DTO de entrada para cadastro e atualização de médicos.
// Contém as validações de Bean Validation antes de chegar na camada de serviço.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicoRequisicao {

    @NotBlank(message = "O nome completo do médico é obrigatório")
    @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "O CRM do médico é obrigatório")
    @Size(max = 20, message = "O CRM deve ter no máximo 20 caracteres")
    private String crm;

    // ID da especialidade à qual o médico será vinculado — deve existir no banco
    @NotNull(message = "A especialidade do médico é obrigatória")
    private Long especialidadeId;
}
