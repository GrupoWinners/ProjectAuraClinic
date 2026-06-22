package br.com.clinica.atendimento.mapper;

import br.com.clinica.atendimento.dto.requisicao.RelatorioClinicoRequisicao;
import br.com.clinica.atendimento.dto.resposta.RelatorioClinicoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.RelatorioClinico;

public class RelatorioClinicoMapper {

    private RelatorioClinicoMapper() {
    }

    public static RelatorioClinico paraEntidade(RelatorioClinicoRequisicao requisicao, Atendimento atendimento) {
        if (requisicao == null) {
            return null;
        }

        return RelatorioClinico.builder()
                .atendimento(atendimento)
                .conteudo(requisicao.getConteudo())
                .build();
    }

    public static RelatorioClinicoResposta paraResposta(RelatorioClinico relatorioClinico) {
        if (relatorioClinico == null) {
            return null;
        }

        return RelatorioClinicoResposta.builder()
                .id(relatorioClinico.getId())
                .atendimentoId(relatorioClinico.getAtendimento().getId())
                .conteudo(relatorioClinico.getConteudo())
                .criadoEm(relatorioClinico.getCriadoEm())
                .atualizadoEm(relatorioClinico.getAtualizadoEm())
                .build();
    }
}