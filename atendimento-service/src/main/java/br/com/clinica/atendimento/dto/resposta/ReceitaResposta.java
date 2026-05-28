package br.com.clinica.atendimento.dto.resposta;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceitaResposta {

    private Long id;
    private String medicamento;
    private String dosagem;
    private String frequencia;
    private String duracao;
    private String observacoes;
    private LocalDateTime criadoEm;
}
