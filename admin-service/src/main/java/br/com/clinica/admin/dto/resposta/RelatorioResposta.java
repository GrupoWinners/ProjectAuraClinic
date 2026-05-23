package br.com.clinica.admin.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

// DTO de saída para relatórios gerenciais — agrega estatísticas por período, médico, especialidade ou convênio.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelatorioResposta {

    // Tipo de relatório gerado (ex: CONSULTAS_POR_PERIODO, PACIENTES_CADASTRADOS)
    private String tipoRelatorio;

    // Período de referência do relatório
    private LocalDateTime inicio;
    private LocalDateTime fim;

    // Total de registros no período consultado
    private Long totalRegistros;

    // Dados agregados específicos por tipo de relatório (descrição, médico, especialidade, etc.)
    private String descricao;

    private Object dados;

    private LocalDateTime geradoEm;
}
