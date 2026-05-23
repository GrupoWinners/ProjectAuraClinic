package br.com.clinica.admin.dto.requisicao;

import br.com.clinica.admin.validacao.CnpjValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

// DTO de entrada para cadastro e atualização de convênios médicos.
// O CNPJ é validado pelo algoritmo antes de persistir — unicidade verificada no service.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvenioRequisicao {

    @NotBlank(message = "O nome da empresa do convênio é obrigatório")
    @Size(max = 150, message = "O nome da empresa deve ter no máximo 150 caracteres")
    private String nomeEmpresa;

    // CNPJ formato: XX.XXX.XXX/XXXX-XX — validado pelo algoritmo Módulo 11
    @NotBlank(message = "O CNPJ do convênio é obrigatório")
    @Size(max = 18, message = "O CNPJ deve ter no máximo 18 caracteres")
    @CnpjValido
    private String cnpj;

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres")
    private String telefone;
}
