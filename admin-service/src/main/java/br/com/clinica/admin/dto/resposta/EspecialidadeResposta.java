package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para especialidades médicas — usado na listagem e nas respostas de CRUD.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EspecialidadeResposta {

    private Long id;
    private String descricao;
    private Boolean ativo;
    private LocalDateTime criadoEm;
}
