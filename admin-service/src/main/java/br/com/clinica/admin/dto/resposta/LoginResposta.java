package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para o endpoint de autenticação — retorna o token JWT gerado após login bem-sucedido.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResposta {

    private String token;

    // Tipo do token conforme padrão Bearer JWT
    @Builder.Default
    private String tipo = "Bearer";

    private String nomeUsuario;
    private String perfil;

    // Data/hora de expiração do token para o cliente calcular renovação
    private LocalDateTime expiracao;
}
