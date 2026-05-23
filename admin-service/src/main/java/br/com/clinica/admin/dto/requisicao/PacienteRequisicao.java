package br.com.clinica.admin.dto.requisicao;

import br.com.clinica.admin.entity.Genero;
import br.com.clinica.admin.validacao.CpfValido;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

// DTO de entrada para cadastro e atualização de pacientes.
// Inclui todas as validações de dados pessoais, endereço e vínculo com convênio.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteRequisicao {

    @NotBlank(message = "O nome completo do paciente é obrigatório")
    @Size(max = 150, message = "O nome completo deve ter no máximo 150 caracteres")
    private String nomeCompleto;

    @NotBlank(message = "O RG do paciente é obrigatório")
    @Size(max = 20, message = "O RG deve ter no máximo 20 caracteres")
    private String rg;

    // CPF é validado pelo algoritmo Módulo 11 — unicidade verificada no service
    @NotBlank(message = "O CPF do paciente é obrigatório")
    @Size(max = 14, message = "O CPF deve ter no máximo 14 caracteres")
    @CpfValido
    private String cpf;

    @NotBlank(message = "O endereço do paciente é obrigatório")
    @Size(max = 255, message = "O endereço deve ter no máximo 255 caracteres")
    private String endereco;

    @NotBlank(message = "O bairro do paciente é obrigatório")
    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
    private String bairro;

    @NotBlank(message = "A cidade do paciente é obrigatória")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @NotBlank(message = "O estado do paciente é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve ter exatamente 2 caracteres (sigla)")
    private String estado;

    @NotBlank(message = "O CEP do paciente é obrigatório")
    @Size(max = 10, message = "O CEP deve ter no máximo 10 caracteres")
    private String cep;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @NotBlank(message = "O celular do paciente é obrigatório")
    @Size(max = 20, message = "O celular deve ter no máximo 20 caracteres")
    private String celular;

    @NotNull(message = "A data de nascimento do paciente é obrigatória")
    @Past(message = "A data de nascimento deve ser uma data passada")
    private LocalDate dataNascimento;

    @NotNull(message = "O gênero do paciente é obrigatório")
    private Genero genero;

    // Se possuiConvenio for true, convenioId deve ser informado
    @Builder.Default
    private Boolean possuiConvenio = false;

    private Long convenioId;
}
