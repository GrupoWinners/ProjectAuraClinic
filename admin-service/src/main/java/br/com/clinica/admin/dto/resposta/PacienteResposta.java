package br.com.clinica.admin.dto.resposta;

import br.com.clinica.admin.entity.Genero;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

// DTO de saída para as operações de CRUD de pacientes.
// Inclui dados pessoais, endereço e informações de convênio quando aplicável.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteResposta {

    private Long id;
    private String nomeCompleto;
    private String rg;
    private String cpf;
    private String endereco;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String telefone;
    private String celular;
    private LocalDate dataNascimento;
    private Genero genero;

    // Indica se o paciente possui cobertura de convênio
    private Boolean possuiConvenio;

    // Nome do convênio vinculado — nulo quando não possui plano de saúde
    private String nomeConvenio;

    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
