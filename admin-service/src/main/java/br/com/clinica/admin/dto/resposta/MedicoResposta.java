package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para as operações de CRUD de médicos.
// Expõe apenas os dados necessários para o cliente — nunca a entidade diretamente.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicoResposta {

    private Long id;
    private String nomeCompleto;
    private String crm;

    // Descrição da especialidade (nome) — evita expor o objeto Especialidade completo
    private String especialidade;

    private Boolean ativo;
    private LocalDateTime criadoEm;
}
