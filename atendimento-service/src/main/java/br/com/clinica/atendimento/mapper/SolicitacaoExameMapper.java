package br.com.clinica.atendimento.mapper;

import br.com.clinica.atendimento.dto.requisicao.SolicitacaoExameRequisicao;
import br.com.clinica.atendimento.dto.resposta.SolicitacaoExameResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.SolicitacaoExame;
import br.com.clinica.atendimento.enums.StatusExame;
import br.com.clinica.atendimento.enums.UrgenciaExame;

public class SolicitacaoExameMapper {

    private SolicitacaoExameMapper() {
    }

    public static SolicitacaoExame paraEntidade(SolicitacaoExameRequisicao requisicao, Atendimento atendimento) {
        if (requisicao == null) {
            return null;
        }

        return SolicitacaoExame.builder()
                .atendimento(atendimento)
                .tipoExame(requisicao.getTipoExame())
                .descricao(requisicao.getDescricao())
                .urgencia(requisicao.getUrgencia() != null ? requisicao.getUrgencia() : UrgenciaExame.NORMAL)
                .status(StatusExame.SOLICITADO)
                .build();
    }

    public static SolicitacaoExameResposta paraResposta(SolicitacaoExame solicitacaoExame) {
        if (solicitacaoExame == null) {
            return null;
        }

        return SolicitacaoExameResposta.builder()
                .id(solicitacaoExame.getId())
                .tipoExame(solicitacaoExame.getTipoExame())
                .descricao(solicitacaoExame.getDescricao())
                .urgencia(solicitacaoExame.getUrgencia())
                .status(solicitacaoExame.getStatus())
                .criadoEm(solicitacaoExame.getCriadoEm())
                .build();
    }
}