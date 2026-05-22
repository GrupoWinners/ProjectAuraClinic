package br.com.clinica.agendamento.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvenioResponse {
    private Long id;
    private String nome;
    private boolean ativo;
}