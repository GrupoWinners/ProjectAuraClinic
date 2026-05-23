package br.com.clinica.admin.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

// DTO de entrada para o endpoint de autenticação (login).
// As credenciais são validadas contra o banco e, se corretas, retornam um token JWT.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequisicao {

    @NotBlank(message = "O nome de usuário é obrigatório")
    private String nomeUsuario;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;
}
