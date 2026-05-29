package br.com.clinica.atendimento.mapper;

import br.com.clinica.atendimento.dto.requisicao.AtendimentoRequisicao;
import br.com.clinica.atendimento.dto.resposta.AtendimentoResposta;
import br.com.clinica.atendimento.dto.resposta.ReceitaResposta;
import br.com.clinica.atendimento.dto.resposta.SolicitacaoExameResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.Prontuario;
import br.com.clinica.atendimento.entity.Receita;
import br.com.clinica.atendimento.entity.SolicitacaoExame;
import br.com.clinica.atendimento.enums.NivelUrgencia;

import java.util.ArrayList;
import java.util.List;

public class AtendimentoMapper {

    private AtendimentoMapper() {
    }

    public static Atendimento paraEntidade(AtendimentoRequisicao requisicao, Prontuario prontuario) {
        if (requisicao == null) {
            return null;
        }

        Atendimento atendimento = Atendimento.builder()
                .prontuario(prontuario)
                .consultaId(requisicao.getConsultaId())
                .medicoId(requisicao.getMedicoId())
                .sintomas(requisicao.getSintomas())
                .diagnostico(requisicao.getDiagnostico())
                .escopoMedico(requisicao.getEscopoMedico())
                .observacoes(requisicao.getObservacoes())
                .nivelUrgencia(requisicao.getNivelUrgencia() != null ? requisicao.getNivelUrgencia() : NivelUrgencia.VERDE)
                .receitas(new ArrayList<>())
                .solicitacoesExame(new ArrayList<>())
                .build();

        if (requisicao.getReceitas() != null) {
            List<Receita> receitas = requisicao.getReceitas()
                    .stream()
                    .map(receitaRequisicao -> ReceitaMapper.paraEntidade(receitaRequisicao, atendimento))
                    .toList();

            atendimento.getReceitas().addAll(receitas);
        }

        if (requisicao.getSolicitacoesExame() != null) {
            List<SolicitacaoExame> solicitacoesExame = requisicao.getSolicitacoesExame()
                    .stream()
                    .map(solicitacaoRequisicao -> SolicitacaoExameMapper.paraEntidade(solicitacaoRequisicao, atendimento))
                    .toList();

            atendimento.getSolicitacoesExame().addAll(solicitacoesExame);
        }

        return atendimento;
    }

    public static AtendimentoResposta paraResposta(Atendimento atendimento) {
        if (atendimento == null) {
            return null;
        }

        List<ReceitaResposta> receitas = atendimento.getReceitas() == null
                ? List.of()
                : atendimento.getReceitas()
                .stream()
                .map(ReceitaMapper::paraResposta)
                .toList();

        List<SolicitacaoExameResposta> solicitacoesExame = atendimento.getSolicitacoesExame() == null
                ? List.of()
                : atendimento.getSolicitacoesExame()
                .stream()
                .map(SolicitacaoExameMapper::paraResposta)
                .toList();

        return AtendimentoResposta.builder()
                .id(atendimento.getId())
                .prontuarioId(atendimento.getProntuario().getId())
                .pacienteId(atendimento.getProntuario().getPacienteId())
                .consultaId(atendimento.getConsultaId())
                .medicoId(atendimento.getMedicoId())
                .dataAtendimento(atendimento.getDataAtendimento())
                .sintomas(atendimento.getSintomas())
                .diagnostico(atendimento.getDiagnostico())
                .escopoMedico(atendimento.getEscopoMedico())
                .observacoes(atendimento.getObservacoes())
                .nivelUrgencia(atendimento.getNivelUrgencia())
                .receitas(receitas)
                .solicitacoesExame(solicitacoesExame)
                .criadoEm(atendimento.getCriadoEm())
                .atualizadoEm(atendimento.getAtualizadoEm())
                .build();
    }
}