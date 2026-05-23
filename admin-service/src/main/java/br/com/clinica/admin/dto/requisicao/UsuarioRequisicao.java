package br.com.clinica.admin.dto.requisicao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

// DTO de entrada para criação de usuários no sistema administrativo.
// A senha recebida aqui é criptografada com BCrypt antes de ser persistida.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequisicao {

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(max = 100, message = "O nome de usuário deve ter no máximo 100 caracteres")
    private String nomeUsuario;

    // Senha em texto plano — nunca persistida diretamente (criptografada com BCrypt strength 12)
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, max = 100, message = "A senha deve ter entre 6 e 100 caracteres")
    private String senha;

    @NotNull(message = "O ID do perfil é obrigatório")
    private Long perfilId;
}
