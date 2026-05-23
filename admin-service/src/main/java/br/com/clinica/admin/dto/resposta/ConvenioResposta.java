package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para convênios — inclui dados completos incluindo CNPJ e timestamps de auditoria.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioResposta {

    private Long id;
    private String nomeEmpresa;
    private String cnpj;
    private String telefone;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
