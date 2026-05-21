package br.com.clinica.admin.mapper;

import br.com.clinica.admin.dto.requisicao.MedicoRequisicao;
import br.com.clinica.admin.resposta.MedicoResposta;
import br.com.clinica.admin.entity.Especialidade;
import br.com.clinica.admin.entity.Medico;

public class MedicoMapper {
    public static Medico paraEntidade(MedicoRequisicao requisicao, Especialidade especialidade){
        if (requisicao == null) return null;
        return Medico.builder()
        .nomeCompleto(requisicao.getNomeCompleto())
        .crm(requisicao.getCrm())
        .especialidade(especialidade)
        .build();
    }

    public static MedicoResposta paraResposta(Medico medico){
        if (medico == null) return null;
        String especialidadeDesc = medico.getEspecialidade() != null ? medico.getEspecialidade().getDescricao() : null;
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
