package br.com.clinica.atendimento.service;

import br.com.clinica.atendimento.dto.requisicao.RelatorioClinicoRequisicao;
import br.com.clinica.atendimento.dto.resposta.RelatorioClinicoResposta;
import br.com.clinica.atendimento.entity.Atendimento;
import br.com.clinica.atendimento.entity.RelatorioClinico;
import br.com.clinica.atendimento.mapper.RelatorioClinicoMapper;
import br.com.clinica.atendimento.repository.RelatorioClinicoRepository;
import br.com.clinica.commons.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RelatorioClinicoService {

    private final RelatorioClinicoRepository relatorioClinicoRepository;
    private final AtendimentoService atendimentoService;

    public RelatorioClinicoResposta criar(RelatorioClinicoRequisicao requisicao) {
        Atendimento atendimento = atendimentoService.buscarEntidadePorId(requisicao.getAtendimentoId());

        RelatorioClinico relatorioClinico = RelatorioClinicoMapper.paraEntidade(requisicao, atendimento);
        RelatorioClinico relatorioSalvo = relatorioClinicoRepository.save(relatorioClinico);

        log.info("Relatório clínico criado com sucesso. ID={}, atendimentoId={}",
                relatorioSalvo.getId(),
                atendimento.getId());

        return RelatorioClinicoMapper.paraResposta(relatorioSalvo);
    }

    public RelatorioClinicoResposta buscarPorId(Long id) {
        RelatorioClinico relatorioClinico = buscarEntidadePorId(id);
        return RelatorioClinicoMapper.paraResposta(relatorioClinico);
    }

    public List<RelatorioClinicoResposta> listarPorPaciente(Long pacienteId) {
        return relatorioClinicoRepository.findByAtendimentoProntuarioPacienteId(pacienteId)
                .stream()
                .map(RelatorioClinicoMapper::paraResposta)
                .toList();
    }

    public RelatorioClinicoResposta atualizar(Long id, RelatorioClinicoRequisicao requisicao) {
        RelatorioClinico relatorioClinico = buscarEntidadePorId(id);
        relatorioClinico.setConteudo(requisicao.getConteudo());

        RelatorioClinico relatorioAtualizado = relatorioClinicoRepository.save(relatorioClinico);

        log.info("Relatório clínico atualizado com sucesso. ID={}", relatorioAtualizado.getId());

        return RelatorioClinicoMapper.paraResposta(relatorioAtualizado);
    }

    private RelatorioClinico buscarEntidadePorId(Long id) {
        return relatorioClinicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Relatório clínico com ID " + id + " não encontrado"));
    }
}