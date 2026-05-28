package br.com.clinica.atendimento.client.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReconsultaClientRequisicao {

    private Long pacienteId;
    private Long medicoId;
    private LocalDateTime dataHora;
    private String tipo;
}