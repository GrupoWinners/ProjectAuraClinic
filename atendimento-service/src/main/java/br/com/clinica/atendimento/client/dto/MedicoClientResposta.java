package br.com.clinica.atendimento.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicoClientResposta {

    private Long id;
    private String nomeCompleto;
    private String crm;
    private Boolean ativo;
}