package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.MedicoRequisicao;
import br.com.clinica.admin.dto.resposta.MedicoResposta;
import br.com.clinica.admin.entity.Especialidade;
import br.com.clinica.admin.entity.Medico;

// Mapper responsável por converter a entidade Medico para seus DTOs de entrada e saída,
// isolando o domínio da camada HTTP e evitando exposição direta da entidade na API.
public class MedicoMapper {

    public static Medico paraEntidade(MedicoRequisicao requisicao, Especialidade especialidade) {
        if (requisicao == null) return null;
        return Medico.builder()
                .nomeCompleto(requisicao.getNomeCompleto())
                .crm(requisicao.getCrm())
                .especialidade(especialidade)
                .build();
    }

    public static MedicoResposta paraResposta(Medico medico) {
        if (medico == null) return null;

        // Obtém a descrição da especialidade com fallback para nulo caso o vínculo não exista
        String especialidadeDesc = medico.getEspecialidade() != null
                ? medico.getEspecialidade().getDescricao()
                : null;

        return MedicoResposta.builder()
                .id(medico.getId())
                .nomeCompleto(medico.getNomeCompleto())
                .crm(medico.getCrm())
                .especialidade(especialidadeDesc)
                .ativo(medico.getAtivo())
                .criadoEm(medico.getCriadoEm())
                .build();
    }
}
