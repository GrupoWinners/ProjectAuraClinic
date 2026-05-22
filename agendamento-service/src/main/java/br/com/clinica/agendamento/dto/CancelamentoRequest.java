package br.com.clinica.agendamento.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelamentoRequest {
    private String motivo;
}