package br.com.clinica.atendimento.dto.resposta;

import br.com.clinica.atendimento.enums.StatusExame;
import br.com.clinica.atendimento.enums.UrgenciaExame;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitacaoExameResposta {

    private Long id;
    private String tipoExame;
    private String descricao;
    private UrgenciaExame urgencia;
    private StatusExame status;
    private LocalDateTime criadoEm;
}