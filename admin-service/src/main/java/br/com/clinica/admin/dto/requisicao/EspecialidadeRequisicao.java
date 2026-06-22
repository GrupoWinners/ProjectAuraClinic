package br.com.clinica.admin.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// DTO de entrada para cadastro e atualização de especialidades médicas.
// A unicidade da descrição é validada no service antes de persistir.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeRequisicao {

    @NotBlank(message = "A descrição da especialidade é obrigatória")
    @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
    private String descricao;
}
