package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para usuários — nunca expõe a senha ou o hash da senha ao cliente.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResposta {

    private Long id;
    private String nomeUsuario;

    // Nome do perfil de acesso do usuário (ADM, MEDICO, SECRETARIA)
    private String perfil;

    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
