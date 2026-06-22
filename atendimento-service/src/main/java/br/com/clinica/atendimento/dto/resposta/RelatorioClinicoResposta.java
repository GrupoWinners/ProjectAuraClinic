package br.com.clinica.atendimento.dto.resposta;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatorioClinicoResposta {

    private Long id;
    private Long atendimentoId;
    private String conteudo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}