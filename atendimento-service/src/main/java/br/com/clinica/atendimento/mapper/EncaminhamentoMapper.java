package br.com.clinica.atendimento.mapper;

import br.com.clinica.atendimento.dto.requisicao.EncaminhamentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.EncaminhamentoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Encaminhamento;
import br.com.clinica.atendimento.enums.PrioridadeEncaminhamento;
import br.com.clinica.atendimento.enums.StatusEncaminhamento;

public class EncaminhamentoMapper {

    private EncaminhamentoMapper() {
    }

    public static Encaminhamento paraEntidade(EncaminhamentoRequisicao requisicao, Atendimento atendimento) {
        if (requisicao == null) {
            return null;
        }

        return Encaminhamento.builder()
                .atendimento(atendimento)
                .medicoOrigemId(requisicao.getMedicoOrigemId())
                .medicoDestinoId(requisicao.getMedicoDestinoId())
                .motivo(requisicao.getMotivo())
                .especialidadeDestino(requisicao.getEspecialidadeDestino())
                .prioridade(requisicao.getPrioridade() != null ? requisicao.getPrioridade() : PrioridadeEncaminhamento.MEDIA)
                .status(StatusEncaminhamento.PENDENTE)
                .build();
    }

    public static EncaminhamentoResposta paraResposta(Encaminhamento encaminhamento) {
        if (encaminhamento == null) {
            return null;
        }

        return EncaminhamentoResposta.builder()
                .id(encaminhamento.getId())
                .atendimentoId(encaminhamento.getAtendimento().getId())
                .medicoOrigemId(encaminhamento.getMedicoOrigemId())
                .medicoDestinoId(encaminhamento.getMedicoDestinoId())
                .motivo(encaminhamento.getMotivo())
                .especialidadeDestino(encaminhamento.getEspecialidadeDestino())
                .prioridade(encaminhamento.getPrioridade())
                .status(encaminhamento.getStatus())
                .criadoEm(encaminhamento.getCriadoEm())
                .build();
    }
}