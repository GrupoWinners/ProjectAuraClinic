package br.com.clinica.atendimento.client.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PacienteClientResposta {

    private Long id;
    private String nomeCompleto;
    private String cpf;
    private Boolean ativo;
}