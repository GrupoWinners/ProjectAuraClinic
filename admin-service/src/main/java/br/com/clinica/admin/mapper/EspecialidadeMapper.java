package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.EspecialidadeRequisicao;
import br.com.clinica.admin.dto.resposta.EspecialidadeResposta;
import br.com.clinica.admin.entity.Especialidade;

// Mapper responsável por converter a entidade Especialidade para seus DTOs de entrada e saída,
// isolando o domínio da camada HTTP.
public class EspecialidadeMapper {

    public static Especialidade paraEntidade(EspecialidadeRequisicao requisicao) {
        if (requisicao == null) return null;
        return Especialidade.builder()
                .descricao(requisicao.getDescricao())
                .build();
    }

    public static EspecialidadeResposta paraResposta(Especialidade especialidade) {
        if (especialidade == null) return null;
        return EspecialidadeResposta.builder()
                .id(especialidade.getId())
                .descricao(especialidade.getDescricao())
                .ativo(especialidade.getAtivo())
                .criadoEm(especialidade.getCriadoEm())
                .build();
    }
}
