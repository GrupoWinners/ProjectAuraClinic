package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.PacienteRequisicao;
import br.com.clinica.admin.dto.resposta.PacienteResposta;
import br.com.clinica.admin.entity.Convenio;
import br.com.clinica.admin.entity.Paciente;

// Mapper responsável por converter a entidade Paciente para seus DTOs de entrada e saída,
// isolando o domínio da camada HTTP e garantindo que campos sensíveis não sejam expostos.
public class PacienteMapper {

    public static Paciente paraEntidade(PacienteRequisicao requisicao, Convenio convenio) {
        if (requisicao == null) return null;
        return Paciente.builder()
                .nomeCompleto(requisicao.getNomeCompleto())
                .rg(requisicao.getRg())
                .cpf(requisicao.getCpf())
                .endereco(requisicao.getEndereco())
                .bairro(requisicao.getBairro())
                .cidade(requisicao.getCidade())
                .estado(requisicao.getEstado())
                .cep(requisicao.getCep())
                .telefone(requisicao.getTelefone())
                .celular(requisicao.getCelular())
                .dataNascimento(requisicao.getDataNascimento())
                .genero(requisicao.getGenero())
                .possuiConvenio(requisicao.getPossuiConvenio() != null ? requisicao.getPossuiConvenio() : false)
                .convenio(convenio)
                .build();
    }

    public static PacienteResposta paraResposta(Paciente paciente) {
        if (paciente == null) return null;

        // Obtém o nome do convênio com fallback para null quando o paciente não possui plano
        String nomeConvenio = paciente.getConvenio() != null
                ? paciente.getConvenio().getNomeEmpresa()
                : null;

        return PacienteResposta.builder()
                .id(paciente.getId())
                .nomeCompleto(paciente.getNomeCompleto())
                .rg(paciente.getRg())
                .cpf(paciente.getCpf())
                .endereco(paciente.getEndereco())
                .bairro(paciente.getBairro())
                .cidade(paciente.getCidade())
                .estado(paciente.getEstado())
                .cep(paciente.getCep())
                .telefone(paciente.getTelefone())
                .celular(paciente.getCelular())
                .dataNascimento(paciente.getDataNascimento())
                .genero(paciente.getGenero())
                .possuiConvenio(paciente.getPossuiConvenio())
                .nomeConvenio(nomeConvenio)
                .ativo(paciente.getAtivo())
                .criadoEm(paciente.getCriadoEm())
                .atualizadoEm(paciente.getAtualizadoEm())
                .build();
    }
}
