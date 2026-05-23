package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.ConvenioRequisicao;
import br.com.clinica.admin.dto.resposta.ConvenioResposta;
import br.com.clinica.admin.entity.Convenio;

// Mapper responsável por converter a entidade Convenio para seus DTOs de entrada e saída,
// garantindo que o CNPJ e outros dados sensíveis não sejam manipulados fora do domínio.
public class ConvenioMapper {

    public static Convenio paraEntidade(ConvenioRequisicao requisicao) {
        if (requisicao == null) return null;
        return Convenio.builder()
                .nomeEmpresa(requisicao.getNomeEmpresa())
                .cnpj(requisicao.getCnpj())
                .telefone(requisicao.getTelefone())
                .build();
    }

    public static ConvenioResposta paraResposta(Convenio convenio) {
        if (convenio == null) return null;
        return ConvenioResposta.builder()
                .id(convenio.getId())
                .nomeEmpresa(convenio.getNomeEmpresa())
                .cnpj(convenio.getCnpj())
                .telefone(convenio.getTelefone())
                .ativo(convenio.getAtivo())
                .criadoEm(convenio.getCriadoEm())
                .atualizadoEm(convenio.getAtualizadoEm())
                .build();
    }
}
